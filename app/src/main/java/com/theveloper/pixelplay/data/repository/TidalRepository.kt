package com.theveloper.pixelplay.data.repository

import android.util.Log
import com.theveloper.pixelplay.data.api.tidal.TidalApiService
import com.theveloper.pixelplay.data.api.tidal.TidalAuthResponse
import com.theveloper.pixelplay.data.api.tidal.TidalSearchResults
import com.theveloper.pixelplay.data.api.tidal.TidalTrack
import com.theveloper.pixelplay.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Tidal HiFi API integration
 */
@Singleton
class TidalRepository @Inject constructor(
    private val tidalApiService: TidalApiService,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private var cachedAccessToken: String? = null
    private var tokenExpiryTime: Long = 0

    companion object {
        private const val TAG = "TidalRepository"
    }

    /**
     * Test connection to Tidal
     */
    suspend fun testConnection(): Result<Boolean> {
        return try {
            val username = userPreferencesRepository.tidalUsernameFlow.first()
            val password = userPreferencesRepository.tidalPasswordFlow.first()

            if (username.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Tidal credentials not configured"))
            }

            val response = tidalApiService.login(username, password)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                cachedAccessToken = authResponse.accessToken
                tokenExpiryTime = System.currentTimeMillis() + (authResponse.expiresIn * 1000)
                Result.success(true)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            Result.failure(e)
        }
    }

    /**
     * Get valid access token, refreshing if necessary
     */
    private suspend fun getAccessToken(): String? {
        // Check if we have a valid cached token
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return cachedAccessToken
        }

        // Try to log in
        val username = userPreferencesRepository.tidalUsernameFlow.first()
        val password = userPreferencesRepository.tidalPasswordFlow.first()

        if (username.isBlank() || password.isBlank()) {
            return null
        }

        try {
            val response = tidalApiService.login(username, password)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                cachedAccessToken = authResponse.accessToken
                tokenExpiryTime = System.currentTimeMillis() + (authResponse.expiresIn * 1000)
                return cachedAccessToken
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get access token", e)
        }

        return null
    }

    /**
     * Search for tracks
     */
    suspend fun searchTracks(query: String, limit: Int = 50): Result<List<TidalTrack>> {
        return try {
            val token = getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = tidalApiService.searchTracks("Bearer $token", query, limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.items)
            } else {
                Result.failure(Exception("Search failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search failed", e)
            Result.failure(e)
        }
    }

    /**
     * Get track details
     */
    suspend fun getTrack(trackId: String): Result<TidalTrack> {
        return try {
            val token = getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = tidalApiService.getTrack("Bearer $token", trackId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get track: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get track", e)
            Result.failure(e)
        }
    }

    /**
     * Get stream URL for a track
     * @param audioQuality Options: LOW, HIGH, LOSSLESS, HI_RES
     */
    suspend fun getStreamUrl(trackId: String, audioQuality: String = "HI_RES"): Result<String> {
        return try {
            val token = getAccessToken()
                ?: return Result.failure(Exception("Not authenticated"))

            val response = tidalApiService.getStreamUrl("Bearer $token", trackId, audioQuality)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // Extract manifest/stream URL from response
                val manifest = body["manifest"] as? String
                    ?: return Result.failure(Exception("No stream URL in response"))
                Result.success(manifest)
            } else {
                Result.failure(Exception("Failed to get stream URL: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get stream URL", e)
            Result.failure(e)
        }
    }
}

