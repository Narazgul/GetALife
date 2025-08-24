package app.tinygiants.getalife.domain.model.categorization

import androidx.compose.runtime.Immutable
import app.tinygiants.getalife.domain.model.Money
import kotlin.time.Instant

/**
 * Result of smart categorization containing both existing category matches
 * and suggestions for new categories
 */
@Immutable
data class CategorizationResult(
    val existingCategoryMatch: CategoryMatch?,
    val newCategorySuggestion: NewCategorySuggestion?
) {
    val hasAnyMatch: Boolean get() = existingCategoryMatch != null || newCategorySuggestion != null
    val bestConfidence: Float get() = existingCategoryMatch?.confidence ?: 0f
}

/**
 * A match with an existing user category
 */
@Immutable
data class CategoryMatch(
    val categoryId: Long,
    val categoryName: String,
    val categoryEmoji: String,
    val confidence: Float,
    val reasoning: String = ""
) {
    companion object {
        fun noMatch() = CategoryMatch(-1, "", "", 0f, "No matching category found")
    }

    val isConfident: Boolean get() = confidence >= 0.7f
    val isReasonable: Boolean get() = confidence >= 0.3f
}

/**
 * AI suggestion for creating a new category
 */
@Immutable
data class NewCategorySuggestion(
    val categoryName: String,
    val emoji: String,
    val groupId: Long,
    val groupName: String,
    val reasoning: String,
    val suggestedBudget: Money? = null
)

/**
 * User feedback on categorization suggestions for learning
 */
data class CategorizationFeedback(
    val id: Long = 0,
    val transactionPartner: String,
    val description: String,
    val amount: Money,
    val suggestedCategoryId: Long?,
    val suggestedConfidence: Float,
    val userChosenCategoryId: Long?,
    val userCreatedNewCategory: Boolean,
    val wasCorrect: Boolean,
    val timestamp: Instant,
    val createdAt: Instant = Instant.DISTANT_PAST
)

/**
 * Configuration for similarity calculation
 */
data class SimilarityConfig(
    val keywordWeight: Float = 0.4f,
    val historicalWeight: Float = 0.4f,
    val aiWeight: Float = 0.2f,
    val minConfidenceThreshold: Float = 0.3f
)