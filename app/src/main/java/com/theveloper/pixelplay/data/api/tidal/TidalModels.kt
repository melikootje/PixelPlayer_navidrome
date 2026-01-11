package com.theveloper.pixelplay.data.api.tidal

import com.google.gson.annotations.SerializedName

/**
 * Tidal API response models
 */
data class TidalAuthResponse(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("sessionId")
    val sessionId: String,
    @SerializedName("countryCode")
    val countryCode: String,
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String?,
    @SerializedName("expiresIn")
    val expiresIn: Long
)

data class TidalSearchResults(
    @SerializedName("items")
    val items: List<TidalTrack>,
    @SerializedName("totalNumberOfItems")
    val totalNumberOfItems: Int
)

data class TidalTrack(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("duration")
    val duration: Int,
    @SerializedName("trackNumber")
    val trackNumber: Int?,
    @SerializedName("volumeNumber")
    val volumeNumber: Int?,
    @SerializedName("isrc")
    val isrc: String?,
    @SerializedName("explicit")
    val explicit: Boolean?,
    @SerializedName("audioQuality")
    val audioQuality: String?,
    @SerializedName("album")
    val album: TidalAlbum?,
    @SerializedName("artist")
    val artist: TidalArtist?,
    @SerializedName("artists")
    val artists: List<TidalArtist>?
)

data class TidalAlbum(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("duration")
    val duration: Int?,
    @SerializedName("numberOfTracks")
    val numberOfTracks: Int?,
    @SerializedName("releaseDate")
    val releaseDate: String?,
    @SerializedName("cover")
    val cover: String?,
    @SerializedName("artist")
    val artist: TidalArtist?,
    @SerializedName("explicit")
    val explicit: Boolean?
)

data class TidalArtist(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("picture")
    val picture: String?
)

data class TidalStreamInfo(
    @SerializedName("manifest")
    val manifest: String?,
    @SerializedName("manifestMimeType")
    val manifestMimeType: String?,
    @SerializedName("audioQuality")
    val audioQuality: String?,
    @SerializedName("trackId")
    val trackId: String?
)

