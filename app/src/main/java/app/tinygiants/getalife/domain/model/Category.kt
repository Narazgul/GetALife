package app.tinygiants.getalife.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Category(
    val id: Long,
    val headerId: Long,
    val emoji: String,
    val name: String,
    val budgetTarget: Money,
    val availableMoney: Money,
    val progress: Float,
    val optionalText: String,
    val listPosition: Int,
    val isEmptyCategory: Boolean
)