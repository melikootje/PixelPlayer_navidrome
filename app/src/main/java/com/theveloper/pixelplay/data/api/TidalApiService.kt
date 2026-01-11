package com.theveloper.pixelplay.data.api

import com.theveloper.pixelplay.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit interface for Tidal HiFi API
 * API Documentation: https://github.com/uimaxbai/hifi-api
 *
 * Base URL should be configured in settings (e.g., http://localhost:3000 or your deployed server)
 * All requests require authentication via Bearer token in Authorization header
 */
interface TidalApiService {

    /**
     * Authenticate with Tidal using username and password
     * POST /auth/login
     *
     * Request body: { "username": "your-email", "password": "your-password" }
     * Response: { "accessToken": "...", "refreshToken": "...", "userId": "...", ... }
     */
    @POST("/auth/login")
    suspend fun login(
        @Body request: TidalLoginRequest
    ): Response<TidalAuthResponse>

    /**
     * Get track details by ID
     * GET /tracks/{id}
     * Requires: Bearer token
     */
    @GET("/tracks/{id}")
    suspend fun getTrack(
        @Path("id") trackId: Long,
        @Header("Authorization") authorization: String
    ): Response<TidalTrack>

    /**
     * Get album details by ID
     * GET /albums/{id}
     * Requires: Bearer token
     */
    @GET("/albums/{id}")
    suspend fun getAlbum(
        @Path("id") albumId: Long,
        @Header("Authorization") authorization: String
    ): Response<TidalAlbum>

    /**
     * Get all tracks in an album
     * GET /albums/{id}/tracks
     * Requires: Bearer token
     */
    @GET("/albums/{id}/tracks")
    suspend fun getAlbumTracks(
        @Path("id") albumId: Long,
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<List<TidalTrack>>

    /**
     * Get artist details by ID
     * GET /artists/{id}
     * Requires: Bearer token
     */
    @GET("/artists/{id}")
    suspend fun getArtist(
        @Path("id") artistId: Long,
        @Header("Authorization") authorization: String
    ): Response<TidalArtist>

    /**
     * Get all albums by an artist
     * GET /artists/{id}/albums
     * Requires: Bearer token
     */
    @GET("/artists/{id}/albums")
    suspend fun getArtistAlbums(
        @Path("id") artistId: Long,
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<TidalSearchAlbums>

    /**
     * Get user's playlists
     * GET /playlists
     * Requires: Bearer token
     */
    @GET("/playlists")
    suspend fun getPlaylists(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<List<TidalPlaylist>>

    /**
     * Get playlist details by UUID
     * GET /playlists/{uuid}
     * Requires: Bearer token
     */
    @GET("/playlists/{uuid}")
    suspend fun getPlaylist(
        @Path("uuid") playlistUuid: String,
        @Header("Authorization") authorization: String
    ): Response<TidalPlaylist>

    /**
     * Search for tracks, albums, and artists
     * GET /search
     * Requires: Bearer token
     *
     * @param query Search query string
     * @param type Comma-separated list: "tracks", "albums", "artists", or "tracks,albums,artists"
     * @param limit Number of results per type (default 50)
     */
    @GET("/search")
    suspend fun search(
        @Query("query") query: String,
        @Header("Authorization") authorization: String,
        @Query("type") type: String = "tracks,albums,artists",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<TidalSearchResult>

    /**
     * Get stream URL for a track
     * GET /tracks/{id}/stream
     * Requires: Bearer token
     *
     * @param trackId Tidal track ID
     * @param quality Audio quality: "LOW" (96kbps), "HIGH" (320kbps), "LOSSLESS" (16-bit/44.1kHz), "HI_RES" (24-bit/96kHz)
     */
    @GET("/tracks/{id}/stream")
    suspend fun getStreamUrl(
        @Path("id") trackId: Long,
        @Header("Authorization") authorization: String,
        @Query("quality") quality: String = "HIGH"
    ): Response<TidalStreamResponse>

    /**
     * Get user's favorite tracks
     * GET /favorites/tracks
     * Requires: Bearer token
     */
    @GET("/favorites/tracks")
    suspend fun getFavoriteTracks(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<TidalSearchTracks>

    /**
     * Get user's favorite albums
     * GET /favorites/albums
     * Requires: Bearer token
     */
    @GET("/favorites/albums")
    suspend fun getFavoriteAlbums(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<TidalSearchAlbums>

    /**
     * Get user's favorite artists
     * GET /favorites/artists
     * Requires: Bearer token
     */
    @GET("/favorites/artists")
    suspend fun getFavoriteArtists(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<TidalSearchArtists>

    /**
     * Add track to favorites
     * POST /favorites/tracks/{id}
     * Requires: Bearer token
     */
    @POST("/favorites/tracks/{id}")
    suspend fun addFavoriteTrack(
        @Path("id") trackId: Long,
        @Header("Authorization") authorization: String
    ): Response<Unit>

    /**
     * Remove track from favorites
     * DELETE /favorites/tracks/{id}
     * Requires: Bearer token
     */
    @DELETE("/favorites/tracks/{id}")
    suspend fun removeFavoriteTrack(
        @Path("id") trackId: Long,
        @Header("Authorization") authorization: String
    ): Response<Unit>

    /**
     * Add album to favorites
     * POST /favorites/albums/{id}
     * Requires: Bearer token
     */
    @POST("/favorites/albums/{id}")
    suspend fun addFavoriteAlbum(
        @Path("id") albumId: Long,
        @Header("Authorization") authorization: String
    ): Response<Unit>

    /**
     * Remove album from favorites
     * DELETE /favorites/albums/{id}
     * Requires: Bearer token
     */
    @DELETE("/favorites/albums/{id}")
    suspend fun removeFavoriteAlbum(
        @Path("id") albumId: Long,
        @Header("Authorization") authorization: String
    ): Response<Unit>
}

