package app.tinygiants.getalife.domain.usecase.categorization

import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.categorization.CategoryMatch
import app.tinygiants.getalife.domain.model.categorization.CategorizationFeedback
import app.tinygiants.getalife.domain.model.categorization.NewCategorySuggestion
import app.tinygiants.getalife.domain.repository.CategorizationFeedbackRepository
import app.tinygiants.getalife.domain.usecase.FeatureFlagUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Use case for learning from user categorization decisions to improve future suggestions
 */
class SmartCategorizationLearningUseCase @Inject constructor(
    private val feedbackRepository: CategorizationFeedbackRepository,
    private val featureFlagUseCase: FeatureFlagUseCase,
    private val dispatcher: CoroutineDispatcher
) {

    /**
     * Record user decision on categorization suggestion for learning
     */
    suspend operator fun invoke(
        transaction: Transaction,
        suggestion: CategoryMatch?,
        userChoice: Category?,
        createdNewCategory: Boolean = false
    ) {
        withContext(dispatcher) {
            val config = featureFlagUseCase.getSmartCategorizationConfig().first()
            if (!config.enableLearning) return@withContext

            val feedback = createFeedback(
                transaction = transaction,
                suggestion = suggestion,
                userChoice = userChoice,
                createdNewCategory = createdNewCategory
            )

            feedbackRepository.saveFeedback(feedback)

            // Periodic cleanup of old feedback to maintain performance
            if (shouldCleanup()) {
                feedbackRepository.cleanupOldFeedback()
            }
        }
    }

    /**
     * Get improved confidence score based on historical feedback
     */
    suspend fun getImprovedConfidence(
        partner: String,
        description: String,
        categoryName: String,
        baseConfidence: Float
    ): Float {
        return withContext(dispatcher) {
            val config = featureFlagUseCase.getSmartCategorizationConfig().first()
            if (!config.enableLearning) return@withContext baseConfidence

            try {
                val historicalFeedback = feedbackRepository.getSuccessfulCategorizationsForPartner(partner)

                if (historicalFeedback.isEmpty()) return@withContext baseConfidence

                // Calculate success rate for this partner-category combination
                val matchingCategoryFeedback = historicalFeedback.filter { feedback ->
                    feedback.userChosenCategoryId != null &&
                            getCategoryName(feedback.userChosenCategoryId!!) == categoryName
                }

                if (matchingCategoryFeedback.isEmpty()) return@withContext baseConfidence

                val successRate = matchingCategoryFeedback.count { it.wasCorrect }.toFloat() /
                        matchingCategoryFeedback.size.toFloat()

                // Adjust confidence based on historical success
                val adjustment = when {
                    successRate >= 0.8f -> 0.2f  // Boost confidence for highly successful matches
                    successRate >= 0.6f -> 0.1f  // Slight boost for moderately successful matches
                    successRate < 0.4f -> -0.2f  // Reduce confidence for unsuccessful matches
                    else -> 0f // No adjustment for neutral success rates
                }

                (baseConfidence + adjustment).coerceIn(0f, 1f)
            } catch (e: Exception) {
                // Return base confidence if learning fails
                baseConfidence
            }
        }
    }

    /**
     * Get learning-enhanced category suggestions for better matching
     */
    suspend fun getEnhancedSuggestions(
        partner: String,
        description: String,
        availableCategories: List<Category>
    ): List<Pair<Category, Float>> {
        return withContext(dispatcher) {
            val config = featureFlagUseCase.getSmartCategorizationConfig().first()
            if (!config.enableLearning) return@withContext emptyList()

            try {
                val historicalFeedback = feedbackRepository.getSuccessfulCategorizationsForPartner(partner)

                if (historicalFeedback.isEmpty()) return@withContext emptyList()

                // Count successful categorizations by category
                val categorySuccessMap = historicalFeedback
                    .filter { it.wasCorrect && it.userChosenCategoryId != null }
                    .groupBy { it.userChosenCategoryId!! }
                    .mapValues { (_, feedbacks) -> feedbacks.size }

                // Convert to available categories with confidence scores
                availableCategories.mapNotNull { category ->
                    val successCount = categorySuccessMap[category.id] ?: 0
                    if (successCount > 0) {
                        val confidence = (successCount.toFloat() / historicalFeedback.size.toFloat())
                            .coerceAtMost(0.9f) // Cap at 0.9 to allow room for other factors
                        Pair(category, confidence)
                    } else null
                }.sortedByDescending { it.second }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun createFeedback(
        transaction: Transaction,
        suggestion: CategoryMatch?,
        userChoice: Category?,
        createdNewCategory: Boolean
    ): CategorizationFeedback {
        val wasCorrect = when {
            suggestion == null -> false // No suggestion was made
            userChoice == null -> false // User didn't choose any category
            createdNewCategory -> false // User created new category (suggestion wasn't good enough)
            else -> suggestion.categoryId == userChoice.id // User accepted the suggestion
        }

        return CategorizationFeedback(
            id = 0, // Auto-generated
            transactionPartner = transaction.transactionPartner,
            description = transaction.description,
            amount = transaction.amount,
            suggestedCategoryId = suggestion?.categoryId,
            suggestedConfidence = suggestion?.confidence ?: 0f,
            userChosenCategoryId = userChoice?.id,
            userCreatedNewCategory = createdNewCategory,
            wasCorrect = wasCorrect,
            timestamp = Clock.System.now(),
            createdAt = Clock.System.now()
        )
    }

    private fun shouldCleanup(): Boolean {
        // Clean up roughly every 100 feedback entries
        return (0..99).random() == 0
    }

    // TODO: This should be moved to a proper category lookup
    private fun getCategoryName(categoryId: Long): String {
        // This is a placeholder - in real implementation, we'd lookup the category name
        return "Category_$categoryId"
    }
}