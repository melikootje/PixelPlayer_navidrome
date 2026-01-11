package com.theveloper.pixelplay.data.repository

import com.theveloper.pixelplay.data.model.Album
import com.theveloper.pixelplay.data.model.Artist
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.model.ArtistRef
import com.theveloper.pixelplay.data.network.subsonic.*
import com.theveloper.pixelplay.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * Repository for fetching music data from Subsonic/Navidrome server
 */
@Singleton
class SubsonicRepository @Inject constructor(
    private val subsonicApiService: SubsonicApiService,
    private val authHelper: SubsonicAuthHelper,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private suspend fun getAuthParams(): Triple<String, String, String>? {
        val serverUrl = userPreferencesRepository.subsonicServerUrlFlow.first()
        val username = userPreferencesRepository.subsonicUsernameFlow.first()
        val password = userPreferencesRepository.subsonicPasswordFlow.first()

        if (serverUrl.isBlank() || username.isBlank() || password.isBlank()) {
            return null
        }

        val (token, salt) = authHelper.generateAuthParams(password)
        return Triple(username, token, salt)
    }

    suspend fun testConnection(): Result<Boolean> {
        return try {
            val auth = getAuthParams() ?: return Result.failure(Exception("Server not configured"))
            val (username, token, salt) = auth

            val response = subsonicApiService.ping(username, token, salt)
            if (response.subsonicResponse.status == "ok") {
                Result.success(true)
            } else {
                val error = response.subsonicResponse.error
                Result.failure(Exception(error?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e("SubsonicRepository", "Connection test failed", e)
            Result.failure(e)
        }
    }

    suspend fun getArtists(): Result<List<Artist>> {
        return try {
            val auth = getAuthParams() ?: return Result.failure(Exception("Server not configured"))
            val (username, token, salt) = auth

            val response = subsonicApiService.getArtists(username, token, salt)
            if (response.subsonicResponse.status != "ok") {
                val error = response.subsonicResponse.error
                return Result.failure(Exception(error?.message ?: "Unknown error"))
            }

            val artists = mutableListOf<Artist>()
            response.subsonicResponse.artists?.index?.forEach { index ->
                index.artist.forEach { subsonicArtist ->
                    artists.add(subsonicArtist.toArtist())
                }
            }

            Result.success(artists)
        } catch (e: Exception) {
            Log.e("SubsonicRepository", "Failed to fetch artists", e)
            Result.failure(e)
        }
    }

    suspend fun getArtist(id: String): Result<Artist> {
        return try {
            val auth = getAuthParams() ?: return Result.failure(Exception("Server not configured"))
            val (username, token, salt) = auth

            val response = subsonicApiService.getArtist(username, token, salt, id)
            if (response.subsonicResponse.status != "ok") {
                val error = response.subsonicResponse.error
                return Result.failure(Exception(error?.message ?: "Unknown error"))
            }

            val artist = response.subsonicResponse.artist
                ?: return Result.failure(Exception("Artist not found"))

            Result.success(artist.toArtist())
        } catch (e: Exception) {
            Log.e("SubsonicRepository", "Failed to fetch artist", e)
            Result.failure(e)
        }
    }

    suspend fun getArtistWithAlbums(id: String): Result<Pair<Artist, List<Album>>> {
        return try {
            val auth = getAuthParams() ?: return Result.failure(Exception("Server not configured"))
            val (username, token, salt) = auth

            val response = subsonicApiService.getArtist(username, token, salt, id)
            if (response.subsonicResponse.status != "ok") {
                val error = response.subsonicResponse.error
                return Result.failure(Exception(error?.message ?: "Unknown error"))
            }

            val artistResult = response.subsonicResponse.artist
                ?: return Result.failure(Exception("Artist not found"))

            val artist = artistResult.toArtist()
            val albums = artistResult.album?.map { it.toAlbum() } ?: emptyList()

            Log.d("SubsonicRepository", "getArtistWithAlbums(${id}): Artist '${artist.name}' has ${artistResult.album?.size ?: 0} albums in API response")
            if (albums.isNotEmpty()) {
                Log.d("SubsonicRepository", "  Albums: ${albums.joinToString(", ") { "'${it.title}' (id=${it.subsonicId})" }}")
            }

            Result.success(Pair(artist, albums))
        } catch (e: Exception) {
            Log.e("SubsonicRepository", "Failed to fetch artist with albums", e)
            Result.failure(e)
        }
    }

    suspend fun getAlbum(id: String): Result<Pair<Album, List<Song>>> {
        return try {
            val auth = getAuthParams() ?: return Result.failure(Exception("Server not configured"))
            val (username, token, salt) = auth

            Log.d("SubsonicRepository", "getAlbum($id): Calling API...")
            val response = subsonicApiService.getAlbum(username, token, salt, id)

            Log.d("SubsonicRepository", "getAlbum($id): Response status = ${response.subsonicResponse.status}")

            if (response.subsonicResponse.status != "ok") {
                val error = response.subsonicResponse.error
                Log.e("SubsonicRepository", "getAlbum($id): API returned error: ${error?.message} (code=${error?.code})")
                return Result.failure(Exception(error?.message ?: "Unknown error"))
            }

            val albumResult = response.subsonicResponse.album
            if (albumResult == null) {
                Log.e("SubsonicRepository", "getAlbum($id): API returned OK but album field is null")
                return Result.failure(Exception("Album not found"))
            }

            val album = albumResult.toAlbum()
            val songs = albumResult.song?.map { it.toSong(getServerUrl(), auth) } ?: emptyList()

            Log.d("SubsonicRepository", "getAlbum(${id}): Album '${album.title}' has ${albumResult.song?.size ?: 0} songs in response, mapped to ${songs.size} song entities")

            Result.success(Pair(album, songs))
        } catch (e: Exception) {
            Log.e("SubsonicRepository", "getAlbum($id): Exception occurred: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getAlbumList(type: String, size: Int = 50, offset: Int = 0): Result<List<Album>> {
        return try {
            val auth = getAuthParams() ?: return Result.failure(Exception("Server not configured"))
            val (username, token, salt) = auth

            val response = subsonicApiService.getAlbumList2(username, token, salt, type, size, offset)
            if (response.subsonicResponse.status != "ok") {
                val error = response.subsonicResponse.error
                return Result.failure(Exception(error?.message ?: "Unknown error"))
            }

            val albums = response.subsonicResponse.albumList2?.album?.map { it.toAlbum() } ?: emptyList()

            Result.success(albums)
        } catch (e: Exception) {
            Log.e("SubsonicRepository", "Failed to fetch album list", e)
            Result.failure(e)
        }
    }

    suspend fun getRandomSongs(size: Int = 50, genre: String? = null): Result<List<Song>> {
        return try {
            val auth = getAuthParams() ?: return Result.failure(Exception("Server not configured"))
            val (username, token, salt) = auth

            val response = subsonicApiService.getRandomSongs(username, token, salt, size, genre = genre)
            if (response.subsonicResponse.status != "ok") {
                val error = response.subsonicResponse.error
                return Result.failure(Exception(error?.message ?: "Unknown error"))
            }

            val songs = response.subsonicResponse.randomSongs?.song?.map { 
                it.toSong(getServerUrl(), auth) 
            } ?: emptyList()

            Result.success(songs)
        } catch (e: Exception) {
            Log.e("SubsonicRepository", "Failed to fetch random songs", e)
            Result.failure(e)
        }
    }

    suspend fun search(query: String): Result<Triple<List<Artist>, List<Album>, List<Song>>> {
        return try {
            val auth = getAuthParams() ?: return Result.failure(Exception("Server not configured"))
            val (username, token, salt) = auth

            val response = subsonicApiService.search3(username, token, salt, query)
            if (response.subsonicResponse.status != "ok") {
                val error = response.subsonicResponse.error
                return Result.failure(Exception(error?.message ?: "Unknown error"))
            }

            val searchResult = response.subsonicResponse.searchResult3
            val artists = searchResult?.artist?.map { it.toArtist() } ?: emptyList()
            val albums = searchResult?.album?.map { it.toAlbum() } ?: emptyList()
            val songs = searchResult?.song?.map { it.toSong(getServerUrl(), auth) } ?: emptyList()

            Result.success(Triple(artists, albums, songs))
        } catch (e: Exception) {
            Log.e("SubsonicRepository", "Search failed", e)
            Result.failure(e)
        }
    }

    suspend fun starItem(songId: String? = null, albumId: String? = null, artistId: String? = null): Result<Boolean> {
        return try {
            val auth = getAuthParams() ?: return Result.failure(Exception("Server not configured"))
            val (username, token, salt) = auth

            val response = subsonicApiService.star(username, token, salt, songId, albumId, artistId)
            if (response.subsonicResponse.status == "ok") {
                Result.success(true)
            } else {
                val error = response.subsonicResponse.error
                Result.failure(Exception(error?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e("SubsonicRepository", "Failed to star item", e)
            Result.failure(e)
        }
    }

    suspend fun unstarItem(songId: String? = null, albumId: String? = null, artistId: String? = null): Result<Boolean> {
        return try {
            val auth = getAuthParams() ?: return Result.failure(Exception("Server not configured"))
            val (username, token, salt) = auth

            val response = subsonicApiService.unstar(username, token, salt, songId, albumId, artistId)
            if (response.subsonicResponse.status == "ok") {
                Result.success(true)
            } else {
                val error = response.subsonicResponse.error
                Result.failure(Exception(error?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e("SubsonicRepository", "Failed to unstar item", e)
            Result.failure(e)
        }
    }

    suspend fun scrobble(songId: String, time: Long? = null): Result<Boolean> {
        return try {
            val auth = getAuthParams() ?: return Result.failure(Exception("Server not configured"))
            val (username, token, salt) = auth

            val response = subsonicApiService.scrobble(username, token, salt, songId, time)
            if (response.subsonicResponse.status == "ok") {
                Result.success(true)
            } else {
                val error = response.subsonicResponse.error
                Result.failure(Exception(error?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e("SubsonicRepository", "Failed to scrobble", e)
            Result.failure(e)
        }
    }

    fun getStreamUrl(songId: String): String? {
        val serverUrl = runCatching { 
            kotlinx.coroutines.runBlocking { 
                userPreferencesRepository.subsonicServerUrlFlow.first() 
            } 
        }.getOrNull()
        val username = runCatching { 
            kotlinx.coroutines.runBlocking { 
                userPreferencesRepository.subsonicUsernameFlow.first() 
            } 
        }.getOrNull()
        val password = runCatching { 
            kotlinx.coroutines.runBlocking { 
                userPreferencesRepository.subsonicPasswordFlow.first() 
            } 
        }.getOrNull()

        if (serverUrl.isNullOrBlank() || username.isNullOrBlank() || password.isNullOrBlank()) {
            return null
        }

        val (token, salt) = authHelper.generateAuthParams(password)
        return SubsonicUrlHelper.getStreamUrl(
            authHelper.normalizeServerUrl(serverUrl),
            username,
            token,
            salt,
            songId
        )
    }

    fun getCoverArtUrl(coverArtId: String, size: Int? = null): String? {
        val serverUrl = runCatching { 
            kotlinx.coroutines.runBlocking { 
                userPreferencesRepository.subsonicServerUrlFlow.first() 
            } 
        }.getOrNull()
        val username = runCatching { 
            kotlinx.coroutines.runBlocking { 
                userPreferencesRepository.subsonicUsernameFlow.first() 
            } 
        }.getOrNull()
        val password = runCatching { 
            kotlinx.coroutines.runBlocking { 
                userPreferencesRepository.subsonicPasswordFlow.first() 
            } 
        }.getOrNull()

        if (serverUrl.isNullOrBlank() || username.isNullOrBlank() || password.isNullOrBlank()) {
            return null
        }

        val (token, salt) = authHelper.generateAuthParams(password)
        return SubsonicUrlHelper.getCoverArtUrl(
            authHelper.normalizeServerUrl(serverUrl),
            username,
            token,
            salt,
            coverArtId,
            size
        )
    }

    private suspend fun getServerUrl(): String {
        return authHelper.normalizeServerUrl(
            userPreferencesRepository.subsonicServerUrlFlow.first()
        )
    }

    // Extension functions to convert Subsonic models to app models
    private fun SubsonicArtist.toArtist(): Artist {
        return Artist(
            id = id.toLongOrNull() ?: id.hashCode().toLong(),
            name = name,
            songCount = 0, // Not provided by Subsonic getArtists
            imageUrl = coverArt?.let { getCoverArtUrl(it) },
            subsonicId = id // Preserve original Subsonic string ID
        )
    }

    private fun ArtistResult.toArtist(): Artist {
        return Artist(
            id = id.toLongOrNull() ?: id.hashCode().toLong(),
            name = name,
            songCount = album?.sumOf { it.songCount } ?: 0,
            imageUrl = coverArt?.let { getCoverArtUrl(it) },
            subsonicId = id // Preserve original Subsonic string ID
        )
    }

    private fun SubsonicAlbum.toAlbum(): Album {
        return Album(
            id = id.toLongOrNull() ?: id.hashCode().toLong(),
            title = getDisplayTitle(),
            artist = artist ?: "Unknown Artist",
            year = year ?: 0,
            albumArtUriString = coverArt?.let { getCoverArtUrl(it) },
            songCount = songCount,
            subsonicId = id // Preserve original Subsonic string ID
        )
    }

    private fun AlbumResult.toAlbum(): Album {
        val displayTitle = title ?: name ?: "Unknown Album"
        return Album(
            id = id.toLongOrNull() ?: id.hashCode().toLong(),
            title = displayTitle,
            artist = artist ?: "Unknown Artist",
            year = year ?: 0,
            albumArtUriString = coverArt?.let { getCoverArtUrl(it) },
            songCount = songCount,
            subsonicId = id // Preserve original Subsonic string ID
        )
    }

    private fun SubsonicSong.toSong(serverUrl: String, auth: Triple<String, String, String>): Song {
        val (username, token, salt) = auth
        val streamUrl = SubsonicUrlHelper.getStreamUrl(serverUrl, username, token, salt, id, maxBitRate = 0)

        return Song(
            id = id,
            title = title,
            artist = artist ?: "Unknown Artist",
            artistId = artistId?.toLongOrNull() ?: -1L,
            artists = listOf(ArtistRef(
                id = artistId?.toLongOrNull() ?: -1L,
                name = artist ?: "Unknown Artist",
                isPrimary = true
            )),
            album = album ?: "Unknown Album",
            albumId = albumId?.toLongOrNull() ?: -1L,
            albumArtist = artist,
            path = streamUrl,
            contentUriString = streamUrl,
            albumArtUriString = coverArt?.let { getCoverArtUrl(it) },
            duration = (duration * 1000).toLong(), // Convert seconds to milliseconds
            genre = genre,
            lyrics = null,
            isFavorite = starred != null,
            trackNumber = track ?: 0,
            year = year ?: 0,
            dateAdded = System.currentTimeMillis(),
            mimeType = contentType ?: "audio/mpeg",
            bitrate = bitRate ?: 0,
            sampleRate = 0
        )
    }
}
