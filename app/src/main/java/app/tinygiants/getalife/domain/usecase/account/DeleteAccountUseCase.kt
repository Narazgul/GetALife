package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.account.DeleteAccountStatus.AccountClosedInsteadOfDeleted
import app.tinygiants.getalife.domain.usecase.account.DeleteAccountStatus.SuccessfullyDeleted
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Clock

sealed class DeleteAccountStatus {
    data object SuccessfullyDeleted : DeleteAccountStatus()
    data object AccountClosedInsteadOfDeleted : DeleteAccountStatus()
}

class DeleteAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(account: Account): Result<DeleteAccountStatus> {
        val transactions = transactionRepository.getTransactionsByAccountFlow(accountId = account.id).first()

        return when {
            // Credit card accounts with zero balance and no transactions can be deleted
            account.type == AccountType.CreditCard &&
                    account.balance.asDouble() == 0.0 &&
                    transactions.isEmpty() -> {
                handleCreditCardAccountDeletion(account)
                Result.success(SuccessfullyDeleted)
            }

            // Credit card accounts with non-zero balance need balance transfer
            account.type == AccountType.CreditCard &&
                    account.balance.asDouble() != 0.0 -> {
                handleCreditCardAccountWithBalance(account)
                Result.success(AccountClosedInsteadOfDeleted)
            }

            // Regular accounts with no transactions can be deleted
            transactions.isEmpty() -> {
                accountRepository.deleteAccount(account = account)
                Result.success(SuccessfullyDeleted)
            }

            // Accounts with transactions should be closed instead of deleted
            else -> {
                // Create balance transfer if account has non-zero balance
                if (account.balance.asDouble() != 0.0) {
                    createBalanceTransferTransaction(
                        account = account,
                        transferAmount = account.balance,
                        reason = "Account closure balance transfer"
                    )
                }

                // Close the account by marking it as closed
                closeAccount(account)
                Result.success(AccountClosedInsteadOfDeleted)
            }
        }
    }

    /**
     * Handles deletion of credit card accounts at zero balance.
     * Also removes the associated credit card payment category.
     */
    private suspend fun handleCreditCardAccountDeletion(account: Account) {
        // Delete the credit card payment category first
        val creditCardCategory = categoryRepository.getCreditCardPaymentCategory(account.id)
        creditCardCategory?.let { category ->
            categoryRepository.deleteCategory(category)
        }

        // Then delete the account
        accountRepository.deleteAccount(account)
    }

    /**
     * Handles credit card accounts with non-zero balance.
     * Creates balance transfer transactions and closes the account.
     */
    private suspend fun handleCreditCardAccountWithBalance(account: Account) {
        val balance = account.balance.asDouble()

        when {
            // Scenario A: Negative balance (credit card debt) -> Inflow to assignable money
            balance < 0 -> {
                createBalanceTransferTransaction(
                    account = account,
                    transferAmount = Money(-balance), // Convert negative to positive inflow
                    reason = "Credit card debt settlement"
                )
            }

            // Scenario B: Positive balance (credit on card) -> Outflow from assignable money
            balance > 0 -> {
                createBalanceTransferTransaction(
                    account = account,
                    transferAmount = Money(-balance), // Convert to negative outflow
                    reason = "Credit card balance withdrawal"
                )
            }
        }

        // Delete the credit card payment category
        val creditCardCategory = categoryRepository.getCreditCardPaymentCategory(account.id)
        creditCardCategory?.let { category ->
            categoryRepository.deleteCategory(category)
        }

        // Close the account
        closeAccount(account)
    }

    /**
     * Creates a balance transfer transaction when closing accounts with non-zero balances.
     * Positive balances become inflows to assignable money.
     * Negative balances become outflows from assignable money.
     */
    private suspend fun createBalanceTransferTransaction(
        account: Account,
        transferAmount: Money,
        reason: String = "Account closure balance transfer"
    ) {
        val transactionDirection = if (transferAmount.asDouble() >= 0) {
            TransactionDirection.Inflow
        } else {
            TransactionDirection.Outflow
        }

        val transaction = Transaction(
            id = abs(Random.nextLong()),
            amount = Money(abs(transferAmount.asDouble())),
            account = account,
            category = null, // Balance transfers go to assignable money (no category)
            transactionPartner = "Balance Transfer",
            transactionDirection = transactionDirection,
            description = reason,
            dateOfTransaction = Clock.System.now(),
            updatedAt = Clock.System.now(),
            createdAt = Clock.System.now()
        )

        transactionRepository.addTransaction(transaction)
    }

    /**
     * Closes an account by setting isClosed to true.
     * Closed accounts remain in the database but are excluded from active operations.
     */
    private suspend fun closeAccount(account: Account) {
        val closedAccount = account.copy(
            isClosed = true,
            updatedAt = Clock.System.now()
        )

        accountRepository.updateAccount(closedAccount)
    }
}