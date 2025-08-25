package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.tinygiants.getalife.data.local.entity.CategorizationFeedbackEntity

/**
 * DAO for categorization feedback operations
 */
@Dao
interface CategorizationFeedbackDao {

    /**
     * Save feedback from user categorization decisions
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFeedback(feedback: CategorizationFeedbackEntity)

    /**
     * Get feedback for similar transactions based on partner name
     */
    @Query(
        """
        SELECT * FROM categorization_feedback 
        WHERE transactionPartner LIKE '%' || :partner || '%' 
        ORDER BY timestamp DESC 
        LIMIT 50
    """
    )
    suspend fun getFeedbackForPartner(partner: String): List<CategorizationFeedbackEntity>

    /**
     * Get feedback for similar transactions based on partner and description
     */
    @Query(
        """
        SELECT * FROM categorization_feedback 
        WHERE (transactionPartner LIKE '%' || :partner || '%' 
               OR description LIKE '%' || :description || '%')
        AND ABS(amount - :amount) <= (:amount * 0.3)
        ORDER BY timestamp DESC 
        LIMIT 20
    """
    )
    suspend fun getFeedbackForSimilarTransactions(
        partner: String,
        description: String,
        amount: Double
    ): List<CategorizationFeedbackEntity>

    /**
     * Get successful categorizations (where user accepted suggestion) for learning
     */
    @Query(
        """
        SELECT * FROM categorization_feedback 
        WHERE wasCorrect = 1 
        AND transactionPartner LIKE '%' || :partner || '%'
        ORDER BY timestamp DESC
        LIMIT 10
    """
    )
    suspend fun getSuccessfulCategorizationsForPartner(partner: String): List<CategorizationFeedbackEntity>

    /**
     * Get feedback statistics for confidence calibration
     */
    @Query(
        """
        SELECT 
            AVG(CASE WHEN wasCorrect = 1 THEN 1.0 ELSE 0.0 END) as accuracy,
            COUNT(*) as totalFeedback,
            AVG(suggestedConfidence) as avgConfidence
        FROM categorization_feedback 
        WHERE suggestedConfidence >= :minConfidence
        AND timestamp >= :sinceTimestamp
    """
    )
    suspend fun getFeedbackStats(minConfidence: Float, sinceTimestamp: Long): FeedbackStats?

    /**
     * Get recent feedback for analysis (last 30 days)
     */
    @Query(
        """
        SELECT * FROM categorization_feedback 
        WHERE timestamp >= :sinceTimestamp
        ORDER BY timestamp DESC
    """
    )
    suspend fun getRecentFeedback(sinceTimestamp: Long): List<CategorizationFeedbackEntity>

    /**
     * Clean up old feedback (keep last 1000 entries)
     */
    @Query(
        """
        DELETE FROM categorization_feedback 
        WHERE id NOT IN (
            SELECT id FROM categorization_feedback 
            ORDER BY timestamp DESC 
            LIMIT 1000
        )
    """
    )
    suspend fun cleanupOldFeedback()
}

/**
 * Data class for feedback statistics
 */
data class FeedbackStats(
    val accuracy: Float,
    val totalFeedback: Int,
    val avgConfidence: Float
)