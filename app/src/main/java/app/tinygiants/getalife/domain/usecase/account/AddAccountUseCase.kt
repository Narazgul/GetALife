package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.budget.UpdateAssignableMoneyUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.random.Random

class AddAccountUseCase @Inject constructor(
    private val updateAssignableMoney: UpdateAssignableMoneyUseCase,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        name: String,
        balance: Money,
        type: AccountType,
        startingBalanceName: String,
        startingBalanceDescription: String
    ) {
        val accountId = Random.nextLong()

        addAccount(accountId = accountId, name = name, balance = balance.value, type = type)
        addAssignableMoney(amount = balance)
        addStartingBalanceTransaction(accountId = accountId, amount = balance.value, startingBalanceName = startingBalanceName, startingBalanceDescription = startingBalanceDescription)
    }

    private suspend fun addAccount(
        accountId: Long,
        name: String,
        balance: Double,
        type: AccountType
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

        accountRepository.addAccount(accountEntity)
    }

    private suspend fun addAssignableMoney(amount: Money) {
        updateAssignableMoney(amount)
    }

    private suspend fun addStartingBalanceTransaction(
        accountId: Long,
        amount: Double,
        startingBalanceName: String,
        startingBalanceDescription: String,
    ) {
        val direction = if (amount >= 0) TransactionDirection.Inflow else TransactionDirection.Outflow
        val currentTime = Clock.System.now()

        val transactionEntity = TransactionEntity(
            id = Random.nextLong(),
            accountId = accountId,
            categoryId = null,
            amount = amount,
            transactionDirection = direction,
            transactionPartner = startingBalanceName,
            description = startingBalanceDescription,
            updatedAt = currentTime,
            createdAt = currentTime
        )

        transactionRepository.addTransaction(transactionEntity)
    }
}