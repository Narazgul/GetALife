package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Clock

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        accountId: Long,
        category: Category?,
        amount: Money,
        direction: TransactionDirection,
        transactionPartner: String,
        description: String
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
                description = description
            )
            updateAccount(account = account, amount = transformedAmount)
            updateCategory(category = category, amount = transformedAmount)
        }
    }

    private fun transformAmount(direction: TransactionDirection, amount: Money): Money {
        val transformedValue = when (direction) {
            TransactionDirection.Inflow -> abs(amount.asDouble())
            TransactionDirection.Outflow -> -abs(amount.asDouble())
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
        description: String
    ) {
        val currentTime = Clock.System.now()
        val transaction = Transaction(
            id = Random.nextLong(),
            amount = amount,
            account = account,
            category = category,
            transactionPartner = transactionPartner,
            transactionDirection = direction,
            description = description,
            dateOfTransaction = currentTime,
            updatedAt = currentTime,
            createdAt = currentTime
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

    private suspend fun updateCategory(
        category: Category?,
        amount: Money
    ) {
        // TODO: Category-Update muss auf MonthlyBudget umgestellt werden
        // Aktuell wird die Kategorie nicht mehr direkt updated
    }
}