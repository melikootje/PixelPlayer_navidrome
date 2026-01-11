package com.theveloper.pixelplay.data.model

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class Album(
    val id: Long, // MediaStore.Audio.Albums._ID
    val title: String,
    val artist: String,
    val year: Int,
    val albumArtUriString: String?,
    val songCount: Int,
    val subsonicId: String? = null // Original Subsonic/Navidrome string ID
)

@Immutable
data class Artist(
    val id: Long, // MediaStore.Audio.Artists._ID
    val name: String,
    val songCount: Int,
    val imageUrl: String? = null, // Deezer artist image URL
    val subsonicId: String? = null // Original Subsonic/Navidrome string ID
)

/**
 * Represents a simplified artist reference for multi-artist support.
 * Used when displaying multiple artists for a song.
 */
@Immutable
data class ArtistRef(
    val id: Long,
    val name: String,
    val isPrimary: Boolean = false
)