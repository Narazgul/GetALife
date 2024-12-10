package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getTransactionsFlow(): Flow<List<Transaction>>
    fun getTransactionsByAccountFlow(accountId: Long): Flow<List<Transaction>>
    fun getTransactionsByCategoryFlow(categoryId: Long): Flow<List<Transaction>>
    suspend fun getTransaction(transactionId: Long): Transaction?
    suspend fun addTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)

}