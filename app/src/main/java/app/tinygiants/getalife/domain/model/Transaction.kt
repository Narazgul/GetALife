package app.tinygiants.getalife.domain.model

import kotlinx.datetime.Instant

data class Transaction(
    val id: Long,
    val amount: Money,
    val account: Account?,
    val category: Category?,
    val transactionPartner: String,
    val direction: TransactionDirection,
    val description: String,
    val updatedAt: Instant,
    val createdAt: Instant
)