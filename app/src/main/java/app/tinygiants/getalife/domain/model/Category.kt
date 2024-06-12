package app.tinygiants.getalife.domain.model

data class Category(
    val id: Long,
    val headerId: Long,
    val emoji: String,
    val name: String,
    val budgetTarget: Money,
    val budgetPurpose: BudgetPurpose,
    val assignedMoney: Money,
    val availableMoney: Money,
    val progress: Float,
    val optionalText: String,
    val listPosition: Int,
    val isInitialCategory: Boolean
)