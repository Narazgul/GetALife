package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.repository.AccountRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(private val repository: AccountRepository){

    suspend operator fun invoke(account: Account) {
        val accountEntity = AccountEntity(
            id = account.id,
            name = account.name,
            balance = account.balance.value,
            type = account.type,
            listPosition = account.listPosition,
            updatedAt = account.updatedAt,
            createdAt = account.createdAt
        )

        repository.deleteAccount(accountEntity = accountEntity)
    }
}