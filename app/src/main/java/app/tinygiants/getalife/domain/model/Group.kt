package app.tinygiants.getalife.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Group(
    val id: Long,
    val name: String,
    val sumOfAvailableMoney: Money,
    val listPosition: Int,
    var isExpanded: Boolean
)