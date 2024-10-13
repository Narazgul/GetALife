package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getTransactions(): Flow<List<TransactionEntity>>
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>
    suspend fun getTransactionsByCategory(categoryId: Long): List<TransactionEntity>
    suspend fun addTransaction(transaction: TransactionEntity)
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(transaction: TransactionEntity)

}