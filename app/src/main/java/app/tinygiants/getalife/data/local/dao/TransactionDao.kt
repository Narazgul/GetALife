package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.sql.Timestamp

@Dao
interface TransactionDao {

    @Transaction
    @Query("SELECT * FROM transactions WHERE id == :accountId")
    fun getAccountTransactionsFlow(accountId: Long): Flow<List<TransactionEntity>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id == :categoryId")
    fun getCategoryTransactionsFlow(categoryId: Long): Flow<List<TransactionEntity>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id == :accountId")
    suspend fun getAccountTransactions(accountId: Long): List<TransactionEntity>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id == :accountId AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getAccountTransactions(accountId: Long, startTime: Timestamp, endTime: Timestamp): List<TransactionEntity>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id == :categoryId")
    suspend fun getCategoryTransactions(categoryId: Long): List<TransactionEntity>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id == :categoryId  AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getCategoryTransactions(categoryId: Long, startTime: Timestamp, endTime: Timestamp): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id == :transactionId")
    suspend fun getTransaction(transactionId: Long): TransactionEntity

    @Insert
    suspend fun addTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}