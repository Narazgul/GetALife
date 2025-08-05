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

    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE accountId == :accountId")
    fun getAccountTransactionsFlow(accountId: Long): Flow<List<TransactionEntity>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE categoryId == :categoryId")
    fun getCategoryTransactionsFlow(categoryId: Long): Flow<List<TransactionEntity>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE categoryId == :categoryId AND strftime('%Y-%m', datetime(dateOfTransaction / 1000, 'unixepoch')) = :yearMonth")
    suspend fun getCategoryTransactionsForMonth(categoryId: Long, yearMonth: String): List<TransactionEntity>

    @Transaction
    @Query("SELECT * FROM transactions WHERE categoryId == :categoryId  AND createdAt >= :startTime AND createdAt <= :endTime")
    suspend fun getCategoryTransactions(categoryId: Long, startTime: Instant, endTime: Instant): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id == :transactionId")
    suspend fun getTransaction(transactionId: Long): TransactionEntity

    @Insert
    suspend fun addTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    // Recurring payment queries
    @Query("SELECT * FROM transactions WHERE isRecurring = 1 AND parentRecurringTransactionId IS NULL AND isRecurrenceActive = 1")
    fun getActiveRecurringTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1 AND nextPaymentDate <= :currentDate AND isRecurrenceActive = 1 AND parentRecurringTransactionId IS NULL")
    suspend fun getDueRecurringTransactions(currentDate: Instant): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE parentRecurringTransactionId = :recurringTransactionId ORDER BY dateOfTransaction DESC")
    suspend fun getGeneratedTransactions(recurringTransactionId: Long): List<TransactionEntity>

    @Query("UPDATE transactions SET isRecurrenceActive = :isActive WHERE id = :transactionId")
    suspend fun updateRecurrenceStatus(transactionId: Long, isActive: Boolean)

    @Query("UPDATE transactions SET nextPaymentDate = :nextDate WHERE id = :transactionId")
    suspend fun updateNextPaymentDate(transactionId: Long, nextDate: Instant)
}