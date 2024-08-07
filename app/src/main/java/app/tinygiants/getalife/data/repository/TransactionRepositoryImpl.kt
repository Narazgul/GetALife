package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.TransactionDao
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(private val transactionDao: TransactionDao) :
    TransactionRepository {

    override fun getTransactions(): Flow<Result<List<TransactionEntity>>> = flow {
        transactionDao.getAllTransactionsFlow()
            .catch { exception -> emit(Result.failure(exception)) }
            .collect { transactions -> emit(Result.success(transactions)) }
    }

    override fun getTransactionsByAccountFlow(accountId: Long): Flow<Result<List<TransactionEntity>>> = flow {
        transactionDao.getAccountTransactionsFlow(accountId = accountId)
            .catch { exception -> emit(Result.failure(exception)) }
            .collect { transactions -> emit(Result.success(transactions)) }
    }

    override suspend fun getTransactionsByCategory(categoryId: Long): List<TransactionEntity> =
        transactionDao.getCategoryTransactions(categoryId = categoryId)

    override suspend fun addTransaction(transaction: TransactionEntity) =
        transactionDao.addTransaction(transaction = transaction)

    override suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction = transaction)

    override suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction = transaction)
}