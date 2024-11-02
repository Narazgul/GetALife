package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TransactionRepositoryFake : TransactionRepository {

    val transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())

    override fun getTransactions(): Flow<List<TransactionEntity>> = transactions

    override fun getTransactionsByAccount(accountId: Long) =
        transactions.map { it.filter { transaction -> transaction.accountId == accountId } }

    override suspend fun getTransactionsByCategory(categoryId: Long) =
        transactions.map { it.filter { transaction -> transaction.categoryId == categoryId } }

    override suspend fun addTransaction(transaction: TransactionEntity) =
        transactions.update { it + transaction }

    override suspend fun updateTransaction(transaction: TransactionEntity) =
        transactions.update { current ->
            current.map { if (it.id == transaction.id) transaction else it }
        }

    override suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactions.update { it.filterNot { it.id == transaction.id } }
}