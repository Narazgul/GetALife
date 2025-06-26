package app.tinygiants.getalife.domain.model

import kotlin.time.Instant

data class Transaction(
    val id: Long,
    val amount: Money,
    val account: Account,
    val category: Category?,
    val transactionPartner: String,
    val transactionDirection: TransactionDirection,
    val description: String,
    val dateOfTransaction: Instant,
    val updatedAt: Instant,
    val createdAt: Instant
)

enum class TransactionDirection { Unknown, Inflow, Outflow, AccountTransfer }