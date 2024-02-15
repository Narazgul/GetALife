package app.tinygiants.getalife.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Header(
    val id: Long,
    val name: String,
    val sumOfAvailableMoney: Money,
    var isExpanded: Boolean
)