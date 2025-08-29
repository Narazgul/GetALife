package app.tinygiants.getalife.domain.usecase

import app.tinygiants.getalife.domain.repository.RemoteConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for handling feature flags and remote configuration.
 *
 * This centralizes all feature flag logic and provides a clean interface
 * for the rest of the application to check feature availability.
 */
@Singleton
class FeatureFlagUseCase @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository
) {

    /**
     * Check if a specific feature flag is enabled
     */
    fun isFeatureEnabled(flagName: String): Flow<Boolean> =
        remoteConfigRepository.getFeatureFlagValue(flagName)

    // Specific feature flag checks for common features
    fun isAnalyticsEnabled(): Flow<Boolean> =
        isFeatureEnabled("analytics_enabled")

    fun isDebugModeEnabled(): Flow<Boolean> =
        isFeatureEnabled("debug_mode_enabled")

    fun isPremiumFeaturesEnabled(): Flow<Boolean> =
        isFeatureEnabled("premium_features_enabled")
}