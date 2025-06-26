package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.usecase.transaction.AddTransactionUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Clock

class AddAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val addTransaction: AddTransactionUseCase,
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

        createAccount(
            accountId = accountId,
            name = name,
            type = type
        )
        addStartingBalanceTransaction(
            accountId = accountId,
            amount = balance,
            startingBalanceName = startingBalanceName,
            startingBalanceDescription = startingBalanceDescription
        )
    }

    private suspend fun createAccount(accountId: Long, name: String, type: AccountType) {

        val accounts = accountRepository.getAccountsFlow().first()
        val account = withContext(defaultDispatcher) {

            val highestListPosition = accounts.maxOfOrNull { it.listPosition }
            val endOfListPosition = if (highestListPosition == null) 0 else highestListPosition + 1
            val timeOfCreation = Clock.System.now()

            Account(
                id = accountId,
                name = name,
                balance = EmptyMoney(),
                type = type,
                listPosition = endOfListPosition,
                updatedAt = timeOfCreation,
                createdAt = timeOfCreation
            )
        }

        accountRepository.addAccount(account)
    }

    private suspend fun addStartingBalanceTransaction(
        accountId: Long,
        amount: Money,
        startingBalanceName: String,
        startingBalanceDescription: String,
    ) {
        val direction = if (amount >= Money(value = 0.0)) TransactionDirection.Inflow else TransactionDirection.Outflow

        addTransaction(
            accountId = accountId,
            category = null,
            amount = amount,
            direction = direction,
            transactionPartner = startingBalanceName,
            description = startingBalanceDescription
        )
    }
}