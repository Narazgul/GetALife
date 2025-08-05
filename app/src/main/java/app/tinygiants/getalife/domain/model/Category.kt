package app.tinygiants.getalife.domain.model

import kotlin.time.Instant

data class Category(
    val id: Long,
    val groupId: Long,
    val emoji: String,
    val name: String,
    val budgetTarget: Money,
    val monthlyTargetAmount: Money?,
    val targetMonthsRemaining: Int?,
    val listPosition: Int,
    val isInitialCategory: Boolean,
    val linkedAccountId: Long? = null, // For credit card payment categories
    val updatedAt: Instant,
    val createdAt: Instant
) {
    val behaviorType: CategoryBehaviorType
        get() = if (linkedAccountId != null) CategoryBehaviorType.CreditCardPayment
        else CategoryBehaviorType.Normal
}

enum class CategoryBehaviorType {
    Normal,
    CreditCardPayment
}