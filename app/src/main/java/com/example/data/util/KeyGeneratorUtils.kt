package com.example.data.util

import java.security.SecureRandom
import kotlin.random.asKotlinRandom

object KeyGeneratorUtils {
    private val secureRandom = SecureRandom()

    private const val ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01233456789"
    private const val HEX = "0123456789abcdef"
    
    // Alphanumeric avoiding confusing characters (0, O, I, 1, l) for high developer ergonomics
    private const val BASE58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"

    /**
     * Generates a cryptographically secure random string of specified length and character profile.
     */
    fun generateSecureRandomString(length: Int, format: String): String {
        val chars = when (format.lowercase()) {
            "hex" -> HEX
            "base58" -> BASE58
            else -> ALPHANUMERIC
        }

        if (format.lowercase() == "base64") {
            val bytes = ByteArray(length)
            secureRandom.nextBytes(bytes)
            // Perform URL-Safe Base64 encoding without padding or line breaks
            return android.util.Base64.encodeToString(bytes, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING)
                .take(length)
        }

        val builder = StringBuilder(length)
        for (i in 0 until length) {
            val randomIndex = secureRandom.nextInt(chars.length)
            builder.append(chars[randomIndex])
        }
        return builder.toString()
    }

    /**
     * Helper to assemble a complete standard client key with environment visual namespace prefix.
     */
    fun assembleKey(prefix: String, length: Int, format: String): String {
        val cleanPrefix = if (prefix.endsWith("_")) prefix else "${prefix}_"
        val entropyLength = (length - cleanPrefix.length).coerceAtLeast(8)
        val secureSegment = generateSecureRandomString(entropyLength, format)
        return "$cleanPrefix$secureSegment"
    }
}
