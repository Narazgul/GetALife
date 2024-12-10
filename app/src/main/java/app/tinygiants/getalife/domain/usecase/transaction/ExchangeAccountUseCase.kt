package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject

class ExchangeAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(transaction: Transaction, oldAccount: Account?) {

        if (transaction.account.id == oldAccount?.id) return

        withContext(defaultDispatcher) {

            updateOldAccount(oldAccount = oldAccount, newValue = transaction.amount)
            updateNewAccount(transaction = transaction)
            updateTransaction(transaction = transaction)
        }
    }

    private suspend fun updateOldAccount(oldAccount: Account?, newValue: Money) {
        if (oldAccount == null) return

        val updatedBalance = oldAccount.balance - newValue
        val updatedOldAccount = oldAccount.copy(
            balance = updatedBalance,
            updatedAt = Clock.System.now()
        )

        accountRepository.updateAccount(updatedOldAccount)
    }

    private suspend fun updateNewAccount(transaction: Transaction) {
        val account = transaction.account
        val updatedBalance = account.balance + transaction.amount

        val updatedAccount = account.copy(
            balance = updatedBalance,
            updatedAt = Clock.System.now()
        )

        accountRepository.updateAccount(updatedAccount)
    }

    private suspend fun updateTransaction(transaction: Transaction) {

        val updatedTransaction = transaction.copy(
            updatedAt = Clock.System.now()
        )

        transactionRepository.updateTransaction(updatedTransaction)
    }
}