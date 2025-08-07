package app.tinygiants.getalife.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import app.tinygiants.getalife.BuildConfig
import app.tinygiants.getalife.BuildConfigFields
import app.tinygiants.getalife.BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataEncryption @Inject constructor() {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "GetALifeDataEncryptionKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12 // GCM standard IV length
        private const val TAG_LENGTH = 16 // GCM authentication tag length
    }

    private val platformEncryption: PlatformEncryption by lazy {
        EncryptionFactory.createPlatformEncryption()
    }

    init {
        platformEncryption.initialize()
        // Log security configuration in debug builds
        if (BuildConfig.DEBUG) {
            BuildConfigFields.logSecurityConfig()
        }
    }

    /**
     * Encrypts sensitive data before logging/transmitting
     * @param plainText The sensitive data to encrypt
     * @return Encrypted data as Base64 string, or "[ENCRYPTED]" placeholder
     */
    fun encryptSensitiveData(plainText: String): String {
        if (BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG) {
            // In debug mode with flag enabled, return original data
            // NEVER enable this in production!
            return plainText
        }

        return platformEncryption.encryptSensitiveData(plainText)
    }

    /**
     * Encrypts data for secure logging purposes
     * Returns a hashed version for identification without exposing content
     */
    fun hashForLogging(sensitiveData: String): String {
        if (BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG) {
            // In debug mode, return first few characters for identification
            return if (sensitiveData.length > 10) {
                "${sensitiveData.take(6)}***${sensitiveData.takeLast(2)}"
            } else {
                sensitiveData
            }
        }

        return platformEncryption.hashForLogging(sensitiveData)
    }

    /**
     * Decrypts data (for internal use only)
     */
    fun decryptData(encryptedData: String): String? {
        if (BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG) {
            return encryptedData
        }

        return platformEncryption.decryptData(encryptedData)
    }

    /**
     * Check if encryption is currently disabled (for debug purposes)
     */
    fun isEncryptionDisabled(): Boolean = platformEncryption.isEncryptionDisabled()

    /**
     * Create a consistent hash for the same input
     * Useful for creating stable identifiers that don't expose sensitive data
     */
    fun createStableHash(input: String, salt: String = "GetALife2024"): String {
        val encryption = platformEncryption
        return if (encryption is KmpEncryption) {
            encryption.createConsistentHash(input, salt)
        } else {
            // Fallback for other implementations
            hashForLogging("$salt$input$salt")
        }
    }

    /**
     * Sanitize sensitive data for logging
     * Replaces sensitive content with safe placeholders
     */
    fun sanitizeForLogging(data: String, maxLength: Int = 100): String {
        if (BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG) {
            return if (data.length > maxLength) {
                "${data.take(maxLength)}... [TRUNCATED]"
            } else {
                data
            }
        }

        return when {
            data.isBlank() -> "[EMPTY]"
            data.length <= 6 -> "[SENSITIVE]"
            else -> hashForLogging(data)
        }
    }
}

/**
 * Extension functions for easy encryption of common data types
 */
fun String.encryptForLogging(): String {
    val encryption = EncryptionFactory.createPlatformEncryption()
    encryption.initialize()
    return encryption.hashForLogging(this)
}

fun String.isEncryptionHash(): Boolean {
    return this.startsWith("HASH_") ||
            this.startsWith("ID_") ||
            this.startsWith("[SENSITIVE") ||
            this.startsWith("[ENCRYPTED")
}