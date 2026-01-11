package com.theveloper.pixelplay.data.network.subsonic

/**
 * Helper class for building Subsonic API URLs
 */
object SubsonicUrlHelper {

    /**
     * Get cover art for a song, album, or artist
     * Returns the URL - actual streaming handled separately
     */
    fun getCoverArtUrl(
        baseUrl: String,
        username: String,
        token: String,
        salt: String,
        id: String,
        size: Int? = null
    ): String {
        var url = "$baseUrl/rest/getCoverArt?u=$username&t=$token&s=$salt&v=1.16.1&c=PixelPlayer&id=$id"
        if (size != null) {
            url += "&size=$size"
        }
        return url
    }

    /**
     * Get streaming URL for a song
     */
    fun getStreamUrl(
        baseUrl: String,
        username: String,
        token: String,
        salt: String,
        id: String,
        maxBitRate: Int? = null,
        format: String? = null
    ): String {
        var url = "$baseUrl/rest/stream?u=$username&t=$token&s=$salt&v=1.16.1&c=PixelPlayer&id=$id"
        if (maxBitRate != null) {
            url += "&maxBitRate=$maxBitRate"
        }
        if (format != null) {
            url += "&format=$format"
        }
        return url
    }
}

