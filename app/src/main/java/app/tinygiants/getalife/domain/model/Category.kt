package app.tinygiants.getalife.domain.model

import app.tinygiants.getalife.presentation.UiText
import kotlinx.datetime.Instant

data class Category(
    val id: Long,
    val groupId: Long,
    val emoji: String,
    val name: String,
    val budgetTarget: Money,
    val assignedMoney: Money,
    val availableMoney: Money,
    val progress: Progress,
    val optionalText: UiText,
    val listPosition: Int,
    val isInitialCategory: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant
)