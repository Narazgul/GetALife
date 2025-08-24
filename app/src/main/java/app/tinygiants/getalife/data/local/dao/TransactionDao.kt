package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE budgetId = :budgetId")
    fun getAllTransactions(budgetId: String): Flow<List<TransactionEntity>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE accountId == :accountId AND budgetId = :budgetId")
    fun getAccountTransactionsFlow(accountId: Long, budgetId: String): Flow<List<TransactionEntity>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE categoryId == :categoryId AND budgetId = :budgetId")
    fun getCategoryTransactionsFlow(categoryId: Long, budgetId: String): Flow<List<TransactionEntity>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE categoryId == :categoryId AND budgetId = :budgetId AND strftime('%Y-%m', datetime(dateOfTransaction / 1000, 'unixepoch')) = :yearMonth")
    suspend fun getCategoryTransactionsForMonth(categoryId: Long, budgetId: String, yearMonth: String): List<TransactionEntity>

    @Transaction
    @Query("SELECT * FROM transactions WHERE categoryId == :categoryId AND budgetId = :budgetId AND createdAt >= :startTime AND createdAt <= :endTime")
    suspend fun getCategoryTransactions(
        categoryId: Long,
        budgetId: String,
        startTime: Instant,
        endTime: Instant
    ): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id == :transactionId AND budgetId = :budgetId")
    suspend fun getTransaction(transactionId: Long, budgetId: String): TransactionEntity

    @Insert
    suspend fun addTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    // Recurring payment queries
    @Query("SELECT * FROM transactions WHERE isRecurring = 1 AND parentRecurringTransactionId IS NULL AND isRecurrenceActive = 1 AND budgetId = :budgetId")
    fun getActiveRecurringTransactions(budgetId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1 AND nextPaymentDate <= :currentDate AND isRecurrenceActive = 1 AND parentRecurringTransactionId IS NULL AND budgetId = :budgetId")
    suspend fun getDueRecurringTransactions(currentDate: Instant, budgetId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE parentRecurringTransactionId = :recurringTransactionId AND budgetId = :budgetId ORDER BY dateOfTransaction DESC")
    suspend fun getGeneratedTransactions(recurringTransactionId: Long, budgetId: String): List<TransactionEntity>

    @Query("UPDATE transactions SET isRecurrenceActive = :isActive WHERE id = :transactionId AND budgetId = :budgetId")
    suspend fun updateRecurrenceStatus(transactionId: Long, budgetId: String, isActive: Boolean)

    @Query("UPDATE transactions SET nextPaymentDate = :nextDate WHERE id = :transactionId AND budgetId = :budgetId")
    suspend fun updateNextPaymentDate(transactionId: Long, budgetId: String, nextDate: Instant)

    // Smart categorization queries
    @Query("SELECT * FROM transactions WHERE transactionPartner LIKE '%' || :partner || '%' AND budgetId = :budgetId")
    suspend fun getTransactionsByPartner(partner: String, budgetId: String): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions 
        WHERE budgetId = :budgetId 
        AND (transactionPartner LIKE '%' || :partner || '%' 
             OR description LIKE '%' || :description || '%')
        AND ABS(amount - :amount) <= (:amount * 0.2)
        ORDER BY dateOfTransaction DESC 
        LIMIT 20
    """
    )
    suspend fun findSimilarTransactions(
        partner: String,
        description: String,
        amount: Double,
        budgetId: String
    ): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE categoryId IS NULL AND budgetId = :budgetId ORDER BY dateOfTransaction DESC")
    suspend fun getUncategorizedTransactions(budgetId: String): List<TransactionEntity>
}