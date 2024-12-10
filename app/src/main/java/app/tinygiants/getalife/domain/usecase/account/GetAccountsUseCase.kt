package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.repository.AccountRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<Result<List<Account>>> = flow {
        accountRepository.getAccountsFlow()
            .catch { throwable -> emit(Result.failure(throwable)) }
            .collect { accounts ->
                val sortedAccounts = getSortedAccounts(accounts)
                emit(sortedAccounts)
            }
    }

    private suspend fun getSortedAccounts(accounts: List<Account>): Result<List<Account>> {
        return Result.success(
            withContext(defaultDispatcher) {
                accounts
                    .sortedBy { account -> account.listPosition }
                    .mapIndexed { index, account -> account.copy(listPosition = index) }
            }
        )
    }
}