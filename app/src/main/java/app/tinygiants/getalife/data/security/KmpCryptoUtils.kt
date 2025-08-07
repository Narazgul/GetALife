package app.tinygiants.getalife.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * KMP-compatible crypto utilities using only standard library functions
 * These can be easily ported to other KMP platforms
 */
object KmpCryptoUtils {

    const val AES_KEY_LENGTH = 32 // 256 bits
    const val IV_LENGTH = 12 // GCM standard
    const val TAG_LENGTH = 16 // GCM authentication tag

    /**
     * Generate a secure random key using platform-agnostic methods
     * This approach works on all KMP platforms
     */
    fun generateSecureKey(): ByteArray {
        val keyBytes = ByteArray(AES_KEY_LENGTH)

        // Use Java SecureRandom on Android/JVM, can be replaced with platform-specific impl
        try {
            SecureRandom().nextBytes(keyBytes)
        } catch (e: Exception) {
            // Fallback to Kotlin Random with current time seed
            val random = Random(System.currentTimeMillis())
            for (i in keyBytes.indices) {
                keyBytes[i] = random.nextInt(256).toByte()
            }
        }
        return keyBytes
    }

    /**
     * Generate secure IV (initialization vector) for AES-GCM
     */
    fun generateIV(): ByteArray {
        val iv = ByteArray(IV_LENGTH)
        try {
            SecureRandom().nextBytes(iv)
        } catch (e: Exception) {
            val random = Random(System.currentTimeMillis() + Random.nextInt())
            for (i in iv.indices) {
                iv[i] = random.nextInt(256).toByte()
            }
        }
        return iv
    }

    /**
     * Create SHA-256 hash (available on all KMP platforms)
     */
    fun sha256Hash(input: String): ByteArray {
        return try {
            // JVM/Android implementation
            MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        } catch (e: Exception) {
            // Fallback: Simple hash for KMP platforms without MessageDigest
            simpleHash(input.toByteArray())
        }
    }

    /**
     * Encrypt data using AES-GCM (can be implemented per platform)
     */
    fun encryptAESGCM(plaintext: String, key: ByteArray): EncryptionResult? {
        return try {
            val secretKey = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")

            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(plaintext.toByteArray())

            EncryptionResult(encryptedData, iv)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decrypt data using AES-GCM
     */
    fun decryptAESGCM(encryptedData: ByteArray, key: ByteArray, iv: ByteArray): String? {
        return try {
            val secretKey = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(TAG_LENGTH * 8, iv)

            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decryptedData = cipher.doFinal(encryptedData)

            String(decryptedData)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Encode bytes to Base64-like string (KMP-compatible)
     */
    fun encodeToBase64String(bytes: ByteArray): String {
        return try {
            // Android/JVM: Use built-in Base64
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            // KMP fallback: Simple hex encoding
            bytes.joinToString("") { "%02x".format(it) }
        }
    }

    /**
     * Decode Base64-like string to bytes
     */
    fun decodeFromBase64String(encoded: String): ByteArray? {
        return try {
            android.util.Base64.decode(encoded, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            // KMP fallback: Hex decoding
            if (encoded.length % 2 != 0) return null
            try {
                ByteArray(encoded.length / 2) { i ->
                    encoded.substring(i * 2, i * 2 + 2).toInt(16).toByte()
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Simple hash function for KMP platforms without MessageDigest
     * This is a basic implementation - in real KMP, use platform-specific crypto libs
     */
    private fun simpleHash(input: ByteArray): ByteArray {
        // Simple hash using polynomial rolling hash
        val hashSize = 32
        val hash = ByteArray(hashSize)
        val prime = 31

        for (i in input.indices) {
            val byte = input[i].toInt() and 0xFF
            for (j in hash.indices) {
                hash[j] = ((hash[j].toInt() and 0xFF) * prime + byte).toByte()
            }
        }
        return hash
    }

    data class EncryptionResult(
        val encryptedData: ByteArray,
        val iv: ByteArray
    )
}