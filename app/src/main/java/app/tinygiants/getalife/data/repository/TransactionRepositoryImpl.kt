package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.TransactionDao
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.domain.repository.TransactionRepository
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(private val transactionDao: TransactionDao) : TransactionRepository {

    override fun getTransactions() = transactionDao.getAllTransactions()
    override fun getTransactionsByAccount(accountId: Long) = transactionDao.getAccountTransactionsFlow(accountId = accountId)
    override suspend fun getTransactionsByCategory(categoryId: Long): List<TransactionEntity> = transactionDao.getCategoryTransactions(categoryId = categoryId)
    override suspend fun addTransaction(transaction: TransactionEntity) = transactionDao.addTransaction(transaction = transaction)
    override suspend fun updateTransaction(transaction: TransactionEntity) = transactionDao.updateTransaction(transaction = transaction)
    override suspend fun deleteTransaction(transaction: TransactionEntity) = transactionDao.deleteTransaction(transaction = transaction)

}