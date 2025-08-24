package app.tinygiants.getalife.domain.usecase.categorization

import app.tinygiants.getalife.domain.model.categorization.SimilarityConfig
import app.tinygiants.getalife.domain.repository.AiRepository
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
    private val aiRepository: AiRepository,
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
     */
    private suspend fun getHistoricalSimilarity(partner: String, categoryName: String): Float {
        return try {
            // Find similar transactions from history (simplified implementation)
            val similarTransactions = transactionRepository.getTransactionsByPartner(partner)

            if (similarTransactions.isEmpty()) return 0f

            val categorizedSimilar = similarTransactions.filter { it.category?.name == categoryName }

            categorizedSimilar.size.toFloat() / similarTransactions.size.toFloat()
        } catch (e: Exception) {
            0f // Graceful fallback
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
}