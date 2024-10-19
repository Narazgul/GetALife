package app.tinygiants.getalife.domain.model

import kotlinx.datetime.Instant

data class Category(
    val id: Long,
    val groupId: Long,
    val emoji: String,
    val name: String,
    val budgetTarget: Money?,
    val budgetPurpose: BudgetPurpose,
    val assignedMoney: Money,
    val availableMoney: Money,
    val progress: Float,
    val spentProgress: Float,
    val overspentProgress: Float,
    val budgetTargetProgress: Float?,
    val optionalText: String,
    val listPosition: Int,
    val isInitialCategory: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant
)