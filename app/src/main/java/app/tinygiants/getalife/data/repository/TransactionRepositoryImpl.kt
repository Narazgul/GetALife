package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.TransactionDao
import app.tinygiants.getalife.data.local.entities.TransactionEntity
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
        transactionDao.addTransaction(transaction = TransactionEntity.fromDomain(transaction.also {
            // Validate recurring payment data integrity at repository level
            if (it.isRecurring && it.recurrenceFrequency == null) {
                throw IllegalStateException("Cannot save recurring transaction without frequency")
            }
        }))

    override suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(TransactionEntity.fromDomain(transaction.also {
            // Validate recurring payment data integrity at repository level
            if (it.isRecurring && it.recurrenceFrequency == null) {
                throw IllegalStateException("Cannot update recurring transaction without frequency")
            }
        }))

    override suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(TransactionEntity.fromDomain(transaction))

    override fun getSpentAmountByCategoryAndMonthFlow(
        categoryId: Long,
        yearMonth: YearMonth
    ): kotlinx.coroutines.flow.Flow<Money> {
        return getTransactionsByCategoryFlow(categoryId).map { transactions ->
            val totalSpent = transactions
                .filter { transaction ->
                    transaction.transactionDirection.name == "Outflow" &&
                            isTransactionInMonth(transaction.dateOfTransaction, yearMonth)
                }
                .sumOf { transaction -> transaction.amount.asDouble() }

            Money(totalSpent)
        }
    }

    override suspend fun getSpentAmountByCategoryAndMonth(categoryId: Long, yearMonth: YearMonth): Money {
        val monthNumber = (yearMonth.month.ordinal + 1).toString().padStart(2, '0')
        val yearMonthString = "${yearMonth.year}-$monthNumber"
        val transactionEntities = transactionDao.getCategoryTransactionsForMonth(categoryId, yearMonthString)

        val totalSpent = transactionEntities
            .filter { entity -> entity.transactionDirection.name == "Outflow" }
            .sumOf { entity -> entity.amount }

        return Money(totalSpent)
    }

    private fun isTransactionInMonth(instant: kotlin.time.Instant, yearMonth: YearMonth): Boolean {
        // Convert instant to LocalDateTime and check if it's in the target month
        val localDateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        return localDateTime.year == yearMonth.year && localDateTime.month == yearMonth.month
    }

    override fun getActiveRecurringTransactions() = transactionDao.getActiveRecurringTransactions().map { list ->
        list.mapNotNull { entity ->
            val account = accountRepository.getAccount(entity.accountId) ?: return@mapNotNull null
            val category = entity.categoryId?.let { categoryRepository.getCategory(it) }
            entity.toDomain(account = account, category = category)
        }
    }

    override suspend fun getDueRecurringTransactions(currentDate: kotlin.time.Instant): List<Transaction> {
        val entities = transactionDao.getDueRecurringTransactions(currentDate)
        return entities.mapNotNull { entity ->
            val account = accountRepository.getAccount(entity.accountId) ?: return@mapNotNull null
            val category = entity.categoryId?.let { categoryRepository.getCategory(it) }
            entity.toDomain(account = account, category = category)
        }
    }

    override suspend fun updateRecurrenceStatus(transactionId: Long, isActive: Boolean) =
        transactionDao.updateRecurrenceStatus(transactionId, isActive)

    override suspend fun updateNextPaymentDate(transactionId: Long, nextDate: kotlin.time.Instant) =
        transactionDao.updateNextPaymentDate(transactionId, nextDate)
}