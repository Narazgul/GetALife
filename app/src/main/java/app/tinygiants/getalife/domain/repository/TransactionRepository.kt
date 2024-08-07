package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getTransactions(): Flow<Result<List<TransactionEntity>>>
    fun getTransactionsByAccountFlow(accountId: Long): Flow<Result<List<TransactionEntity>>>
    suspend fun getTransactionsByCategory(categoryId: Long): List<TransactionEntity>
    suspend fun addTransaction(transaction: TransactionEntity)
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(transaction: TransactionEntity)
}