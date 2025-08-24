package app.tinygiants.getalife.domain.repository

import kotlinx.coroutines.flow.Flow

interface RemoteConfigRepository {

    fun getAppUpdateType(): Flow<Int?>

    // Feature Flags
    fun isSmartCategorizationEnabled(): Flow<Boolean>
    fun isAiFinancialAdvisorEnabled(): Flow<Boolean>
    fun getSmartCategorizationConfig(): Flow<SmartCategorizationConfig>
    fun getAiFinancialAdvisorConfig(): Flow<AiFinancialAdvisorConfig>
}

data class SmartCategorizationConfig(
    val isEnabled: Boolean,
    val confidenceThreshold: Float,
    val maxSuggestions: Int,
    val enableLearning: Boolean,
    val enableBulkCategorization: Boolean
)

data class AiFinancialAdvisorConfig(
    val isEnabled: Boolean,
    val maxInsightsPerSession: Int,
    val minTransactionsForInsights: Int,
    val enableContextualInsights: Boolean,
    val insightRefreshIntervalHours: Long
)