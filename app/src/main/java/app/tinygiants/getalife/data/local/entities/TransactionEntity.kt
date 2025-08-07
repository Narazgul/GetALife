package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import kotlin.time.Instant

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["isRecurring"]),
        Index(value = ["nextPaymentDate"]),
        Index(value = ["isRecurring", "nextPaymentDate", "isRecurrenceActive"], name = "idx_recurring_payments")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val budgetId: String,
    val accountId: Long,
    val categoryId: Long?,
    val amount: Double,
    val transactionPartner: String,
    val transactionDirection: TransactionDirection,
    val description: String,
    val dateOfTransaction: Instant,
    val updatedAt: Instant,
    val createdAt: Instant,
    // Recurring payment fields
    val isRecurring: Boolean = false,
    val recurrenceFrequency: String? = null, // Store enum name as string
    val nextPaymentDate: Instant? = null,
    val recurrenceEndDate: Instant? = null,
    val isRecurrenceActive: Boolean = true,
    val parentRecurringTransactionId: Long? = null,
    val isSynced: Boolean = false // tracks if this transaction has been synced to Firestore
) {
    companion object {
        fun fromDomain(transaction: Transaction, budgetId: String): TransactionEntity {
            return transaction.run {
                TransactionEntity(
                    id = id,
                    budgetId = budgetId,
                    accountId = account.id,
                    categoryId = category?.id,
                    amount = amount.asDouble(),
                    transactionDirection = transactionDirection,
                    transactionPartner = transactionPartner,
                    description = description,
                    dateOfTransaction = dateOfTransaction,
                    updatedAt = updatedAt,
                    createdAt = createdAt,
                    isRecurring = isRecurring,
                    recurrenceFrequency = recurrenceFrequency?.name,
                    nextPaymentDate = nextPaymentDate,
                    recurrenceEndDate = recurrenceEndDate,
                    isRecurrenceActive = isRecurrenceActive,
                    parentRecurringTransactionId = parentRecurringTransactionId,
                    isSynced = false // new transactions are not synced initially
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
            createdAt = createdAt,
            isRecurring = isRecurring,
            recurrenceFrequency = recurrenceFrequency?.let { RecurrenceFrequency.valueOf(it) },
            nextPaymentDate = nextPaymentDate,
            recurrenceEndDate = recurrenceEndDate,
            isRecurrenceActive = isRecurrenceActive,
            parentRecurringTransactionId = parentRecurringTransactionId
        )
    }
}