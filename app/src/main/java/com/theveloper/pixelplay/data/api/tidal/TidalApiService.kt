package com.theveloper.pixelplay.data.api.tidal

import retrofit2.Response
import retrofit2.http.*

/**
 * Tidal API service interface
 * Based on https://github.com/uimaxbai/hifi-api
 */
interface TidalApiService {

    /**
     * Authenticate with Tidal using username and password
     */
    @POST("login/username")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("token") clientToken: String = TIDAL_CLIENT_TOKEN
    ): Response<TidalAuthResponse>

    /**
     * Refresh access token
     */
    @POST("login/token/refresh")
    @FormUrlEncoded
    suspend fun refreshToken(
        @Field("refreshToken") refreshToken: String
    ): Response<TidalAuthResponse>

    /**
     * Search for tracks
     */
    @GET("search/tracks")
    suspend fun searchTracks(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("countryCode") countryCode: String = "US"
    ): Response<TidalSearchResults>

    /**
     * Get track details
     */
    @GET("tracks/{trackId}")
    suspend fun getTrack(
        @Header("Authorization") authorization: String,
        @Path("trackId") trackId: String,
        @Query("countryCode") countryCode: String = "US"
    ): Response<TidalTrack>

    /**
     * Get stream URL for a track
     */
    @GET("tracks/{trackId}/playbackinfopostpaywall")
    suspend fun getStreamUrl(
        @Header("Authorization") authorization: String,
        @Path("trackId") trackId: String,
        @Query("audioquality") audioQuality: String,
        @Query("playbackmode") playbackMode: String = "STREAM",
        @Query("assetpresentation") assetPresentation: String = "FULL"
    ): Response<Map<String, Any>>

    /**
     * Get user playlists
     */
    @GET("users/{userId}/playlists")
    suspend fun getUserPlaylists(
        @Header("Authorization") authorization: String,
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<Map<String, Any>>

    companion object {
        const val BASE_URL = "https://api.tidal.com/v1/"

        // Note: This is a placeholder. Users need to get their own Tidal API token
        // from https://developer.tidal.com/
        const val TIDAL_CLIENT_TOKEN = "TIDAL_CLIENT_TOKEN_PLACEHOLDER"
    }
}

