package app.tinygiants.getalife.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

enum class TargetType {
    NONE,
    NEEDED_FOR_SPENDING,  // "Ich brauche monatlich 300€ für Lebensmittel" (resets monthly)
    SAVINGS_BALANCE       // "Ich spare auf 2000€ für Urlaub" (accumulates)
}

enum class RepeatFrequency {
    YEARLY,
    NEVER
}

enum class CategoryBehaviorType {
    Normal,
    CreditCardPayment
}

data class Category(
    val id: Long,
    val groupId: Long,
    val emoji: String,
    val name: String,
    val budgetTarget: Money,
    val monthlyTargetAmount: Money?,
    val targetMonthsRemaining: Int?,
    val targetType: TargetType = TargetType.NONE,
    val targetAmount: Money? = null,
    val targetDate: LocalDate? = null,
    val isRepeating: Boolean = false,
    val repeatFrequency: RepeatFrequency = RepeatFrequency.NEVER,
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