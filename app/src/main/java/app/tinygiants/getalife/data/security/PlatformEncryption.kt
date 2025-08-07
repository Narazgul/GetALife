package app.tinygiants.getalife.data.security

/**
 * Platform-agnostic encryption interface for KMP compatibility
 * Supports Android, iOS, Web, and Desktop platforms
 *
 * This interface can be easily converted to expect/actual when migrating to KMP
 */
interface PlatformEncryption {
    /**
     * Encrypts sensitive data using platform-specific secure storage
     * @param plainText The sensitive data to encrypt
     * @return Encrypted data as string, or fallback placeholder
     */
    fun encryptSensitiveData(plainText: String): String

    /**
     * Creates a hash for logging purposes that doesn't expose sensitive content
     * @param sensitiveData The data to hash for logging
     * @return Hashed identifier safe for logging
     */
    fun hashForLogging(sensitiveData: String): String

    /**
     * Decrypts data (for internal use only)
     * @param encryptedData The encrypted data to decrypt
     * @return Decrypted string or null if decryption fails
     */
    fun decryptData(encryptedData: String): String?

    /**
     * Check if encryption is currently disabled for debugging
     * @return true if encryption is disabled (debug mode only)
     */
    fun isEncryptionDisabled(): Boolean

    /**
     * Initialize the platform-specific encryption system
     * Called once during app startup
     */
    fun initialize()
}