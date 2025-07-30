package app.tinygiants.getalife.domain.model

/**
 * Complete model that combines category data with monthly assignment and calculated values.
 * Replaces both CategoryMonthlyAssignment and the old CategoryMonthlyStatus.
 */
data class CategoryMonthlyStatus(
    val category: Category,
    val assignedAmount: Money,
    val isCarryOverEnabled: Boolean,
    val spentAmount: Money,
    val availableAmount: Money,
    val progress: Progress,
    val suggestedAmount: Money?
)