package com.theveloper.pixelplay.data.network.subsonic

import com.google.gson.annotations.SerializedName

/**
 * Subsonic API response models
 */

data class SubsonicResponse<T>(
    @SerializedName("subsonic-response")
    val subsonicResponse: SubsonicResponseBody<T>
)

data class SubsonicResponseBody<T>(
    @SerializedName("status")
    val status: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("serverVersion")
    val serverVersion: String? = null,
    @SerializedName("error")
    val error: SubsonicError? = null,
    // Data fields
    @SerializedName("artists")
    val artists: ArtistsResult? = null,
    @SerializedName("artist")
    val artist: ArtistResult? = null,
    @SerializedName("album")
    val album: AlbumResult? = null,
    @SerializedName("song")
    val song: SongResult? = null,
    @SerializedName("randomSongs")
    val randomSongs: RandomSongsResult? = null,
    @SerializedName("albumList2")
    val albumList2: AlbumList2Result? = null,
    @SerializedName("searchResult3")
    val searchResult3: SearchResult3? = null,
    @SerializedName("genres")
    val genres: GenresResult? = null,
    @SerializedName("starred2")
    val starred2: Starred2Result? = null,
    @SerializedName("playlists")
    val playlists: PlaylistsResult? = null,
    @SerializedName("playlist")
    val playlist: PlaylistResult? = null
) {
    inline fun <reified T> getData(): T? {
        return when (T::class) {
            ArtistsResult::class -> artists as? T
            ArtistResult::class -> artist as? T
            AlbumResult::class -> album as? T
            SongResult::class -> song as? T
            RandomSongsResult::class -> randomSongs as? T
            AlbumList2Result::class -> albumList2 as? T
            SearchResult3::class -> searchResult3 as? T
            GenresResult::class -> genres as? T
            Starred2Result::class -> starred2 as? T
            PlaylistsResult::class -> playlists as? T
            PlaylistResult::class -> playlist as? T
            else -> null
        }
    }
}

data class SubsonicError(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String
)

// Artists
data class ArtistsResult(
    @SerializedName("index")
    val index: List<ArtistIndex>? = null
)

data class ArtistIndex(
    @SerializedName("name")
    val name: String,
    @SerializedName("artist")
    val artist: List<SubsonicArtist>
)

data class SubsonicArtist(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("coverArt")
    val coverArt: String? = null,
    @SerializedName("albumCount")
    val albumCount: Int = 0,
    @SerializedName("starred")
    val starred: String? = null
)

// Artist Detail
data class ArtistResult(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("coverArt")
    val coverArt: String? = null,
    @SerializedName("albumCount")
    val albumCount: Int = 0,
    @SerializedName("starred")
    val starred: String? = null,
    @SerializedName("album")
    val album: List<SubsonicAlbum>? = null
)

// Albums
data class SubsonicAlbum(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("artist")
    val artist: String? = null,
    @SerializedName("artistId")
    val artistId: String? = null,
    @SerializedName("coverArt")
    val coverArt: String? = null,
    @SerializedName("songCount")
    val songCount: Int = 0,
    @SerializedName("duration")
    val duration: Int = 0,
    @SerializedName("created")
    val created: String? = null,
    @SerializedName("year")
    val year: Int? = null,
    @SerializedName("genre")
    val genre: String? = null,
    @SerializedName("starred")
    val starred: String? = null
) {
    fun getDisplayTitle(): String = title ?: name ?: "Unknown Album"
}

data class AlbumResult(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("artist")
    val artist: String? = null,
    @SerializedName("artistId")
    val artistId: String? = null,
    @SerializedName("coverArt")
    val coverArt: String? = null,
    @SerializedName("songCount")
    val songCount: Int = 0,
    @SerializedName("duration")
    val duration: Int = 0,
    @SerializedName("created")
    val created: String? = null,
    @SerializedName("year")
    val year: Int? = null,
    @SerializedName("genre")
    val genre: String? = null,
    @SerializedName("starred")
    val starred: String? = null,
    @SerializedName("song")
    val song: List<SubsonicSong>? = null
)

// Songs
data class SubsonicSong(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("artist")
    val artist: String? = null,
    @SerializedName("artistId")
    val artistId: String? = null,
    @SerializedName("album")
    val album: String? = null,
    @SerializedName("albumId")
    val albumId: String? = null,
    @SerializedName("coverArt")
    val coverArt: String? = null,
    @SerializedName("duration")
    val duration: Int = 0,
    @SerializedName("bitRate")
    val bitRate: Int? = null,
    @SerializedName("track")
    val track: Int? = null,
    @SerializedName("year")
    val year: Int? = null,
    @SerializedName("genre")
    val genre: String? = null,
    @SerializedName("size")
    val size: Long? = null,
    @SerializedName("contentType")
    val contentType: String? = null,
    @SerializedName("suffix")
    val suffix: String? = null,
    @SerializedName("path")
    val path: String? = null,
    @SerializedName("starred")
    val starred: String? = null,
    @SerializedName("type")
    val type: String? = null
)

data class SongResult(
    @SerializedName("song")
    val song: SubsonicSong
)

// Random Songs
data class RandomSongsResult(
    @SerializedName("song")
    val song: List<SubsonicSong>? = null
)

// Album List
data class AlbumList2Result(
    @SerializedName("album")
    val album: List<SubsonicAlbum>? = null
)

// Search
data class SearchResult3(
    @SerializedName("artist")
    val artist: List<SubsonicArtist>? = null,
    @SerializedName("album")
    val album: List<SubsonicAlbum>? = null,
    @SerializedName("song")
    val song: List<SubsonicSong>? = null
)

// Genres
data class GenresResult(
    @SerializedName("genre")
    val genre: List<SubsonicGenre>? = null
)

data class SubsonicGenre(
    @SerializedName("value")
    val value: String,
    @SerializedName("songCount")
    val songCount: Int = 0,
    @SerializedName("albumCount")
    val albumCount: Int = 0
)

// Starred
data class Starred2Result(
    @SerializedName("artist")
    val artist: List<SubsonicArtist>? = null,
    @SerializedName("album")
    val album: List<SubsonicAlbum>? = null,
    @SerializedName("song")
    val song: List<SubsonicSong>? = null
)

// Playlists
data class PlaylistsResult(
    @SerializedName("playlist")
    val playlist: List<SubsonicPlaylist>? = null
)

data class SubsonicPlaylist(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("owner")
    val owner: String? = null,
    @SerializedName("public")
    val public: Boolean = false,
    @SerializedName("songCount")
    val songCount: Int = 0,
    @SerializedName("duration")
    val duration: Int = 0,
    @SerializedName("created")
    val created: String? = null,
    @SerializedName("changed")
    val changed: String? = null,
    @SerializedName("coverArt")
    val coverArt: String? = null
)

data class PlaylistResult(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("owner")
    val owner: String? = null,
    @SerializedName("public")
    val public: Boolean = false,
    @SerializedName("songCount")
    val songCount: Int = 0,
    @SerializedName("duration")
    val duration: Int = 0,
    @SerializedName("created")
    val created: String? = null,
    @SerializedName("changed")
    val changed: String? = null,
    @SerializedName("coverArt")
    val coverArt: String? = null,
    @SerializedName("entry")
    val entry: List<SubsonicSong>? = null
)
