package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.usecase.transaction.AddTransactionUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.random.Random

class AddAccountUseCase @Inject constructor(
    private val repository: AccountRepository,
    private val addTransaction: AddTransactionUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(name: String, balance: Money, type: AccountType, startingBalanceName: String) {

        val accounts = repository.getAccountsFlow().first()
        val accountId = Random.nextLong()

        val accountEntity = withContext(defaultDispatcher) {

            val highestListPosition = accounts.maxOfOrNull { it.listPosition }
            val endOfListPosition = if (highestListPosition == null) 0 else highestListPosition + 1
            val creationTime = Clock.System.now()

            AccountEntity(
                id = accountId,
                name = name,
                balance = balance.value,
                type = type,
                listPosition = endOfListPosition,
                updatedAt = creationTime,
                createdAt = creationTime
            )
        }

        val direction = if (balance.value >= 0) TransactionDirection.Inflow else TransactionDirection.Outflow

        repository.addAccount(accountEntity = accountEntity)
        addTransaction(
            amount = Money(value = balance.value),
            direction = direction,
            accountId = accountId,
            category = null,
            transactionPartner = "",
            description = startingBalanceName,
            isAccountCreation = true
        )
    }
}