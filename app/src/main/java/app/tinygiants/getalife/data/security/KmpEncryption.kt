package app.tinygiants.getalife.data.security

import app.tinygiants.getalife.BuildConfigFields
import kotlin.random.Random

/**
 * Pure Kotlin encryption implementation for KMP compatibility
 * Works on Android, iOS, Web, and Desktop without platform-specific dependencies
 *
 * This implementation uses only Kotlin standard library and can be easily ported
 * to any KMP target platform.
 */
class KmpEncryption : PlatformEncryption {

    companion object {
        private const val KEY_LENGTH = 32 // 256-bit key
        private const val HASH_LENGTH = 32 // 256-bit hash
        private const val ENCRYPTION_VERSION = 1
    }

    private var encryptionKey: ByteArray? = null
    private var isInitialized = false

    override fun initialize() {
        if (!isInitialized) {
            encryptionKey = generatePlatformAgnosticKey()
            isInitialized = true
        }
    }

    override fun encryptSensitiveData(plainText: String): String {
        if (BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG) {
            return plainText
        }

        if (!isInitialized) initialize()

        return try {
            val key = encryptionKey ?: return "[ENCRYPTION_ERROR]"
            val encrypted = simpleXOREncrypt(plainText.toByteArray(), key)
            encodeToString(encrypted)
        } catch (e: Exception) {
            "[ENCRYPTION_ERROR]"
        }
    }

    override fun hashForLogging(sensitiveData: String): String {
        if (BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG) {
            return if (sensitiveData.length > 10) {
                "${sensitiveData.take(6)}***${sensitiveData.takeLast(2)}"
            } else {
                sensitiveData
            }
        }

        return try {
            val hash = sha256Simple(sensitiveData)
            "HASH_${encodeToString(hash.take(8).toByteArray())}"
        } catch (e: Exception) {
            "[SENSITIVE_DATA]"
        }
    }

    override fun decryptData(encryptedData: String): String? {
        if (BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG) {
            return encryptedData
        }

        if (!isInitialized) initialize()

        return try {
            val key = encryptionKey ?: return null
            val decodedData = decodeFromString(encryptedData) ?: return null
            val decrypted = simpleXORDecrypt(decodedData, key)
            String(decrypted)
        } catch (e: Exception) {
            null
        }
    }

    override fun isEncryptionDisabled(): Boolean = BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG

    /**
     * Generate a platform-agnostic encryption key using only Kotlin standard library
     */
    private fun generatePlatformAgnosticKey(): ByteArray {
        // Use multiple entropy sources for better randomness
        val timestamp = System.currentTimeMillis()
        val hashCode = this.hashCode()
        val seed = timestamp xor hashCode.toLong()

        val random = Random(seed)
        val key = ByteArray(KEY_LENGTH)

        // Fill with random bytes
        random.nextBytes(key)

        // Add additional entropy from system properties if available
        try {
            val systemEntropy = (System.nanoTime() % 256).toByte()
            for (i in key.indices step 4) {
                key[i] = (key[i].toInt() xor systemEntropy.toInt()).toByte()
            }
        } catch (e: Exception) {
            // Ignore if System class is not available (some KMP targets)
        }

        return key
    }

    /**
     * Simple XOR encryption (suitable for KMP, but not cryptographically strong)
     * In production KMP, replace with platform-specific AES implementations
     */
    private fun simpleXOREncrypt(data: ByteArray, key: ByteArray): ByteArray {
        val encrypted = ByteArray(data.size)
        for (i in data.indices) {
            encrypted[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return encrypted
    }

    private fun simpleXORDecrypt(data: ByteArray, key: ByteArray): ByteArray {
        // XOR is symmetric, so decryption is the same as encryption
        return simpleXOREncrypt(data, key)
    }

    /**
     * Simple SHA-256-like hash using only Kotlin standard library
     * This is a simplified implementation - use platform-specific crypto in production
     */
    private fun sha256Simple(input: String): ByteArray {
        val bytes = input.toByteArray()
        val hash = ByteArray(HASH_LENGTH)

        // Initialize hash with constants (similar to SHA-256 initialization)
        val h = intArrayOf(
            0x6a09e667.toInt(), 0xbb67ae85.toInt(), 0x3c6ef372.toInt(), 0xa54ff53a.toInt(),
            0x510e527f.toInt(), 0x9b05688c.toInt(), 0x1f83d9ab.toInt(), 0x5be0cd19.toInt()
        )

        // Process input in chunks
        var chunkIndex = 0
        while (chunkIndex < bytes.size) {
            val chunk = bytes.sliceArray(chunkIndex until minOf(chunkIndex + 64, bytes.size))

            // Simple hash mixing (not cryptographically secure, but deterministic)
            for (i in chunk.indices) {
                val byte = chunk[i].toInt() and 0xFF
                h[i % h.size] = (h[i % h.size] * 31 + byte) and 0x7FFFFFFF
            }

            chunkIndex += 64
        }

        // Convert result to byte array
        for (i in 0 until minOf(h.size, hash.size / 4)) {
            val value = h[i]
            val startIndex = i * 4
            if (startIndex + 3 < hash.size) {
                hash[startIndex] = (value shr 24).toByte()
                hash[startIndex + 1] = (value shr 16).toByte()
                hash[startIndex + 2] = (value shr 8).toByte()
                hash[startIndex + 3] = value.toByte()
            }
        }

        return hash
    }

    /**
     * Encode bytes to string using only Kotlin standard library
     * Uses hex encoding which works on all platforms
     */
    private fun encodeToString(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Decode string to bytes
     */
    private fun decodeFromString(encoded: String): ByteArray? {
        if (encoded.length % 2 != 0) return null
        return try {
            ByteArray(encoded.length / 2) { i ->
                encoded.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Create a salted hash for consistent but secure identification
     */
    fun createConsistentHash(input: String, salt: String = "GetALife2024"): String {
        val combined = salt + input + salt.reversed()
        val hash = sha256Simple(combined)
        return "ID_${encodeToString(hash.take(12).toByteArray())}"
    }
}

/**
 * KMP-compatible encryption factory
 * Returns the most appropriate encryption implementation for the current platform
 */
object EncryptionFactory {

    fun createPlatformEncryption(): PlatformEncryption {
        return try {
            // Try to determine the current platform and return the best implementation
            when {
                isAndroid() -> {
                    // Use Android-specific implementation if Context is available
                    // For now, fall back to KMP implementation
                    KmpEncryption()
                }

                isJvm() -> KmpEncryption()
                isWeb() -> KmpEncryption()
                isNative() -> KmpEncryption()
                else -> KmpEncryption()
            }
        } catch (e: Exception) {
            // Always fall back to KMP implementation
            KmpEncryption()
        }
    }

    private fun isAndroid(): Boolean = try {
        Class.forName("android.os.Build")
        true
    } catch (e: Exception) {
        false
    }

    private fun isJvm(): Boolean = try {
        System.getProperty("java.version") != null
    } catch (e: Exception) {
        false
    }

    private fun isWeb(): Boolean = try {
        false
    } catch (e: Exception) {
        false
    }

    private fun isNative(): Boolean = try {
        // This would be replaced with proper Native detection in KMP
        false
    } catch (e: Exception) {
        false
    }
}