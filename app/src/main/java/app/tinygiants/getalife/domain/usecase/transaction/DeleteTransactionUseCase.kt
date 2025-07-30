package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.di.Default
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
import kotlin.time.Clock

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(transaction: Transaction) {

        val transformedAmount = transformAmount(direction = transaction.transactionDirection, amount = transaction.amount)
        val transactionWithTransformedAmount = transaction.copy(amount = transformedAmount)

        withContext(defaultDispatcher) {
            deleteTransaction(transaction = transactionWithTransformedAmount)
            updateAccount(transaction = transactionWithTransformedAmount)
            updateCategory(transaction = transactionWithTransformedAmount)
        }
    }

    private fun transformAmount(direction: TransactionDirection, amount: Money) =
        when (direction) {
            TransactionDirection.Inflow -> amount.negativeMoney()
            TransactionDirection.Outflow -> amount.positiveMoney()
            else -> amount
        }

    private suspend fun deleteTransaction(transaction: Transaction) = transactionRepository.deleteTransaction(transaction)

    private suspend fun updateAccount(transaction: Transaction) {

        val account = transaction.account
        val updatedAccountBalance = account.balance + transaction.amount

        val updatedAccount = account.copy(
            balance = updatedAccountBalance,
            updatedAt = Clock.System.now()
        )

        accountRepository.updateAccount(updatedAccount)
    }

    private suspend fun updateCategory(transaction: Transaction) {
        updateCategory(transaction.category, transaction.amount)
    }

    private suspend fun updateCategory(category: Category?, amount: Money) {
        // TODO: Category-Update muss auf MonthlyBudget umgestellt werden
    }
}