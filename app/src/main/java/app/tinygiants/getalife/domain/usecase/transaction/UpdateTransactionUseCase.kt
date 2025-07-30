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

class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(transaction: Transaction) {

        withContext(defaultDispatcher) {

            val transformedAmount = transformAmount(direction = transaction.transactionDirection, amount = transaction.amount)
            val transformedTransaction = transaction.copy(amount = transformedAmount)

            updateAccount(transaction = transformedTransaction)
            updateCategory(transaction = transformedTransaction)
            updateTransaction(transaction = transformedTransaction)
        }
    }

    private suspend fun updateTransaction(transaction: Transaction) = transactionRepository.updateTransaction(transaction)

    private suspend fun updateAccount(transaction: Transaction) {

        val updatedBalance = calculateUpdatedBalance(
            transaction = transaction,
            toBeUpdatedBalance = transaction.account.balance
        )

        val updatedAccount = transaction.account.copy(
            balance = updatedBalance,
            updatedAt = Clock.System.now()
        )

        accountRepository.updateAccount(updatedAccount)
    }

    private suspend fun updateCategory(transaction: Transaction) {
        // TODO: Category-Update muss auf MonthlyBudget umgestellt werden
    }

    private suspend fun calculateUpdatedBalance(
        transaction: Transaction,
        toBeUpdatedBalance: Money
    ): Money {

        val previousTransactionAmount =
            transactionRepository.getTransaction(transactionId = transaction.id)?.amount ?: return toBeUpdatedBalance
        val differenceBetweenCurrentAndPreviousTransactionAmount = transaction.amount - previousTransactionAmount

        return toBeUpdatedBalance + differenceBetweenCurrentAndPreviousTransactionAmount
    }

    private fun transformAmount(direction: TransactionDirection, amount: Money) =
        when (direction) {
            TransactionDirection.Inflow -> amount.positiveMoney()
            TransactionDirection.Outflow -> amount.negativeMoney()
            else -> amount
        }
}