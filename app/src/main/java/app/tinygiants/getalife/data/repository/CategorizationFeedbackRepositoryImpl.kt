package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.CategorizationFeedbackDao
import app.tinygiants.getalife.data.local.dao.FeedbackStats
import app.tinygiants.getalife.data.local.entity.CategorizationFeedbackEntity
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.categorization.CategorizationFeedback
import app.tinygiants.getalife.domain.repository.CategorizationFeedbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

/**
 * Implementation of categorization feedback repository
 */
@Singleton
class CategorizationFeedbackRepositoryImpl @Inject constructor(
    private val feedbackDao: CategorizationFeedbackDao,
    private val dispatcher: CoroutineDispatcher
) : CategorizationFeedbackRepository {

    override suspend fun saveFeedback(feedback: CategorizationFeedback) {
        withContext(dispatcher) {
            val entity = CategorizationFeedbackEntity.fromDomain(feedback)
            feedbackDao.saveFeedback(entity)
        }
    }

    override suspend fun getFeedbackForSimilarTransactions(
        partner: String,
        description: String,
        amount: Money
    ): List<CategorizationFeedback> {
        return withContext(dispatcher) {
            feedbackDao.getFeedbackForSimilarTransactions(
                partner = partner,
                description = description,
                amount = amount.asDouble()
            ).map { it.toDomain() }
        }
    }

    override suspend fun getSuccessfulCategorizationsForPartner(partner: String): List<CategorizationFeedback> {
        return withContext(dispatcher) {
            feedbackDao.getSuccessfulCategorizationsForPartner(partner)
                .map { it.toDomain() }
        }
    }

    override suspend fun getFeedbackStats(minConfidence: Float, since: Instant): FeedbackStats? {
        return withContext(dispatcher) {
            feedbackDao.getFeedbackStats(
                minConfidence = minConfidence,
                sinceTimestamp = since.toEpochMilliseconds()
            )
        }
    }

    override suspend fun getRecentFeedback(since: Instant): List<CategorizationFeedback> {
        return withContext(dispatcher) {
            feedbackDao.getRecentFeedback(since.toEpochMilliseconds())
                .map { it.toDomain() }
        }
    }

    override suspend fun cleanupOldFeedback() {
        withContext(dispatcher) {
            feedbackDao.cleanupOldFeedback()
        }
    }
}