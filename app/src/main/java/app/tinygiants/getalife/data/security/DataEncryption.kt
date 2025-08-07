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

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    init {
        generateOrGetKey()
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

        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

            val iv = cipher.iv
            val encryptedData = cipher.doFinal(plainText.toByteArray())

            // Combine IV + encrypted data
            val combined = iv + encryptedData
            android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            // If encryption fails, don't expose raw data
            "[ENCRYPTION_ERROR]"
        }
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

        return try {
            val hash = java.security.MessageDigest.getInstance("SHA-256")
                .digest(sensitiveData.toByteArray())
            "HASH_${android.util.Base64.encodeToString(hash.take(8).toByteArray(), android.util.Base64.NO_WRAP)}"
        } catch (e: Exception) {
            "[SENSITIVE_DATA]"
        }
    }

    /**
     * Decrypts data (for internal use only)
     */
    fun decryptData(encryptedData: String): String? {
        if (BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG) {
            return encryptedData
        }

        return try {
            val combined = android.util.Base64.decode(encryptedData, android.util.Base64.NO_WRAP)
            val iv = combined.take(IV_LENGTH).toByteArray()
            val encrypted = combined.drop(IV_LENGTH).toByteArray()

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            String(cipher.doFinal(encrypted))
        } catch (e: Exception) {
            null
        }
    }

    private fun generateOrGetKey(): SecretKey {
        return if (keyStore.containsAlias(KEY_ALIAS)) {
            getSecretKey()
        } else {
            generateNewKey()
        }
    }

    private fun generateNewKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // No biometric for logging
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    /**
     * Check if encryption is currently disabled (for debug purposes)
     */
    fun isEncryptionDisabled(): Boolean = ENCRYPTION_DISABLED_FOR_DEBUG
}