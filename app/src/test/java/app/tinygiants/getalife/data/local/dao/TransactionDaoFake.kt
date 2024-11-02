package app.tinygiants.getalife.data.local.dao

import app.tinygiants.getalife.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class TransactionDaoFake : TransactionDao {

    val transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())

    override fun getAllTransactions(): Flow<List<TransactionEntity>> = transactions

    override fun getAccountTransactionsFlow(accountId: Long): Flow<List<TransactionEntity>> =
        transactions.map { transactions ->
            transactions.filter { it.accountId == accountId }
        }

    override fun getCategoryTransactionsFlow(categoryId: Long): Flow<List<TransactionEntity>> =
        transactions.map { transactions ->
            transactions.filter { it.categoryId == categoryId }
        }

    override suspend fun getCategoryTransactions(
        categoryId: Long,
        startTime: Instant,
        endTime: Instant
    ): List<TransactionEntity> {
        return transactions.value.filter {
            it.categoryId == categoryId && it.createdAt >= startTime && it.createdAt <= endTime
        }
    }

    override suspend fun getTransaction(transactionId: Long): TransactionEntity =
        transactions.value.first { it.id == transactionId }

    override suspend fun addTransaction(transaction: TransactionEntity) {
        val updatedTransactions = transactions.value.toMutableList()
        updatedTransactions.add(transaction)
        transactions.value = updatedTransactions
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        val updatedTransactions = transactions.value.toMutableList()
        val index = updatedTransactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            updatedTransactions[index] = transaction
            transactions.value = updatedTransactions
        }
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        val updatedTransactions = transactions.value.toMutableList()
        updatedTransactions.removeIf { it.id == transaction.id }
        transactions.value = updatedTransactions
    }
}