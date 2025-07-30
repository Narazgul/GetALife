package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.shared_composables.UiText
import kotlin.time.Instant

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: Long,
    val groupId: Long,
    val emoji: String,
    val name: String,
    val budgetTarget: Double,
    val monthlyTargetAmount: Double?, // Für Multi-Monats-Sparziele
    val targetMonthsRemaining: Int?, // Für Multi-Monats-Sparziele
    val listPosition: Int,
    val isInitialCategory: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant
) {
    companion object {
        fun fromDomain(category: Category): CategoryEntity {
            return category.run {
                CategoryEntity(
                    id = id,
                    groupId = groupId,
                    emoji = emoji,
                    name = name,
                    budgetTarget = budgetTarget.asDouble(),
                    monthlyTargetAmount = monthlyTargetAmount?.asDouble(),
                    targetMonthsRemaining = targetMonthsRemaining,
                    listPosition = listPosition,
                    isInitialCategory = isInitialCategory,
                    updatedAt = updatedAt,
                    createdAt = createdAt
                )
            }
        }
    }

    fun toDomain(): Category {
        return Category(
            id = id,
            groupId = groupId,
            emoji = emoji,
            name = name,
            budgetTarget = Money(value = budgetTarget),
            monthlyTargetAmount = monthlyTargetAmount?.let { Money(value = it) },
            targetMonthsRemaining = targetMonthsRemaining,
            progress = EmptyProgress(),
            optionalText = UiText.DynamicString(value = ""),
            listPosition = listPosition,
            isInitialCategory = isInitialCategory,
            updatedAt = updatedAt,
            createdAt = createdAt
        )
    }
}