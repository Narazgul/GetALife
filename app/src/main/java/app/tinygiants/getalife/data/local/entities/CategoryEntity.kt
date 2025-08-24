package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TargetType
import app.tinygiants.getalife.domain.model.RepeatFrequency
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: Long,
    val budgetId: String,
    val groupId: Long,
    val emoji: String,
    val name: String,
    val budgetTarget: Double,
    val monthlyTargetAmount: Double?,
    val targetMonthsRemaining: Int?,
    val listPosition: Int,
    val isInitialCategory: Boolean,
    val linkedAccountId: Long? = null,
    val targetType: TargetType = TargetType.NONE,
    val targetAmount: Double? = null,
    val targetDate: String? = null, // ISO-8601 yyyy-MM-dd
    val isRepeating: Boolean = false,
    val repeatFrequency: RepeatFrequency = RepeatFrequency.NEVER,
    val updatedAt: Instant,
    val createdAt: Instant,
    val isSynced: Boolean = false
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
                    targetType = targetType,
                    targetAmount = targetAmount?.asDouble(),
                    targetDate = targetDate?.toString(),
                    isRepeating = isRepeating,
                    repeatFrequency = repeatFrequency,
                    updatedAt = updatedAt,
                    createdAt = createdAt,
                    isSynced = false
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
            targetType = targetType,
            targetAmount = targetAmount?.let { Money(value = it) },
            targetDate = targetDate?.let { LocalDate.parse(it) },
            isRepeating = isRepeating,
            repeatFrequency = repeatFrequency,
            updatedAt = updatedAt,
            createdAt = createdAt
        )
    }
}