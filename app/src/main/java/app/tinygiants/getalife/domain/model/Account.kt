package app.tinygiants.getalife.domain.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
data class Account(
    val id: Long,
    val name: String,
    val balance: Money,
    val type: AccountType,
    val listPosition: Int,
    val isClosed: Boolean = false, // Accounts can be closed instead of deleted if they have transactions
    val updatedAt: Instant,
    val createdAt: Instant
)

enum class AccountType { Unknown, Cash, Checking, Savings, CreditCard, Mortgage, Loan, Depot  }