package com.theveloper.pixelplay.data.network.subsonic

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * Retrofit interface for Subsonic/Navidrome API.
 * Compatible with Subsonic API v1.16.1
 */
interface SubsonicApiService {

    /**
     * Test connectivity with the server
     */
    @GET("rest/ping")
    suspend fun ping(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json"
    ): SubsonicResponse<Unit>

    /**
     * Get all artists organized by ID3 tags
     */
    @GET("rest/getArtists")
    suspend fun getArtists(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json",
        @Query("musicFolderId") musicFolderId: String? = null
    ): SubsonicResponse<ArtistsResult>

    /**
     * Get details for an artist, including albums
     */
    @GET("rest/getArtist")
    suspend fun getArtist(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("id") id: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json"
    ): SubsonicResponse<ArtistResult>

    /**
     * Get details for an album, including songs
     */
    @GET("rest/getAlbum")
    suspend fun getAlbum(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("id") id: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json"
    ): SubsonicResponse<AlbumResult>

    /**
     * Get details for a song
     */
    @GET("rest/getSong")
    suspend fun getSong(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("id") id: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json"
    ): SubsonicResponse<SongResult>

    /**
     * Get random songs
     */
    @GET("rest/getRandomSongs")
    suspend fun getRandomSongs(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("size") size: Int = 50,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json",
        @Query("genre") genre: String? = null,
        @Query("fromYear") fromYear: Int? = null,
        @Query("toYear") toYear: Int? = null
    ): SubsonicResponse<RandomSongsResult>

    /**
     * Get album list
     */
    @GET("rest/getAlbumList2")
    suspend fun getAlbumList2(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("type") type: String,
        @Query("size") size: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json"
    ): SubsonicResponse<AlbumList2Result>

    /**
     * Search for artists, albums, and songs
     */
    @GET("rest/search3")
    suspend fun search3(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("query") query: String,
        @Query("artistCount") artistCount: Int = 20,
        @Query("artistOffset") artistOffset: Int = 0,
        @Query("albumCount") albumCount: Int = 20,
        @Query("albumOffset") albumOffset: Int = 0,
        @Query("songCount") songCount: Int = 20,
        @Query("songOffset") songOffset: Int = 0,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json"
    ): SubsonicResponse<SearchResult3>

    /**
     * Get all genres
     */
    @GET("rest/getGenres")
    suspend fun getGenres(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json"
    ): SubsonicResponse<GenresResult>

    /**
     * Get starred items
     */
    @GET("rest/getStarred2")
    suspend fun getStarred2(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json"
    ): SubsonicResponse<Starred2Result>

    /**
     * Star an item (song, album, or artist)
     */
    @GET("rest/star")
    suspend fun star(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("id") id: String? = null,
        @Query("albumId") albumId: String? = null,
        @Query("artistId") artistId: String? = null,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json"
    ): SubsonicResponse<Unit>

    /**
     * Unstar an item
     */
    @GET("rest/unstar")
    suspend fun unstar(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("id") id: String? = null,
        @Query("albumId") albumId: String? = null,
        @Query("artistId") artistId: String? = null,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json"
    ): SubsonicResponse<Unit>

    /**
     * Scrobble a song (mark as played)
     */
    @GET("rest/scrobble")
    suspend fun scrobble(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("id") id: String,
        @Query("time") time: Long? = null,
        @Query("submission") submission: Boolean = true,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json"
    ): SubsonicResponse<Unit>

    /**
     * Get playlists
     */
    @GET("rest/getPlaylists")
    suspend fun getPlaylists(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json"
    ): SubsonicResponse<PlaylistsResult>

    /**
     * Get a specific playlist with its songs
     */
    @GET("rest/getPlaylist")
    suspend fun getPlaylist(
        @Query("u") username: String,
        @Query("t") token: String,
        @Query("s") salt: String,
        @Query("id") id: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "PixelPlayer",
        @Query("f") format: String = "json"
    ): SubsonicResponse<PlaylistResult>

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
