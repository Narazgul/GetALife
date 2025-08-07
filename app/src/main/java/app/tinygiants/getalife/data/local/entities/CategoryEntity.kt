package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import kotlin.time.Instant

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: Long,
    val budgetId: String,
    val groupId: Long,
    val emoji: String,
    val name: String,
    val budgetTarget: Double,
    val monthlyTargetAmount: Double?, // Für Multi-Monats-Sparziele
    val targetMonthsRemaining: Int?, // Für Multi-Monats-Sparziele
    val listPosition: Int,
    val isInitialCategory: Boolean,
    val linkedAccountId: Long? = null,
    val updatedAt: Instant,
    val createdAt: Instant,
    val isSynced: Boolean = false // tracks if this category has been synced to Firestore
) {
    companion object {
        fun fromDomain(category: Category, budgetId: String): CategoryEntity {
            return category.run {
                CategoryEntity(
                    id = id,
                    budgetId = budgetId,
                    groupId = groupId,
                    emoji = emoji,
                    name = name,
                    budgetTarget = budgetTarget.asDouble(),
                    monthlyTargetAmount = monthlyTargetAmount?.asDouble(),
                    targetMonthsRemaining = targetMonthsRemaining,
                    listPosition = listPosition,
                    isInitialCategory = isInitialCategory,
                    linkedAccountId = linkedAccountId,
                    updatedAt = updatedAt,
                    createdAt = createdAt,
                    isSynced = false // new categories are not synced initially
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
            listPosition = listPosition,
            isInitialCategory = isInitialCategory,
            linkedAccountId = linkedAccountId,
            updatedAt = updatedAt,
            createdAt = createdAt
        )
    }
}