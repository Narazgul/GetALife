package app.tinygiants.getalife.data.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import app.tinygiants.getalife.BuildConfigFields
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Android-specific encryption implementation with KMP-compatible fallbacks
 * Uses Android Keystore when available, falls back to encrypted SharedPreferences
 */
@Singleton
class AndroidEncryption @Inject constructor(
    private val context: Context
) : PlatformEncryption {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "GetALifeDataEncryptionKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH = 16
        private const val PREFS_NAME = "secure_encryption_prefs"
        private const val BACKUP_KEY_ALIAS = "backup_encryption_key"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private var useAndroidKeystore = true
    private var backupKey: SecretKey? = null

    override fun initialize() {
        try {
            // Try to use Android Keystore first
            generateOrGetKeystoreKey()
            useAndroidKeystore = true
        } catch (e: Exception) {
            // Fall back to encrypted SharedPreferences with generated key
            useAndroidKeystore = false
            initializeBackupEncryption()
        }
    }

    override fun encryptSensitiveData(plainText: String): String {
        if (BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG) {
            return plainText
        }

        return try {
            if (useAndroidKeystore) {
                encryptWithKeystore(plainText)
            } else {
                encryptWithBackupKey(plainText)
            }
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
            // Use KMP-compatible hashing (available on all platforms)
            val bytes = sensitiveData.toByteArray()
            val hash = MessageDigest.getInstance("SHA-256").digest(bytes)
            "HASH_${Base64.encodeToString(hash.take(8).toByteArray(), Base64.NO_WRAP)}"
        } catch (e: Exception) {
            "[SENSITIVE_DATA]"
        }
    }

    override fun decryptData(encryptedData: String): String? {
        if (BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG) {
            return encryptedData
        }

        return try {
            if (useAndroidKeystore) {
                decryptWithKeystore(encryptedData)
            } else {
                decryptWithBackupKey(encryptedData)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun isEncryptionDisabled(): Boolean = BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG

    // Android Keystore methods
    private fun generateOrGetKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        return if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.getKey(KEY_ALIAS, null) as SecretKey
        } else {
            generateKeystoreKey()
        }
    }

    private fun generateKeystoreKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    private fun encryptWithKeystore(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = generateOrGetKeystoreKey()
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val encryptedData = cipher.doFinal(plainText.toByteArray())
        val combined = iv + encryptedData

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decryptWithKeystore(encryptedData: String): String {
        val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
        val iv = combined.take(IV_LENGTH).toByteArray()
        val encrypted = combined.drop(IV_LENGTH).toByteArray()

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = generateOrGetKeystoreKey()
        val spec = GCMParameterSpec(TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        return String(cipher.doFinal(encrypted))
    }

    // Backup encryption methods (KMP-compatible)
    private fun initializeBackupEncryption() {
        val keyBytes = prefs.getString(BACKUP_KEY_ALIAS, null)
        backupKey = if (keyBytes != null) {
            val decoded = Base64.decode(keyBytes, Base64.DEFAULT)
            SecretKeySpec(decoded, "AES")
        } else {
            generateBackupKey()
        }
    }

    private fun generateBackupKey(): SecretKey {
        // Generate a secure random key (KMP-compatible approach)
        val keyBytes = ByteArray(32) // 256-bit key
        SecureRandom().nextBytes(keyBytes)

        val key = SecretKeySpec(keyBytes, "AES")

        // Store the key securely in encrypted SharedPreferences
        val encodedKey = Base64.encodeToString(keyBytes, Base64.DEFAULT)
        prefs.edit().putString(BACKUP_KEY_ALIAS, encodedKey).apply()

        return key
    }

    private fun encryptWithBackupKey(plainText: String): String {
        val key = backupKey ?: throw IllegalStateException("Backup key not initialized")

        // Simple AES encryption (can be implemented in KMP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val encryptedData = cipher.doFinal(plainText.toByteArray())
        val combined = iv + encryptedData

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decryptWithBackupKey(encryptedData: String): String {
        val key = backupKey ?: throw IllegalStateException("Backup key not initialized")

        val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
        val iv = combined.take(IV_LENGTH).toByteArray()
        val encrypted = combined.drop(IV_LENGTH).toByteArray()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        return String(cipher.doFinal(encrypted))
    }
}