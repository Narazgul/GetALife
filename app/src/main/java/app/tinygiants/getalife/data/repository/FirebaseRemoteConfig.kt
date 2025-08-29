package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.domain.repository.RemoteConfigRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRemoteConfigRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) : RemoteConfigRepository {

    init {
        remoteConfig.initializeDefaults()
        remoteConfig.fetchAndActivate()
    }

    override fun getFeatureFlagValue(flagName: String): Flow<Boolean> = flow {
        try {
            emit(remoteConfig.getBoolean(flagName))
        } catch (e: Exception) {
            // Default to false if remote config fails
            emit(false)
        }
    }
}

/**
 * Extension function to initialize Firebase Remote Config with default values
 */
private fun FirebaseRemoteConfig.initializeDefaults() {
    val defaults = mapOf(
        "analytics_enabled" to true,
        "debug_mode_enabled" to false,
        "premium_features_enabled" to true
    )

    setDefaultsAsync(defaults)
}