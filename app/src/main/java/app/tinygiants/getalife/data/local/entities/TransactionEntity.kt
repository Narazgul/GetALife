package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import kotlinx.datetime.Instant

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val categoryId: Long?,
    val amount: Double,
    val transactionPartner: String,
    val transactionDirection: TransactionDirection,
    val description: String,
    val dateOfTransaction: Instant,
    val updatedAt: Instant,
    val createdAt: Instant
) {
    companion object {
        fun fromDomain(transaction: Transaction): TransactionEntity {
            return transaction.run {
                TransactionEntity(
                    id = id,
                    accountId = account.id,
                    categoryId = category?.id,
                    amount = amount.asDouble(),
                    transactionDirection = transactionDirection,
                    transactionPartner = transactionPartner,
                    description = description,
                    dateOfTransaction = dateOfTransaction,
                    updatedAt = updatedAt,
                    createdAt = createdAt
                )
            }
        }
    }

    fun toDomain(account: Account, category: Category?): Transaction {
        return Transaction(
            id = id,
            amount = Money(value = amount),
            account = account,
            category = category,
            transactionPartner = transactionPartner,
            transactionDirection = transactionDirection,
            description = description,
            dateOfTransaction = dateOfTransaction,
            updatedAt = updatedAt,
            createdAt = createdAt
        )
    }
}