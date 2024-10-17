package app.tinygiants.getalife.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class Account(
    val id: Long,
    val name: String,
    val balance: Money,
    val type: AccountType,
    val listPosition: Int,
    val updatedAt: Instant,
    val createdAt: Instant
)