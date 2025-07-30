package app.tinygiants.getalife.domain.model

import app.tinygiants.getalife.presentation.shared_composables.UiText
import kotlin.time.Instant

data class Category(
    val id: Long,
    val groupId: Long,
    val emoji: String,
    val name: String,
    val budgetTarget: Money,
    val monthlyTargetAmount: Money?,
    val targetMonthsRemaining: Int?,
    val progress: Progress,
    val optionalText: UiText,
    val listPosition: Int,
    val isInitialCategory: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant
)