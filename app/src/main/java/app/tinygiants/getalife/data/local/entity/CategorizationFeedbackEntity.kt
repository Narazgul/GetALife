package app.tinygiants.getalife.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.categorization.CategorizationFeedback
import kotlin.time.Instant

/**
 * Room entity for storing categorization feedback to improve future suggestions
 */
@Entity(tableName = "categorization_feedback")
data class CategorizationFeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionPartner: String,
    val description: String,
    val amount: Double,
    val suggestedCategoryId: Long?,
    val suggestedConfidence: Float,
    val userChosenCategoryId: Long?,
    val userCreatedNewCategory: Boolean,
    val wasCorrect: Boolean,
    val timestamp: Long, // Instant as Long
    val createdAt: Long // Instant as Long
) {
    fun toDomain(): CategorizationFeedback {
        return CategorizationFeedback(
            id = id,
            transactionPartner = transactionPartner,
            description = description,
            amount = app.tinygiants.getalife.domain.model.Money(amount),
            suggestedCategoryId = suggestedCategoryId,
            suggestedConfidence = suggestedConfidence,
            userChosenCategoryId = userChosenCategoryId,
            userCreatedNewCategory = userCreatedNewCategory,
            wasCorrect = wasCorrect,
            timestamp = Instant.fromEpochMilliseconds(timestamp),
            createdAt = Instant.fromEpochMilliseconds(createdAt)
        )
    }

    companion object {
        fun fromDomain(feedback: CategorizationFeedback): CategorizationFeedbackEntity {
            return CategorizationFeedbackEntity(
                id = feedback.id,
                transactionPartner = feedback.transactionPartner,
                description = feedback.description,
                amount = feedback.amount.asDouble(),
                suggestedCategoryId = feedback.suggestedCategoryId,
                suggestedConfidence = feedback.suggestedConfidence,
                userChosenCategoryId = feedback.userChosenCategoryId,
                userCreatedNewCategory = feedback.userCreatedNewCategory,
                wasCorrect = feedback.wasCorrect,
                timestamp = feedback.timestamp.toEpochMilliseconds(),
                createdAt = feedback.createdAt.toEpochMilliseconds()
            )
        }
    }
}