package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.repository.AccountRepository
import kotlinx.datetime.Clock
import javax.inject.Inject

class UpdateAccountUseCase @Inject constructor(private val repository: AccountRepository){

    suspend operator fun invoke(account: Account) {
        val accountEntity = AccountEntity(
            id = account.id,
            name = account.name,
            balance = account.balance.value,
            type = account.type,
            listPosition = account.listPosition,
            updatedAt = Clock.System.now(),
            createdAt = account.createdAt
        )

        repository.updateAccount(accountEntity = accountEntity)
    }
}