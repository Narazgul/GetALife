package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepository
import kotlinx.datetime.Clock
import javax.inject.Inject

class GetAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {

    suspend operator fun invoke(accountId: Long): Account? {
        val accountEntity = repository.getAccount(accountId)
        return accountEntity?.run {
            Account(
                id = id,
                name = name,
                balance = Money(value = balance),
                type = type,
                listPosition = listPosition,
                updatedAt = Clock.System.now(),
                createdAt = Clock.System.now()
            )
        } ?: return null
    }
}