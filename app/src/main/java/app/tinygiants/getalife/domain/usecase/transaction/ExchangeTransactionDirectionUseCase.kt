package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Clock

class ExchangeTransactionDirectionUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(transaction: Transaction) {

        if (transaction.transactionDirection == TransactionDirection.Outflow && transaction.category == null) return

        withContext(defaultDispatcher) {

            updateCategory(transaction = transaction)
            updateAccount(transaction = transaction)
            updateTransaction(transaction = transaction)
        }
    }

    private suspend fun updateCategory(transaction: Transaction) {
        val category = transaction.category ?: return

        val updatedAvailableMoney = category.availableMoney - transaction.amount

        val updatedCategory = category.copy(
            availableMoney = updatedAvailableMoney,
            updatedAt = Clock.System.now()
        )

        categoryRepository.updateCategory(updatedCategory)
    }

    private suspend fun updateAccount(transaction: Transaction) {

        val account = transaction.account
        val updatedBalance = account.balance - transaction.amount

        val updatedAccount = account.copy(
            balance = updatedBalance,
            updatedAt = Clock.System.now()
        )

        accountRepository.updateAccount(updatedAccount)
    }

    private suspend fun updateTransaction(transaction: Transaction) {

        val transformedAmount = transformAmount(direction = transaction.transactionDirection, amount = transaction.amount)

        val updatedTransaction = transaction.copy(
            amount = transformedAmount,
            updatedAt = Clock.System.now()
        )

        transactionRepository.updateTransaction(updatedTransaction)
    }

    private fun transformAmount(direction: TransactionDirection, amount: Money) =
        if (direction == TransactionDirection.Inflow) amount.positiveMoney()
        else amount.negativeMoney()
}