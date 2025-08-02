package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.Money
import kotlinx.datetime.YearMonth

@Entity(
    tableName = "category_monthly_status",
    indices = [Index(value = ["categoryId", "yearMonth"], unique = true)]
)
data class CategoryMonthlyStatusEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val yearMonth: String, // Format: "2024-01"
    val assignedAmount: Double,
    val isCarryOverEnabled: Boolean = true,
    // New pre-calculated fields for performance
    val spentAmount: Double = 0.0,
    val carryOverFromPrevious: Double = 0.0,
    val availableAmount: Double = 0.0,
    val updatedAt: Long = 0L // Timestamp when last calculated
) {
    /**
     * Converts to domain model using pre-calculated fields from database.
     */
    fun toDomain(
        category: app.tinygiants.getalife.domain.model.Category,
        progress: app.tinygiants.getalife.domain.model.Progress,
        suggestedAmount: Money?
    ): CategoryMonthlyStatus {
        return CategoryMonthlyStatus(
            category = category,
            assignedAmount = Money(value = assignedAmount),
            isCarryOverEnabled = isCarryOverEnabled,
            spentAmount = Money(value = spentAmount),
            availableAmount = Money(value = availableAmount),
            progress = progress,
            suggestedAmount = suggestedAmount
        )
    }

    companion object {
        /**
         * Creates entity from domain model - stores all calculated fields for performance.
         */
        fun fromDomain(
            status: CategoryMonthlyStatus,
            yearMonth: YearMonth,
            carryOverFromPrevious: Money = Money(0.0)
        ): CategoryMonthlyStatusEntity {
            return CategoryMonthlyStatusEntity(
                categoryId = status.category.id,
                yearMonth = yearMonth.toString(),
                assignedAmount = status.assignedAmount.asDouble(),
                isCarryOverEnabled = status.isCarryOverEnabled,
                spentAmount = status.spentAmount.asDouble(),
                carryOverFromPrevious = carryOverFromPrevious.asDouble(),
                availableAmount = status.availableAmount.asDouble(),
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}