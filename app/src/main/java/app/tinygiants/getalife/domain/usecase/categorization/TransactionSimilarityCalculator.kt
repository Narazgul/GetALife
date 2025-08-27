package app.tinygiants.getalife.domain.usecase.categorization

import app.tinygiants.getalife.domain.model.categorization.SimilarityConfig
import app.tinygiants.getalife.domain.repository.AiRepository
import app.tinygiants.getalife.domain.repository.CategorizationFeedbackRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Calculates similarity between transaction details and category names
 * using multiple strategies (keyword matching, historical patterns, AI semantic similarity)
 */
class TransactionSimilarityCalculator @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val aiRepository: AiRepository,
    private val feedbackRepository: CategorizationFeedbackRepository,
    private val dispatcher: CoroutineDispatcher
) {

    suspend fun calculateSimilarity(
        partner: String,
        description: String,
        categoryName: String,
        config: SimilarityConfig = SimilarityConfig()
    ): Float {
        return withContext(dispatcher) {
            val keywordSimilarity = calculateKeywordSimilarity(
                "$partner $description".lowercase(),
                categoryName.lowercase()
            )

            val historicalSimilarity = getHistoricalSimilarity(partner, categoryName)

            val aiSimilarity = try {
                getAiSemanticSimilarity(partner, description, categoryName)
            } catch (e: Exception) {
                0f // Fallback if AI fails
            }

            // Weighted combination
            val finalSimilarity = (keywordSimilarity * config.keywordWeight +
                    historicalSimilarity * config.historicalWeight +
                    aiSimilarity * config.aiWeight)

            finalSimilarity.coerceIn(0f, 1f)
        }
    }

    /**
     * Calculate simple keyword-based similarity using Jaccard similarity
     */
    private fun calculateKeywordSimilarity(text1: String, text2: String): Float {
        val words1 = text1.split("\\s+".toRegex()).filter { it.length > 2 }.toSet()
        val words2 = text2.split("\\s+".toRegex()).filter { it.length > 2 }.toSet()

        if (words1.isEmpty() || words2.isEmpty()) return 0f

        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size

        return if (union > 0) intersection.toFloat() / union.toFloat() else 0f
    }

    /**
     * Get similarity based on historical categorization patterns
     * Enhanced with feedback data for better accuracy
     */
    private suspend fun getHistoricalSimilarity(partner: String, categoryName: String): Float {
        return try {
            // First check user feedback for similar transactions
            val feedbackSimilarity = getFeedbackBasedSimilarity(partner, categoryName)
            if (feedbackSimilarity > 0f) return feedbackSimilarity

            // Fallback to transaction history
            val similarTransactions = transactionRepository.getTransactionsByPartner(partner)

            if (similarTransactions.isEmpty()) return 0f

            val categorizedSimilar = similarTransactions.filter { it.category?.name == categoryName }

            categorizedSimilar.size.toFloat() / similarTransactions.size.toFloat()
        } catch (e: Exception) {
            0f // Graceful fallback
        }
    }

    /**
     * Get similarity based on user feedback data
     */
    private suspend fun getFeedbackBasedSimilarity(partner: String, categoryName: String): Float {
        return try {
            val successfulFeedback = feedbackRepository.getSuccessfulCategorizationsForPartner(partner)

            if (successfulFeedback.isEmpty()) return 0f

            // Count how often this partner was successfully categorized to this category
            var categoryMatches = 0

            for (feedback in successfulFeedback) {
                if (feedback.userChosenCategoryId != null &&
                    getCategoryName(feedback.userChosenCategoryId!!) == categoryName
                ) {
                    categoryMatches++
                }
            }

            if (categoryMatches == 0) return 0f

            // Calculate success rate with confidence boost for frequently correct matches
            val successRate = categoryMatches.toFloat() / successfulFeedback.size.toFloat()

            // Boost confidence for categories that were chosen multiple times
            val frequencyBonus = when {
                categoryMatches >= 5 -> 0.2f
                categoryMatches >= 3 -> 0.1f
                else -> 0f
            }

            (successRate + frequencyBonus).coerceAtMost(1f)
        } catch (e: Exception) {
            0f
        }
    }

    /**
     * Get AI-powered semantic similarity
     */
    private suspend fun getAiSemanticSimilarity(
        partner: String,
        description: String,
        categoryName: String
    ): Float {
        return aiRepository.calculateSemanticSimilarity(
            text1 = "$partner $description",
            text2 = categoryName
        ).getOrElse { 0f }
    }

    /**
     * Get category name by ID using CategoryRepository
     */
    private suspend fun getCategoryName(categoryId: Long): String {
        return try {
            categoryRepository.getCategory(categoryId)?.name ?: "Unknown Category"
        } catch (e: Exception) {
            "Unknown Category"
        }
    }

    /**
     * Calculate similarity between two transactions for smart categorization
     *
     * @param partner1 Transaction partner of first transaction
     * @param description1 Description of first transaction
     * @param partner2 Transaction partner of second transaction
     * @param description2 Description of second transaction
     * @return Similarity score between 0.0 and 1.0
     */
    suspend fun calculateSimilarity(
        transaction1: app.tinygiants.getalife.domain.model.Transaction,
        transaction2: app.tinygiants.getalife.domain.model.Transaction,
        config: SimilarityConfig = SimilarityConfig()
    ): Float {
        return withContext(dispatcher) {
            val partnerSimilarity = calculateKeywordSimilarity(
                transaction1.transactionPartner.lowercase(),
                transaction2.transactionPartner.lowercase()
            )

            val descriptionSimilarity = calculateKeywordSimilarity(
                transaction1.description.lowercase(),
                transaction2.description.lowercase()
            )

            val amountSimilarity = calculateAmountSimilarity(
                transaction1.amount,
                transaction2.amount
            )

            // Weighted combination for transaction-to-transaction similarity
            val finalSimilarity = (partnerSimilarity * 0.5f +
                    descriptionSimilarity * 0.3f +
                    amountSimilarity * 0.2f)

            finalSimilarity.coerceIn(0f, 1f)
        }
    }

    /**
     * Calculate amount similarity between two Money values
     */
    private fun calculateAmountSimilarity(
        amount1: app.tinygiants.getalife.domain.model.Money,
        amount2: app.tinygiants.getalife.domain.model.Money
    ): Float {
        val diff = kotlin.math.abs(amount1.asDouble() - amount2.asDouble())
        val maxAmount = kotlin.math.max(kotlin.math.abs(amount1.asDouble()), kotlin.math.abs(amount2.asDouble()))

        return if (maxAmount == 0.0) {
            1f // Both amounts are zero
        } else {
            val similarity = 1f - (diff.toFloat() / maxAmount.toFloat())
            similarity.coerceIn(0f, 1f)
        }
    }
}