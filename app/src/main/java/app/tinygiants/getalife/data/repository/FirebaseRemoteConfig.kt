package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.domain.repository.AiFinancialAdvisorConfig
import app.tinygiants.getalife.domain.repository.RemoteConfigRepository
import app.tinygiants.getalife.domain.repository.SmartCategorizationConfig
import com.google.android.play.core.install.model.AppUpdateType
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FirebaseRemoteConfigRepository @Inject constructor(private val remoteConfig: FirebaseRemoteConfig) :
    RemoteConfigRepository {

    init {
        remoteConfig.fetchAndActivate()
    }

    override fun getAppUpdateType(): Flow<Int?> = flow {
        val showImmediateUpdate = remoteConfig.getBoolean("immediate_update")
        val showFlexibleUpdate = remoteConfig.getBoolean("flexible_update")

        when {
            showImmediateUpdate && showFlexibleUpdate -> emit(AppUpdateType.IMMEDIATE)
            showImmediateUpdate -> emit(AppUpdateType.IMMEDIATE)
            showFlexibleUpdate -> emit(AppUpdateType.FLEXIBLE)
            else -> emit(null)
        }
    }

    override fun isSmartCategorizationEnabled(): Flow<Boolean> = flow {
        emit(remoteConfig.getBoolean("smart_categorization_enabled"))
    }

    override fun isAiFinancialAdvisorEnabled(): Flow<Boolean> = flow {
        emit(remoteConfig.getBoolean("ai_financial_advisor_enabled"))
    }

    override fun getSmartCategorizationConfig(): Flow<SmartCategorizationConfig> = flow {
        emit(
            SmartCategorizationConfig(
                isEnabled = true,//remoteConfig.getBoolean("smart_categorization_enabled"),
                confidenceThreshold = remoteConfig.getDouble("smart_categorization_confidence_threshold").toFloat(),
                maxSuggestions = remoteConfig.getLong("smart_categorization_max_suggestions").toInt(),
                enableLearning = remoteConfig.getBoolean("smart_categorization_enable_learning"),
                enableBulkCategorization = true //remoteConfig.getBoolean("smart_categorization_enable_bulk")
            )
        )
    }

    override fun getAiFinancialAdvisorConfig(): Flow<AiFinancialAdvisorConfig> = flow {
        emit(
            AiFinancialAdvisorConfig(
                isEnabled = remoteConfig.getBoolean("ai_financial_advisor_enabled"),
                maxInsightsPerSession = remoteConfig.getLong("ai_financial_advisor_max_insights").toInt(),
                minTransactionsForInsights = remoteConfig.getLong("ai_financial_advisor_min_transactions").toInt(),
                enableContextualInsights = remoteConfig.getBoolean("ai_financial_advisor_enable_contextual"),
                insightRefreshIntervalHours = remoteConfig.getLong("ai_financial_advisor_refresh_interval_hours")
            )
        )
    }
}