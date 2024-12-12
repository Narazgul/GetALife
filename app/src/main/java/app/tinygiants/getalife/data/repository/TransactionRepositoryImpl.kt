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
            transactionEntities.mapNotNull { transactionEntity ->
                val account = accountRepository.getAccount(transactionEntity.accountId) ?: return@mapNotNull null
                val category = transactionEntity.categoryId?.let { categoryId -> categoryRepository.getCategory(categoryId) }
                transactionEntity.toDomain(account = account, category = category)
            }
        }

    override fun getTransactionsByAccountFlow(accountId: Long) = transactionDao.getAccountTransactionsFlow(accountId)
        .map { transactionEntities ->
            transactionEntities.mapNotNull { transactionEntity ->
                val account = accountRepository.getAccount(accountId) ?: return@mapNotNull null
                val category = transactionEntity.categoryId?.let { categoryId -> categoryRepository.getCategory(categoryId) }
                transactionEntity.toDomain(account = account, category = category)
            }
        }

    override fun getTransactionsByCategoryFlow(categoryId: Long) = transactionDao.getCategoryTransactionsFlow(categoryId)
        .map { transactionEntities ->
            transactionEntities.mapNotNull { transactionEntity ->
                val account = accountRepository.getAccount(transactionEntity.accountId) ?: return@mapNotNull null
                val category = categoryRepository.getCategory(categoryId)
                transactionEntity.toDomain(account = account, category = category)
            }
        }

    override suspend fun getTransaction(transactionId: Long): Transaction? {
        val transactionEntity = transactionDao.getTransaction(transactionId)
        val account = accountRepository.getAccount(transactionEntity.accountId) ?: return null
        val category = transactionEntity.categoryId?.let { categoryRepository.getCategory(it) }
        return transactionEntity.toDomain(account = account, category = category)
    }

    override suspend fun addTransaction(transaction: Transaction) =
        transactionDao.addTransaction(transaction = TransactionEntity.fromDomain(transaction))

    override suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(TransactionEntity.fromDomain(transaction))

    override suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(TransactionEntity.fromDomain(transaction))
}