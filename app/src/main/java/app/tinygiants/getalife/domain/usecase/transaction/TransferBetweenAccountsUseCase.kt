package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Clock

class TransferBetweenAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        fromAccount: Account,
        toAccount: Account,
        amount: Money,
        description: String = "Account Transfer"
    ) {
        if (fromAccount.id == toAccount.id) return
        if (amount.asDouble() <= 0) return

        withContext(defaultDispatcher) {
            try {
                // Update fromAccount (subtract money)
                val updatedFromAccount = fromAccount.copy(
                    balance = fromAccount.balance - amount,
                    updatedAt = Clock.System.now()
                )
                accountRepository.updateAccount(updatedFromAccount)

                // Update toAccount (add money)
                val updatedToAccount = toAccount.copy(
                    balance = toAccount.balance + amount,
                    updatedAt = Clock.System.now()
                )
                accountRepository.updateAccount(updatedToAccount)

                // Create transfer transaction for tracking
                val currentTime = Clock.System.now()
                val transferTransaction = Transaction(
                    id = abs(Random.nextLong()),
                    amount = amount,
                    account = fromAccount,
                    category = null, // Transfers don't have categories
                    transactionPartner = "Transfer to ${toAccount.name}",
                    transactionDirection = TransactionDirection.AccountTransfer,
                    description = description,
                    dateOfTransaction = currentTime,
                    updatedAt = currentTime,
                    createdAt = currentTime
                )

                transactionRepository.addTransaction(transferTransaction)
            } catch (e: Exception) {
                // Re-throw with more context
                throw RuntimeException(
                    "Transfer failed: ${fromAccount.name} -> ${toAccount.name}, amount: ${amount.formattedMoney}",
                    e
                )
            }
        }
    }
}