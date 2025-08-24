package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.repository.AccountRepository
import javax.inject.Inject

class UpdateAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {

    suspend operator fun invoke(account: Account) {
        repository.updateAccount(account)
    }
}