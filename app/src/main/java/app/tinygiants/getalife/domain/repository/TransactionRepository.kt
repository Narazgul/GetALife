package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

interface TransactionRepository {

    fun getTransactionsFlow(): Flow<List<Transaction>>
    fun getTransactionsByAccountFlow(accountId: Long): Flow<List<Transaction>>
    fun getTransactionsByCategoryFlow(categoryId: Long): Flow<List<Transaction>>
    fun getSpentAmountByCategoryAndMonthFlow(categoryId: Long, yearMonth: YearMonth): Flow<Money>
    suspend fun getTransaction(transactionId: Long): Transaction?
    suspend fun addTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun getSpentAmountByCategoryAndMonth(categoryId: Long, yearMonth: YearMonth): Money

    // Recurring payment methods
    fun getActiveRecurringTransactions(): Flow<List<Transaction>>
    suspend fun getDueRecurringTransactions(currentDate: kotlin.time.Instant): List<Transaction>
    suspend fun updateRecurrenceStatus(transactionId: Long, isActive: Boolean)
    suspend fun updateNextPaymentDate(transactionId: Long, nextDate: kotlin.time.Instant)

}