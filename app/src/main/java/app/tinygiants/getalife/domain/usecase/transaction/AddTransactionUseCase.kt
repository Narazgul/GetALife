package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.budget.RecalculateCategoryMonthlyStatusUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Clock

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val recalculateCategoryMonthlyStatus: RecalculateCategoryMonthlyStatusUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        accountId: Long,
        category: Category?,
        amount: Money,
        direction: TransactionDirection,
        transactionPartner: String,
        description: String,
        dateOfTransaction: kotlin.time.Instant = Clock.System.now()
    ) {
        val account = accountRepository.getAccount(accountId) ?: return
        if (direction == TransactionDirection.Unknown) return

        withContext(defaultDispatcher) {

            val transformedAmount = transformAmount(direction = direction, amount = amount)
            addTransaction(
                account = account,
                category = category,
                amount = transformedAmount,
                direction = direction,
                transactionPartner = transactionPartner,
                description = description,
                dateOfTransaction = dateOfTransaction
            )
            updateAccount(account = account, amount = transformedAmount)

            // Trigger recalculation for the affected category and month
            category?.let { cat ->
                val transactionMonth = dateOfTransaction.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                val yearMonth = kotlinx.datetime.YearMonth(transactionMonth.year, transactionMonth.month)
                recalculateCategoryMonthlyStatus(cat.id, yearMonth)
            }
        }
    }

    private fun transformAmount(direction: TransactionDirection, amount: Money): Money {
        val transformedValue = when (direction) {
            TransactionDirection.Inflow -> abs(amount.asDouble())
            TransactionDirection.Outflow -> -abs(amount.asDouble())
            TransactionDirection.AccountTransfer -> amount.asDouble() // Keep original amount for transfers
            else -> amount.asDouble()
        }
        return Money(value = transformedValue)
    }

    private suspend fun addTransaction(
        account: Account,
        category: Category?,
        amount: Money,
        direction: TransactionDirection,
        transactionPartner: String,
        description: String,
        dateOfTransaction: kotlin.time.Instant
    ) {
        val transaction = Transaction(
            id = abs(Random.nextLong()),
            amount = amount,
            account = account,
            category = category,
            transactionPartner = transactionPartner,
            transactionDirection = direction,
            description = description,
            dateOfTransaction = dateOfTransaction,
            updatedAt = Clock.System.now(),
            createdAt = Clock.System.now()
        )

        transactionRepository.addTransaction(transaction = transaction)
    }

    private suspend fun updateAccount(account: Account, amount: Money) {
        val updatedBalance = account.balance + amount

        val updatedAccount = account.copy(
            balance = updatedBalance,
            updatedAt = Clock.System.now()
        )

        accountRepository.updateAccount(account = updatedAccount)
    }
}