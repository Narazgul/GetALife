package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

class AddAccountUseCase @Inject constructor(
    private val repository: AccountRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(name: String, balance: Money, type: AccountType) {

        val accounts = repository.getAccounts()

        val accountEntity = withContext(defaultDispatcher) {

            val highestListPosition = accounts.maxOfOrNull { it.listPosition }
            val endOfListPosition = if (highestListPosition == null) 0 else highestListPosition + 1

            AccountEntity(
                id = Random.nextLong(),
                name = name,
                balance = balance.value,
                type = type,
                listPosition = endOfListPosition
            )
        }

        repository.addAccount(accountEntity = accountEntity)
    }
}