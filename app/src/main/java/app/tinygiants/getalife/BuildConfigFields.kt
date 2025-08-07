package app.tinygiants.getalife

/**
 * Centralized configuration for build-specific features
 *
 * SECURITY WARNING:
 * - ENCRYPTION_DISABLED should NEVER be true in production
 * - This flag is only for development and debugging purposes
 * - All sensitive data should be encrypted before logging/transmitting
 */
object BuildConfigFields {

    /**
     * Controls whether sensitive data encryption is disabled for debugging
     *
     * When enabled (true):
     * - Sensitive data will be logged in plain text
     * - Firebase Crashlytics will receive unencrypted data
     * - ONLY USE FOR LOCAL DEVELOPMENT
     *
     * When disabled (false) - PRODUCTION DEFAULT:
     * - All sensitive data is encrypted before logging
     * - Firebase Crashlytics receives hashed/encrypted data only
     * - User privacy is protected
     */
    const val ENCRYPTION_DISABLED_FOR_DEBUG = false

    /**
     * Additional debug flags for development
     */
    val VERBOSE_AI_LOGGING = BuildConfig.DEBUG && false
    val MOCK_SUBSCRIPTION_STATUS = BuildConfig.DEBUG && false
    val BYPASS_ONBOARDING = BuildConfig.DEBUG && false

    /**
     * Security audit helper - logs current encryption status
     */
    fun logSecurityConfig() {
        if (BuildConfig.DEBUG) {
            println("🔐 SECURITY CONFIG:")
            println("   ├─ Build Type: ${if (BuildConfig.DEBUG) "DEBUG" else "RELEASE"}")
            println("   ├─ Encryption Disabled: $ENCRYPTION_DISABLED_FOR_DEBUG")
            println("   ├─ Verbose AI Logging: $VERBOSE_AI_LOGGING")
            println("   └─ ${if (ENCRYPTION_DISABLED_FOR_DEBUG) "⚠️  WARNING: Sensitive data not encrypted!" else "✅ Sensitive data encrypted"}")
        }
    }
}