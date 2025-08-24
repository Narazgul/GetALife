package app.tinygiants.getalife.domain.usecase

import app.tinygiants.getalife.domain.repository.AiFinancialAdvisorConfig
import app.tinygiants.getalife.domain.repository.RemoteConfigRepository
import app.tinygiants.getalife.domain.repository.SmartCategorizationConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for managing feature flags from Firebase Remote Config.
 * Provides a centralized way to check feature availability across the app.
 */
@Singleton
class FeatureFlagUseCase @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val dispatcher: CoroutineDispatcher
) {

    /**
     * Check if smart categorization feature is enabled
     */
    fun isSmartCategorizationEnabled(): Flow<Boolean> =
        remoteConfigRepository.isSmartCategorizationEnabled()
            .flowOn(dispatcher)

    /**
     * Check if AI financial advisor feature is enabled
     */
    fun isAiFinancialAdvisorEnabled(): Flow<Boolean> =
        remoteConfigRepository.isAiFinancialAdvisorEnabled()
            .flowOn(dispatcher)

    /**
     * Get complete smart categorization configuration
     */
    fun getSmartCategorizationConfig(): Flow<SmartCategorizationConfig> =
        remoteConfigRepository.getSmartCategorizationConfig()
            .flowOn(dispatcher)

    /**
     * Get complete AI financial advisor configuration
     */
    fun getAiFinancialAdvisorConfig(): Flow<AiFinancialAdvisorConfig> =
        remoteConfigRepository.getAiFinancialAdvisorConfig()
            .flowOn(dispatcher)
}