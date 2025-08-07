# 🔐 Security & Data Encryption Guide

## Overview

This app implements **AES-256-GCM encryption** to protect sensitive user data before logging or transmitting to
third-party services like Firebase Crashlytics.

## 🚨 CRITICAL SECURITY RULES

### ⚠️ NEVER Enable Debug Encryption in Production

- `ENCRYPTION_DISABLED_FOR_DEBUG` must **ALWAYS** be `false` in production builds
- This flag exists **ONLY** for local development and debugging
- Enabling this in production would expose sensitive user data

### 🛡️ What Data is Protected

- User financial information (tags, prompts)
- AI generation requests and responses
- Error context containing sensitive data
- Any data logged to Firebase Crashlytics

## 📋 How It Works

### Production Mode (Default)

```kotlin
BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG = false
```

- ✅ All sensitive data is **encrypted** before logging
- ✅ Firebase Crashlytics receives **hashed identifiers** only
- ✅ User privacy is **fully protected**
- ✅ **AES-256-GCM encryption** with Android Keystore

### Debug Mode (Development Only)

```kotlin
BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG = true
```

- ⚠️ Sensitive data logged in **partial plain text** for debugging
- ⚠️ **ONLY** use for local development
- ⚠️ **NEVER** commit this change to repository
- ⚠️ **NEVER** use in any distributed builds

## 🔧 Usage Examples

### For Debugging (Local Development Only)

```kotlin
// 1. Temporarily enable debug mode
BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG = true

// 2. Test your feature with readable logs
// 3. IMMEDIATELY revert to false
BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG = false

// 4. Commit only with encryption enabled
```

### Normal Production Usage

```kotlin
// DataEncryption is injected automatically
class SomeService @Inject constructor(
    private val dataEncryption: DataEncryption
) {
    fun logSensitiveData(userInput: String) {
        // This will be encrypted in production, readable in debug mode
        val safeForLogging = dataEncryption.hashForLogging(userInput)
        Log.d("Service", "Processing: $safeForLogging")
    }
}
```

## 🔐 Encryption Technical Details

### Algorithm

- **AES-256-GCM** (Galois/Counter Mode)
- **256-bit key** stored in Android Keystore
- **12-byte IV** (initialization vector)
- **16-byte authentication tag**

### Key Management

- Keys stored in **Android Hardware Keystore** when available
- **No user authentication required** for logging keys
- Keys are **device-specific** and cannot be extracted
- **Automatic key generation** on first use

### Data Flow

```
Sensitive Data → Hash/Encrypt → Safe for Logging → Firebase Crashlytics
```

## 🚨 Security Audit Checklist

Before any release, verify:

- [ ] `ENCRYPTION_DISABLED_FOR_DEBUG = false` in `BuildConfigFields.kt`
- [ ] No debug logs containing sensitive data in release builds
- [ ] Firebase Crashlytics receiving only encrypted/hashed data
- [ ] Security config logging shows "✅ Sensitive data encrypted"

## 🔍 Monitoring

### Debug Logs

In debug builds, the app will log security configuration:

```
🔐 SECURITY CONFIG:
   ├─ Build Type: DEBUG
   ├─ Encryption Disabled: false
   ├─ Verbose AI Logging: false
   └─ ✅ Sensitive data encrypted
```

### Firebase Crashlytics

Production crashes will show:

- `ai_tag_hash`: `HASH_XYZ123...` (instead of raw user data)
- `ai_prompt_hash`: `HASH_ABC789...` (instead of raw prompts)
- `encryption_disabled`: `false`

## 📞 Emergency Procedures

### If Sensitive Data is Accidentally Exposed

1. **Immediately disable** the affected logging
2. **Revoke and regenerate** any API keys if exposed
3. **Contact Firebase** to request data deletion if necessary
4. **Review and audit** all logging statements
5. **Update security measures** to prevent recurrence

## 👥 Developer Guidelines

### Code Reviews

- ✅ Always check `BuildConfigFields.ENCRYPTION_DISABLED_FOR_DEBUG` value
- ✅ Verify all sensitive data uses `dataEncryption.hashForLogging()`
- ✅ Ensure no raw user data in log statements
- ✅ Test that Firebase receives only encrypted data

### Testing

- Test with encryption **enabled** (production mode)
- Test with encryption **disabled** (debug mode) for troubleshooting
- Always **revert to encrypted mode** before committing
- Verify Firebase Crashlytics data privacy

## 🚀 Quick Setup

1. **Inject DataEncryption** in your service:
   ```kotlin
   @Inject constructor(private val dataEncryption: DataEncryption)
   ```

2. **Use for sensitive logging**:
   ```kotlin
   val safeData = dataEncryption.hashForLogging(sensitiveUserInput)
   crashlytics.log("Processing: $safeData")
   ```

3. **Verify encryption is working**:
   ```kotlin
   if (dataEncryption.isEncryptionDisabled()) {
       Log.w("Security", "⚠️ Encryption is disabled!")
   }
   ```

Remember: **User privacy and data security are paramount!** 🛡️