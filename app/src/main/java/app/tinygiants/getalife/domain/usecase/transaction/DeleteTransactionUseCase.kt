package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.budget.RecalculateCategoryMonthlyStatusUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val recalculateCategoryMonthlyStatus: RecalculateCategoryMonthlyStatusUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(transaction: Transaction) {

        withContext(defaultDispatcher) {
            deleteTransaction(transaction = transaction)

            if (transaction.transactionDirection == TransactionDirection.AccountTransfer) {
                // Special handling for account transfers - need to revert both accounts
                handleAccountTransferDeletion(transaction)
            } else {
                // Regular transaction handling
                val transformedAmount = transformAmount(direction = transaction.transactionDirection, amount = transaction.amount)
                val transactionWithTransformedAmount = transaction.copy(amount = transformedAmount)
                updateAccount(transaction = transactionWithTransformedAmount)
                // Removed updateCategory call as it's already handled by RecalculateCategoryMonthlyStatusUseCase
            }

            // Trigger recalculation for the affected category and month
            transaction.category?.let { category ->
                val transactionMonth =
                    transaction.dateOfTransaction.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                val yearMonth = kotlinx.datetime.YearMonth(transactionMonth.year, transactionMonth.month)
                recalculateCategoryMonthlyStatus(category.id, yearMonth)
            }
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

    private suspend fun handleAccountTransferDeletion(transaction: Transaction) {
        // For AccountTransfer: transaction.account is the "from" account, we need to find the "to" account
        val fromAccount = transaction.account
        val transferAmount = transaction.amount

        // Find the "to" account from the transaction partner description
        // Format: "Transfer to {AccountName}"
        val toAccountName = transaction.transactionPartner.removePrefix("Transfer to ")
        val allAccounts = accountRepository.getAccountsFlow().first()
        val toAccount = allAccounts.find { it.name == toAccountName }

        toAccount?.let { targetAccount ->
            // Revert fromAccount: add money back (it was subtracted during transfer)
            val updatedFromAccount = fromAccount.copy(
                balance = fromAccount.balance + transferAmount,
                updatedAt = Clock.System.now()
            )
            accountRepository.updateAccount(updatedFromAccount)

            // Revert toAccount: subtract money back (it was added during transfer)
            val updatedToAccount = targetAccount.copy(
                balance = targetAccount.balance - transferAmount,
                updatedAt = Clock.System.now()
            )
            accountRepository.updateAccount(updatedToAccount)
        }
    }
}