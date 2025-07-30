package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.TransactionDao
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.map
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
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

    override suspend fun getSpentAmountByCategoryAndMonth(categoryId: Long, yearMonth: YearMonth): Money {
        // Return sample spending data for demonstration
        // In a real app, this would query the database for actual transactions
        return when (categoryId) {
            101L -> Money(800.0)  // Rent - partially paid
            102L -> Money(120.0)  // Utilities - mostly paid
            103L -> Money(280.0)  // Groceries - in progress
            201L -> Money(250.0)  // Dining Out - over budget
            202L -> Money(45.0)   // Entertainment - under budget
            203L -> Money(85.0)   // Transportation - close to budget
            301L -> Money(0.0)    // Vacation Fund - saving, no spending
            302L -> Money(0.0)    // Emergency Fund - saving, no spending
            else -> EmptyMoney() // Other categories - no spending
        }
    }

    private fun isInMonth(instant: kotlin.time.Instant, yearMonth: YearMonth): Boolean {
        // Convert instant to LocalDateTime and check if it's in the target month
        val localDateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        return localDateTime.year == yearMonth.year && localDateTime.month == yearMonth.month
    }
}