package app.tinygiants.getalife.data.local.dao

import app.tinygiants.getalife.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class TransactionDaoFake : TransactionDao {

    private val transactionsFlow = MutableStateFlow<List<TransactionEntity>>(emptyList())

    override fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionsFlow

    override fun getAccountTransactionsFlow(accountId: Long): Flow<List<TransactionEntity>> =
        transactionsFlow.map { transactions ->
            transactions.filter { it.accountId == accountId }
        }

    override fun getCategoryTransactionsFlow(categoryId: Long): Flow<List<TransactionEntity>> =
        transactionsFlow.map { transactions ->
            transactions.filter { it.categoryId == categoryId }
        }

    override suspend fun getCategoryTransactions(categoryId: Long): List<TransactionEntity> =
        transactionsFlow.value.filter { it.categoryId == categoryId }

    override suspend fun getCategoryTransactions(
        categoryId: Long,
        startTime: Instant,
        endTime: Instant
    ): List<TransactionEntity> {
        return transactionsFlow.value.filter {
            it.categoryId == categoryId && it.timestamp >= startTime && it.timestamp <= endTime
        }
    }

    override suspend fun getTransaction(transactionId: Long): TransactionEntity =
        transactionsFlow.value.first { it.id == transactionId }

    override suspend fun addTransaction(transaction: TransactionEntity) {
        val updatedTransactions = transactionsFlow.value.toMutableList()
        updatedTransactions.add(transaction)
        transactionsFlow.value = updatedTransactions
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        val updatedTransactions = transactionsFlow.value.toMutableList()
        val index = updatedTransactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            updatedTransactions[index] = transaction
            transactionsFlow.value = updatedTransactions
        }
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        val updatedTransactions = transactionsFlow.value.toMutableList()
        updatedTransactions.removeIf { it.id == transaction.id }
        transactionsFlow.value = updatedTransactions
    }
}