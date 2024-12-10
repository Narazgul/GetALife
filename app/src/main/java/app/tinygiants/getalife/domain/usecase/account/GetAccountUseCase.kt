package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.domain.repository.AccountRepository
import javax.inject.Inject

class GetAccountUseCase @Inject constructor(private val repository: AccountRepository) {

    suspend operator fun invoke(accountId: Long) =
        repository.getAccount(accountId)
}