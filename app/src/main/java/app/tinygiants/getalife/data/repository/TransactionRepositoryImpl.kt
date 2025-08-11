package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.TransactionDao
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.data.remote.FirestoreDataSource
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.budget.GetCurrentBudgetUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Instant

/**
 * Offline-first repository for Transactions.
 * Room = Single Source of Truth, Firestore = automatic sync layer.
 */
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val getCurrentBudget: GetCurrentBudgetUseCase,
    private val firestore: FirestoreDataSource,
    private val externalScope: CoroutineScope
) : TransactionRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getTransactionsFlow(): Flow<List<Transaction>> =
        getCurrentBudget.currentBudgetIdOrDefaultFlow.flatMapLatest { budgetId ->
            transactionDao.getAllTransactions(budgetId).map { transactionEntities ->
                transactionEntities.mapNotNull { transactionEntity ->
                    val account = accountRepository.getAccount(transactionEntity.accountId) ?: return@mapNotNull null
                    val category = transactionEntity.categoryId?.let { categoryId -> categoryRepository.getCategory(categoryId) }
                    transactionEntity.toDomain(account = account, category = category)
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getTransactionsByAccountFlow(accountId: Long): Flow<List<Transaction>> =
        getCurrentBudget.currentBudgetIdOrDefaultFlow.flatMapLatest { budgetId ->
            transactionDao.getAccountTransactionsFlow(accountId, budgetId).map { transactionEntities ->
                transactionEntities.mapNotNull { transactionEntity ->
                    val account = accountRepository.getAccount(accountId) ?: return@mapNotNull null
                    val category = transactionEntity.categoryId?.let { categoryId -> categoryRepository.getCategory(categoryId) }
                    transactionEntity.toDomain(account = account, category = category)
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getTransactionsByCategoryFlow(categoryId: Long): Flow<List<Transaction>> =
        getCurrentBudget.currentBudgetIdOrDefaultFlow.flatMapLatest { budgetId ->
            transactionDao.getCategoryTransactionsFlow(categoryId, budgetId).map { transactionEntities ->
                transactionEntities.mapNotNull { transactionEntity ->
                    val account = accountRepository.getAccount(transactionEntity.accountId) ?: return@mapNotNull null
                    val category = categoryRepository.getCategory(categoryId)
                    transactionEntity.toDomain(account = account, category = category)
                }
            }
        }

    override suspend fun getTransaction(transactionId: Long): Transaction? {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val transactionEntity = transactionDao.getTransaction(transactionId, budgetId)
        val account = accountRepository.getAccount(transactionEntity.accountId) ?: return null
        val category = transactionEntity.categoryId?.let { categoryRepository.getCategory(it) }
        return transactionEntity.toDomain(account = account, category = category)
    }

    override suspend fun addTransaction(transaction: Transaction) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entity = TransactionEntity.fromDomain(transaction.also {
            // Validate recurring payment data integrity at repository level
            if (it.isRecurring && it.recurrenceFrequency == null) {
                throw IllegalStateException("Cannot save recurring transaction without frequency")
            }
        }, budgetId)
        transactionDao.addTransaction(entity.copy(isSynced = false))
        syncTransactionInBackground(entity)
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entity = TransactionEntity.fromDomain(transaction.also {
            // Validate recurring payment data integrity at repository level
            if (it.isRecurring && it.recurrenceFrequency == null) {
                throw IllegalStateException("Cannot update recurring transaction without frequency")
            }
        }, budgetId)
        transactionDao.updateTransaction(entity.copy(isSynced = false))
        syncTransactionInBackground(entity)
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entity = TransactionEntity.fromDomain(transaction, budgetId)
        transactionDao.deleteTransaction(entity)
        // TODO: firestore deletion
    }

    override fun getSpentAmountByCategoryAndMonthFlow(
        categoryId: Long,
        yearMonth: YearMonth
    ): Flow<Money> {
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
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val monthNumber = (yearMonth.month.ordinal + 1).toString().padStart(2, '0')
        val yearMonthString = "${yearMonth.year}-$monthNumber"
        val transactionEntities = transactionDao.getCategoryTransactionsForMonth(categoryId, budgetId, yearMonthString)

        val totalSpent = transactionEntities
            .filter { entity -> entity.transactionDirection.name == "Outflow" }
            .sumOf { entity -> entity.amount }

        return Money(totalSpent)
    }

    private fun isTransactionInMonth(instant: Instant, yearMonth: YearMonth): Boolean {
        // Convert instant to LocalDateTime and check if it's in the target month
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDateTime.year == yearMonth.year && localDateTime.month == yearMonth.month
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getActiveRecurringTransactions(): Flow<List<Transaction>> =
        getCurrentBudget.currentBudgetIdOrDefaultFlow.flatMapLatest { budgetId ->
            transactionDao.getActiveRecurringTransactions(budgetId).map { list ->
                list.mapNotNull { entity ->
                    val account = accountRepository.getAccount(entity.accountId) ?: return@mapNotNull null
                    val category = entity.categoryId?.let { categoryRepository.getCategory(it) }
                    entity.toDomain(account = account, category = category)
                }
            }
        }

    override suspend fun getDueRecurringTransactions(currentDate: Instant): List<Transaction> {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entities = transactionDao.getDueRecurringTransactions(currentDate, budgetId)
        return entities.mapNotNull { entity ->
            val account = accountRepository.getAccount(entity.accountId) ?: return@mapNotNull null
            val category = entity.categoryId?.let { categoryRepository.getCategory(it) }
            entity.toDomain(account = account, category = category)
        }
    }

    override suspend fun updateRecurrenceStatus(transactionId: Long, isActive: Boolean) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        transactionDao.updateRecurrenceStatus(transactionId, budgetId, isActive)
    }

    override suspend fun updateNextPaymentDate(transactionId: Long, nextDate: Instant) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        transactionDao.updateNextPaymentDate(transactionId, budgetId, nextDate)
    }

    private fun syncTransactionInBackground(entity: TransactionEntity) {
        externalScope.async {
            try {
                firestore.saveTransaction(entity)
                // Mark as synced in local database
                transactionDao.updateTransaction(entity.copy(isSynced = true))
            } catch (_: Exception) {
                // Firestore handles offline persistence automatically
            }
        }
    }

    suspend fun syncWithFirestore(budgetId: String) {
        try {
            val remote = firestore.getTransactions(budgetId)
            val local = transactionDao.getAllTransactions(budgetId).first()

            remote.forEach { remoteTransaction ->
                val localTransaction = local.find { it.id == remoteTransaction.id }
                if (localTransaction == null || !localTransaction.isSynced) {
                    transactionDao.updateTransaction(remoteTransaction.copy(isSynced = true))
                }
            }
        } catch (_: Exception) {
            // Handle gracefully - local remains source of truth
        }
    }
}