package app.tinygiants.getalife.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Account(
    val id: Long,
    val name: String,
    val balance: Money,
    val type: AccountType,
    val listPosition: Int
)