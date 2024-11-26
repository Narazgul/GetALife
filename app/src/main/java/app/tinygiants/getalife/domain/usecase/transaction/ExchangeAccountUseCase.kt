package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
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

        if (transaction.account == null) return
        if (transaction.account.id == oldAccount?.id) return

        withContext(defaultDispatcher) {

            updateOldAccount(oldAccount = oldAccount, newValue = transaction.amount.value)
            updateNewAccount(transaction = transaction)
            updateTransaction(transaction = transaction)
        }
    }

    private suspend fun updateOldAccount(oldAccount: Account?, newValue: Double) {
        if (oldAccount == null) return

        val updatedBalance = oldAccount.balance.value - newValue

        val updatedOldAccount = oldAccount.run {
            AccountEntity(
                id = id,
                name = name,
                balance = updatedBalance,
                type = type,
                listPosition = listPosition,
                updatedAt = Clock.System.now(),
                createdAt = createdAt,
            )
        }

        accountRepository.updateAccount(updatedOldAccount)
    }

    private suspend fun updateNewAccount(transaction: Transaction) {
        val account = transaction.account ?: return
        val updatedBalance = account.balance.value + transaction.amount.value

        val newAccountEntity = account.run {
            AccountEntity(
                id = id,
                name = name,
                balance = updatedBalance,
                type = type,
                listPosition = listPosition,
                updatedAt = Clock.System.now(),
                createdAt = createdAt
            )
        }
        accountRepository.updateAccount(newAccountEntity)
    }

    private suspend fun updateTransaction(transaction: Transaction) {
        if (transaction.account == null) return

        val updatedTransaction = transaction.run {
            TransactionEntity(
                id = id,
                accountId = account!!.id,
                categoryId = category?.id,
                amount = amount.value,
                transactionPartner = transactionPartner,
                transactionDirection = transactionDirection,
                description = description,
                updatedAt = Clock.System.now(),
                createdAt = createdAt,
            )
        }

        transactionRepository.updateTransaction(updatedTransaction)
    }
}