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
    val isCarryOverEnabled: Boolean = true
) {
    /**
     * Converts to domain model with calculated fields provided by use case.
     * Only persistent fields are stored in entity.
     */
    fun toDomain(
        category: app.tinygiants.getalife.domain.model.Category,
        spentAmount: Money,
        availableAmount: Money,
        progress: app.tinygiants.getalife.domain.model.Progress,
        suggestedAmount: Money?
    ): CategoryMonthlyStatus {
        return CategoryMonthlyStatus(
            category = category,
            assignedAmount = Money(value = assignedAmount),
            isCarryOverEnabled = isCarryOverEnabled,
            spentAmount = spentAmount,
            availableAmount = availableAmount,
            progress = progress,
            suggestedAmount = suggestedAmount
        )
    }

    companion object {
        /**
         * Creates entity from domain model - only stores persistent fields.
         * Calculated fields (spent, available, progress) are ignored.
         */
        fun fromDomain(status: CategoryMonthlyStatus, yearMonth: YearMonth): CategoryMonthlyStatusEntity {
            return CategoryMonthlyStatusEntity(
                categoryId = status.category.id,
                yearMonth = yearMonth.toString(),
                assignedAmount = status.assignedAmount.asDouble(),
                isCarryOverEnabled = status.isCarryOverEnabled
            )
        }
    }
}