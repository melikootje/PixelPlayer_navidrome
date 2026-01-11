package com.theveloper.pixelplay.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.theveloper.pixelplay.data.database.AlbumEntity
import com.theveloper.pixelplay.data.database.ArtistEntity
import com.theveloper.pixelplay.data.database.MusicDao
import com.theveloper.pixelplay.data.database.SongArtistCrossRef
import com.theveloper.pixelplay.data.database.SongEntity
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.repository.SubsonicRepository
import com.theveloper.pixelplay.data.preferences.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Worker that syncs music library from Navidrome/Subsonic server
 */
@HiltWorker
class NavidromeSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val subsonicRepository: SubsonicRepository,
    private val musicDao: MusicDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    /**
     * Temporary data class to track an album with its parent artist ID.
     * Needed because Album model only has artist name, not artist ID.
     */
    private data class AlbumWithArtist(
        val album: com.theveloper.pixelplay.data.model.Album,
        val artistId: Long
    )

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo("Preparing sync...")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "========================================")
            Log.i(TAG, "Starting Navidrome library sync...")
            Log.i(TAG, "========================================")

            // Set foreground notification
            setForeground(createForegroundInfo("Starting sync..."))

            val startTime = System.currentTimeMillis()

            // Log configuration for debugging
            val isEnabled = userPreferencesRepository.subsonicEnabledFlow.first()
            val serverUrl = userPreferencesRepository.subsonicServerUrlFlow.first()
            val username = userPreferencesRepository.subsonicUsernameFlow.first()

            Log.i(TAG, "Configuration check:")
            Log.i(TAG, "  - Subsonic enabled: $isEnabled")
            Log.i(TAG, "  - Server URL: ${if (serverUrl.isBlank()) "[EMPTY]" else serverUrl}")
            Log.i(TAG, "  - Username: ${if (username.isBlank()) "[EMPTY]" else username}")

            if (!isEnabled) {
                Log.w(TAG, "Navidrome/Subsonic is DISABLED in settings. Cannot sync.")
                Log.w(TAG, "Please enable it in Settings > Navidrome/Subsonic")
                return@withContext Result.failure(workDataOf(
                    "error" to "Navidrome/Subsonic is not enabled"
                ))
            }

            if (serverUrl.isBlank() || username.isBlank()) {
                Log.e(TAG, "Server URL or username is empty. Please configure Navidrome settings.")
                return@withContext Result.failure(workDataOf(
                    "error" to "Server URL or username not configured"
                ))
            }

            // Test connection first
            setForeground(createForegroundInfo("Connecting to server..."))
            Log.i(TAG, "Testing connection to Navidrome server...")
            val connectionResult = subsonicRepository.testConnection()
            if (connectionResult.isFailure) {
                val error = connectionResult.exceptionOrNull()
                Log.e(TAG, "Connection test FAILED: ${error?.message}")
                Log.e(TAG, "Error details:", error)
                return@withContext Result.failure(workDataOf(
                    "error" to "Connection test failed: ${error?.message}"
                ))
            }

            Log.i(TAG, "Connection test SUCCESSFUL! Proceeding with sync...")

            setProgress(workDataOf(
                PROGRESS_PHASE to 0,
                PROGRESS_MESSAGE to "Fetching artists..."
            ))
            setForeground(createForegroundInfo("Fetching artists..."))

            // Fetch all artists
            val artistsResult = subsonicRepository.getArtists()
            if (artistsResult.isFailure) {
                Log.e(TAG, "Failed to fetch artists: ${artistsResult.exceptionOrNull()?.message}")
                return@withContext Result.failure()
            }

            val artists = artistsResult.getOrNull() ?: emptyList()
            Log.i(TAG, "Fetched ${artists.size} artists from Navidrome")

            setProgress(workDataOf(
                PROGRESS_PHASE to 1,
                PROGRESS_MESSAGE to "Fetching albums and songs..."
            ))

            // Fetch songs by going through each artist and their albums
            // This is more reliable than getAlbumList2 which has ID issues
            val allSongs = mutableListOf<Song>()
            val allAlbumsWithArtists = mutableListOf<AlbumWithArtist>()

            var processedArtists = 0
            val totalArtists = artists.size

            for (artist in artists) {
                try {
                    // Fetch artist details which includes their albums
                    // Use the original Subsonic ID if available, otherwise fall back to numeric ID
                    val artistId = artist.subsonicId ?: artist.id.toString()
                    val artistDetailResult = subsonicRepository.getArtistWithAlbums(artistId)

                    if (artistDetailResult.isSuccess) {
                        val (artistDetails, albums) = artistDetailResult.getOrNull() ?: continue

                        if (albums.isEmpty()) {
                            Log.d(TAG, "Artist '${artist.name}' (subsonicId=${artist.subsonicId}) returned 0 albums - skipping")
                        } else {
                            Log.d(TAG, "Artist '${artist.name}' (subsonicId=${artist.subsonicId}) returned ${albums.size} albums")
                        }

                        // Process each album to get songs
                        for (album in albums) {
                            try {
                                // Add album to our collection if not already there, tracking its artist
                                if (allAlbumsWithArtists.none { it.album.id == album.id }) {
                                    allAlbumsWithArtists.add(AlbumWithArtist(album, artist.id))
                                }

                                // Fetch album details to get songs
                                // Use the original Subsonic ID if available, otherwise fall back to numeric ID
                                val albumId = album.subsonicId ?: album.id.toString()
                                Log.d(TAG, "  Fetching album '${album.title}' with subsonicId='${album.subsonicId}' (numeric id=${album.id})")
                                val albumDetailResult = subsonicRepository.getAlbum(albumId)

                                if (albumDetailResult.isSuccess) {
                                    val (_, songs) = albumDetailResult.getOrNull() ?: continue

                                    if (songs.isNotEmpty()) {
                                        allSongs.addAll(songs)
                                        Log.d(TAG, "    → Album '${album.title}' has ${songs.size} songs")
                                    } else {
                                        Log.w(TAG, "    → Album '${album.title}' returned 0 songs!")
                                    }
                                } else {
                                    val error = albumDetailResult.exceptionOrNull()
                                    Log.w(TAG, "    → Failed to fetch album '${album.title}' (subsonicId=${album.subsonicId}): ${error?.message}")
                                    Log.w(TAG, "       Error details: ${error?.toString()}")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing album ${album.title}: ${e.message}", e)
                            }
                        }
                    } else {
                        Log.w(TAG, "Failed to fetch artist '${artist.name}': ${artistDetailResult.exceptionOrNull()?.message}")
                    }

                    processedArtists++
                    if (processedArtists % 5 == 0) {
                        val message = "Processing artists: $processedArtists/$totalArtists"
                        setForeground(createForegroundInfo(message))
                        setProgress(workDataOf(
                            PROGRESS_PHASE to 1,
                            PROGRESS_CURRENT to processedArtists,
                            PROGRESS_TOTAL to totalArtists,
                            PROGRESS_MESSAGE to message
                        ))
                        Log.i(TAG, "Progress: $processedArtists/$totalArtists artists processed, ${allSongs.size} songs, ${allAlbumsWithArtists.size} albums collected")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing artist ${artist.name}: ${e.message}", e)
                }
            }

            Log.i(TAG, "Fetched ${allSongs.size} songs and ${allAlbumsWithArtists.size} albums from Navidrome")

            setProgress(workDataOf(
                PROGRESS_PHASE to 2,
                PROGRESS_MESSAGE to "Storing in database..."
            ))
            setForeground(createForegroundInfo("Saving library..."))

            // Convert to database entities
            Log.i(TAG, "Converting ${artists.size} artists to entities...")
            val artistEntities = artists.map { it.toArtistEntity() }
            Log.i(TAG, "Converting ${allAlbumsWithArtists.size} albums to entities...")
            val albumEntities = allAlbumsWithArtists.map { it.toAlbumEntity() }
            Log.i(TAG, "Converted ${artistEntities.size} artists and ${albumEntities.size} albums")

            // Create ID lookup maps to ensure foreign key consistency
            val artistIdMap = artistEntities.associateBy { it.id }
            val albumIdMap = albumEntities.associateBy { it.id }
            Log.i(TAG, "Created lookup maps: ${artistIdMap.size} artists, ${albumIdMap.size} albums")

            // Convert songs, filtering out any with invalid foreign keys
            Log.i(TAG, "Converting ${allSongs.size} songs to entities...")
            val songEntities = allSongs.mapNotNull { song ->
                try {
                    val songEntity = song.toSongEntity()

                    // Verify foreign key references exist
                    if (!albumIdMap.containsKey(songEntity.albumId)) {
                        Log.w(TAG, "Skipping song '${songEntity.title}' - album ID ${songEntity.albumId} not found in ${albumIdMap.size} albums")
                        return@mapNotNull null
                    }
                    if (!artistIdMap.containsKey(songEntity.artistId)) {
                        Log.w(TAG, "Skipping song '${songEntity.title}' - artist ID ${songEntity.artistId} not found in ${artistIdMap.size} artists")
                        return@mapNotNull null
                    }

                    songEntity
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting song '${song.title}' to entity: ${e.message}", e)
                    null
                }
            }
            Log.i(TAG, "Converted ${songEntities.size} songs (filtered from ${allSongs.size})")

            // Build set of valid song IDs for cross-reference validation
            val validSongIds = songEntities.map { it.id }.toSet()
            Log.i(TAG, "Valid song IDs: ${validSongIds.size}")

            // Create cross-references, filtering out invalid artist IDs and song IDs
            Log.i(TAG, "Creating cross-references for ${allSongs.size} songs...")
            val crossRefs = allSongs.flatMap { song ->
                val songId = song.id.toLongOrNull() ?: song.id.hashCode().toLong()

                // Only create cross-refs for songs that passed validation
                if (!validSongIds.contains(songId)) {
                    return@flatMap emptyList()
                }

                song.artists.mapNotNull { artistRef ->
                    if (!artistIdMap.containsKey(artistRef.id)) {
                        Log.w(TAG, "Skipping cross-ref for song '${song.title}' - artist ID ${artistRef.id} not found")
                        return@mapNotNull null
                    }
                    try {
                        SongArtistCrossRef(
                            songId = songId,
                            artistId = artistRef.id
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating cross-ref: ${e.message}", e)
                        null
                    }
                }
            }
            Log.i(TAG, "Created ${crossRefs.size} cross-references")

            Log.i(TAG, "Prepared for database: ${artistEntities.size} artists, ${albumEntities.size} albums, ${songEntities.size} songs, ${crossRefs.size} cross-refs")

            // Clear existing Navidrome data first
            Log.i(TAG, "Clearing existing music data...")
            musicDao.clearAllMusicDataWithCrossRefs()

            // Insert all data in a single transaction to ensure consistency
            Log.i(TAG, "Inserting all data in transaction...")
            musicDao.insertMusicDataWithCrossRefs(
                songs = songEntities,
                albums = albumEntities,
                artists = artistEntities,
                crossRefs = crossRefs
            )
            Log.i(TAG, "Database insertion completed successfully")

            val endTime = System.currentTimeMillis()
            Log.i(TAG, "Navidrome sync completed successfully in ${endTime - startTime}ms. " +
                    "Synced ${songEntities.size} songs, ${albumEntities.size} albums, ${artistEntities.size} artists")

            return@withContext Result.success(workDataOf(
                OUTPUT_TOTAL_SONGS to songEntities.size,
                OUTPUT_TOTAL_ALBUMS to albumEntities.size,
                OUTPUT_TOTAL_ARTISTS to artistEntities.size
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Error during Navidrome sync", e)
            return@withContext Result.failure()
        }
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val id = "navidrome_sync_channel"
        val title = "Navidrome Sync"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, title, NotificationManager.IMPORTANCE_LOW)
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle("Syncing Music Library")
            .setTicker("Syncing Music")
            .setContentText(progress)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .build()

        // Specify foreground service type for Android 10+ (API 29+)
        // Required for Android 14+ (API 34+)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                1001,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(1001, notification)
        }
    }

    private fun Song.toSongEntity(): SongEntity {
        return SongEntity(
            id = this.id.toLongOrNull() ?: this.id.hashCode().toLong(),
            title = this.title,
            artistName = this.artists.firstOrNull()?.name ?: this.artist,
            artistId = this.artists.firstOrNull()?.id ?: this.artistId,
            albumArtist = this.albumArtist,
            albumName = this.album,
            albumId = this.albumId,
            contentUriString = this.contentUriString, // Stream URL
            albumArtUriString = this.albumArtUriString,
            duration = this.duration,
            genre = this.genre,
            filePath = this.path, // Stream URL as path
            parentDirectoryPath = "", // Empty for Navidrome songs
            isFavorite = this.isFavorite,
            lyrics = this.lyrics,
            trackNumber = this.trackNumber,
            year = this.year,
            dateAdded = this.dateAdded,
            mimeType = this.mimeType,
            bitrate = this.bitrate,
            sampleRate = this.sampleRate
        )
    }

    private fun AlbumWithArtist.toAlbumEntity(): AlbumEntity {
        return AlbumEntity(
            id = this.album.id,
            title = this.album.title,
            artistName = this.album.artist,
            artistId = this.artistId, // Use tracked artist ID
            albumArtUriString = this.album.albumArtUriString,
            songCount = this.album.songCount,
            year = this.album.year,
            subsonicId = this.album.subsonicId
        )
    }

    private fun com.theveloper.pixelplay.data.model.Artist.toArtistEntity(): ArtistEntity {
        return ArtistEntity(
            id = this.id,
            name = this.name,
            trackCount = this.songCount,
            imageUrl = this.imageUrl,
            subsonicId = this.subsonicId
        )
    }

    companion object {
        const val WORK_NAME = "com.theveloper.pixelplay.data.worker.NavidromeSyncWorker"
        private const val TAG = "NavidromeSyncWorker"

        const val PROGRESS_PHASE = "progress_phase"
        const val PROGRESS_CURRENT = "progress_current"
        const val PROGRESS_TOTAL = "progress_total"
        const val PROGRESS_MESSAGE = "progress_message"

        const val OUTPUT_TOTAL_SONGS = "output_total_songs"
        const val OUTPUT_TOTAL_ALBUMS = "output_total_albums"
        const val OUTPUT_TOTAL_ARTISTS = "output_total_artists"

        fun navidromeSyncWork() = OneTimeWorkRequestBuilder<NavidromeSyncWorker>()
            .setExpedited(androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
    }
}

