package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.TransactionDao
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : TransactionRepository {

    override fun getTransactionsFlow() = transactionDao.getAllTransactions()
        .map { transactionEntities ->
            transactionEntities.map { transactionEntity ->
                val account = accountRepository.getAccount(transactionEntity.accountId)
                val category = transactionEntity.categoryId?.let { categoryRepository.getCategory(it) }
                transactionEntity.toDomain(account = account, category = category)
            }
        }

    override fun getTransactionsByAccountFlow(accountId: Long) = transactionDao.getAccountTransactionsFlow(accountId)
        .map { transactionEntities ->
            val account = accountRepository.getAccount(accountId)
            transactionEntities.map { entity ->
                val category = entity.categoryId?.let { categoryRepository.getCategory(it) }
                entity.toDomain(account = account, category = category)
            }
        }

    override fun getTransactionsByCategoryFlow(categoryId: Long) = transactionDao.getCategoryTransactionsFlow(categoryId)
        .map { transactionEntities ->
            val category = categoryRepository.getCategory(categoryId)
            transactionEntities.map { entity ->
                val account = accountRepository.getAccount(entity.accountId)
                entity.toDomain(account = account, category = category)
            }
        }

    override suspend fun getTransaction(transactionId: Long): Transaction {
        val entity = transactionDao.getTransaction(transactionId)
        val account = accountRepository.getAccount(entity.accountId)
        val category = entity.categoryId?.let { categoryRepository.getCategory(it) }
        return entity.toDomain(account = account, category = category)
    }

    override suspend fun addTransaction(transaction: Transaction) =
        transactionDao.addTransaction(transaction = TransactionEntity.fromDomain(transaction))

    override suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(TransactionEntity.fromDomain(transaction))

    override suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(TransactionEntity.fromDomain(transaction))
}