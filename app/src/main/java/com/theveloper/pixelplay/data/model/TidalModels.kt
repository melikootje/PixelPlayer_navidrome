package com.theveloper.pixelplay.data.model

import com.google.gson.annotations.SerializedName

/**
 * Enum representing different music sources in the app
 */
enum class MusicSource {
    LOCAL,      // Local device files
    NAVIDROME,  // Navidrome/Subsonic server
    TIDAL       // Tidal via HiFi API
}

/**
 * Tidal track model from HiFi API
 * Based on: https://github.com/uimaxbai/hifi-api
 */
data class TidalTrack(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("trackNumber") val trackNumber: Int?,
    @SerializedName("volumeNumber") val volumeNumber: Int?,
    @SerializedName("isrc") val isrc: String?,
    @SerializedName("copyright") val copyright: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("audioQuality") val audioQuality: String?,
    @SerializedName("audioModes") val audioModes: List<String>?,
    @SerializedName("artist") val artist: TidalArtist?,
    @SerializedName("artists") val artists: List<TidalArtist>?,
    @SerializedName("album") val album: TidalAlbum?,
    @SerializedName("explicit") val explicit: Boolean?
)

/**
 * Tidal artist model from HiFi API
 * Based on: https://github.com/uimaxbai/hifi-api
 */
data class TidalArtist(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("picture") val picture: String?,
    @SerializedName("url") val url: String?
)

/**
 * Tidal album model from HiFi API
 * Based on: https://github.com/uimaxbai/hifi-api
 */
data class TidalAlbum(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("duration") val duration: Int?,
    @SerializedName("numberOfTracks") val numberOfTracks: Int?,
    @SerializedName("numberOfVolumes") val numberOfVolumes: Int?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("copyright") val copyright: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("cover") val cover: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("explicit") val explicit: Boolean?,
    @SerializedName("audioQuality") val audioQuality: String?,
    @SerializedName("audioModes") val audioModes: List<String>?,
    @SerializedName("artist") val artist: TidalArtist?,
    @SerializedName("artists") val artists: List<TidalArtist>?
)

/**
 * Tidal playlist model from HiFi API
 * Based on: https://github.com/uimaxbai/hifi-api
 */
data class TidalPlaylist(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("duration") val duration: Int?,
    @SerializedName("lastUpdated") val lastUpdated: String?,
    @SerializedName("created") val created: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("publicPlaylist") val publicPlaylist: Boolean?,
    @SerializedName("url") val url: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("squareImage") val squareImage: String?,
    @SerializedName("numberOfTracks") val numberOfTracks: Int?,
    @SerializedName("tracks") val tracks: List<TidalTrack>?
)

/**
 * Tidal search result wrapper from HiFi API
 * Based on: https://github.com/uimaxbai/hifi-api
 */
data class TidalSearchResult(
    @SerializedName("tracks") val tracks: TidalSearchTracks?,
    @SerializedName("albums") val albums: TidalSearchAlbums?,
    @SerializedName("artists") val artists: TidalSearchArtists?
)

data class TidalSearchTracks(
    @SerializedName("items") val items: List<TidalTrack>?,
    @SerializedName("totalNumberOfItems") val totalNumberOfItems: Int?
)

data class TidalSearchAlbums(
    @SerializedName("items") val items: List<TidalAlbum>?,
    @SerializedName("totalNumberOfItems") val totalNumberOfItems: Int?
)

data class TidalSearchArtists(
    @SerializedName("items") val items: List<TidalArtist>?,
    @SerializedName("totalNumberOfItems") val totalNumberOfItems: Int?
)

/**
 * Tidal authentication response from HiFi API
 * Based on: https://github.com/uimaxbai/hifi-api
 */
data class TidalAuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("expiresIn") val expiresIn: Long?,
    @SerializedName("userId") val userId: String?,
    @SerializedName("countryCode") val countryCode: String?
)

/**
 * Tidal stream URL response from HiFi API
 * Based on: https://github.com/uimaxbai/hifi-api
 */
data class TidalStreamResponse(
    @SerializedName("url") val url: String,
    @SerializedName("codec") val codec: String?,
    @SerializedName("encryptionKey") val encryptionKey: String?,
    @SerializedName("bitrate") val bitrate: Int?,
    @SerializedName("sampleRate") val sampleRate: Int?,
    @SerializedName("bitDepth") val bitDepth: Int?
)

/**
 * Tidal login request body
 */
data class TidalLoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

