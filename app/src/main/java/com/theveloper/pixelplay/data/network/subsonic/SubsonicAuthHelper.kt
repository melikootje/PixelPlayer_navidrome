package com.theveloper.pixelplay.data.network.subsonic

import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Helper class for Subsonic authentication
 */
@Singleton
class SubsonicAuthHelper @Inject constructor() {

    /**
     * Generate authentication parameters for Subsonic API
     * @param password User's password
     * @return Pair of (token, salt)
     */
    fun generateAuthParams(password: String): Pair<String, String> {
        val salt = generateSalt()
        val token = generateToken(password, salt)
        return Pair(token, salt)
    }

    /**
     * Generate a random salt string
     */
    private fun generateSalt(length: Int = 12): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    /**
     * Generate authentication token as MD5(password + salt)
     */
    private fun generateToken(password: String, salt: String): String {
        val input = password + salt
        return md5(input)
    }

    /**
     * Calculate MD5 hash
     */
    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Validate server URL format
     */
    fun validateServerUrl(url: String): Boolean {
        if (url.isBlank()) return false
        val trimmed = url.trim()
        return trimmed.startsWith("http://") || trimmed.startsWith("https://")
    }

    /**
     * Normalize server URL (remove trailing slash)
     */
    fun normalizeServerUrl(url: String): String {
        return url.trim().trimEnd('/')
    }
}
