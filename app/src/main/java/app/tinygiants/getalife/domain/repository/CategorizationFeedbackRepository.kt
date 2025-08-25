package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.dao.FeedbackStats
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.categorization.CategorizationFeedback
import kotlin.time.Instant

/**
 * Repository interface for categorization feedback operations
 */
interface CategorizationFeedbackRepository {

    /**
     * Save user feedback on categorization suggestion
     */
    suspend fun saveFeedback(feedback: CategorizationFeedback)

    /**
     * Get feedback for similar transactions to improve suggestions
     */
    suspend fun getFeedbackForSimilarTransactions(
        partner: String,
        description: String,
        amount: Money
    ): List<CategorizationFeedback>

    /**
     * Get successful categorizations for a specific partner
     */
    suspend fun getSuccessfulCategorizationsForPartner(partner: String): List<CategorizationFeedback>

    /**
     * Get feedback statistics for confidence calibration
     */
    suspend fun getFeedbackStats(minConfidence: Float, since: Instant): FeedbackStats?

    /**
     * Get recent feedback for analysis
     */
    suspend fun getRecentFeedback(since: Instant): List<CategorizationFeedback>

    /**
     * Clean up old feedback to maintain performance
     */
    suspend fun cleanupOldFeedback()
}