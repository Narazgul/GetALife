package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.random.Random

class AddAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(name: String, balance: Money, type: AccountType, startingBalanceName: String) {

        val accountId = Random.nextLong()

        addAccount(
            accountId = accountId,
            name = name,
            balance = balance.value,
            type = type,
            accountRepository::addAccount
        )

        addInitialStartingBalanceTransaction(
            accountId = accountId,
            amount = balance.value,
            description = startingBalanceName,
            addTransaction = transactionRepository::addTransaction
        )
    }

    private suspend fun addAccount(
        accountId: Long,
        name: String,
        balance: Double,
        type: AccountType,
        addAccount: suspend (AccountEntity) -> Unit
    ) {
        val accounts = accountRepository.getAccountsFlow().first()
        val accountEntity = withContext(defaultDispatcher) {

            val highestListPosition = accounts.maxOfOrNull { it.listPosition }
            val endOfListPosition = if (highestListPosition == null) 0 else highestListPosition + 1
            val creationTime = Clock.System.now()

            AccountEntity(
                id = accountId,
                name = name,
                balance = balance,
                type = type,
                listPosition = endOfListPosition,
                updatedAt = creationTime,
                createdAt = creationTime
            )
        }

        addAccount(accountEntity)
    }

    private suspend fun addInitialStartingBalanceTransaction(
        accountId: Long,
        amount: Double,
        description: String,
        addTransaction: suspend (TransactionEntity) -> Unit
    ) {
        val direction = if (amount >= 0) TransactionDirection.Inflow else TransactionDirection.Outflow
        val currentTime = Clock.System.now()

        val transactionEntity = TransactionEntity(
            id = Random.nextLong(),
            accountId = accountId,
            categoryId = null,
            amount = amount,
            transactionDirection = direction,
            transactionPartner = "",
            description = description,
            updatedAt = currentTime,
            createdAt = currentTime
        )

        addTransaction(transactionEntity)
    }
}