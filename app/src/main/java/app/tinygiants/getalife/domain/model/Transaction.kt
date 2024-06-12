package app.tinygiants.getalife.domain.model

data class Transaction(
    val id: Long,
    val amount: Money,
    val account: Account,
    val category: Category?,
    val transactionPartner: String,
    val direction: TransactionDirection,
    val description: String
)