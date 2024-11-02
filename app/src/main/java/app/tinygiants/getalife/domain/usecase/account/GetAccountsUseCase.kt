package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Money
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
                emit(getSortedAccounts(accounts))
            }
    }

    private suspend fun getSortedAccounts(accounts: List<AccountEntity>): Result<List<Account>> {
        return Result.success(
            withContext(defaultDispatcher) {
                accounts
                    .sortedBy { account -> account.listPosition }
                    .mapIndexed { index, account ->
                        Account(
                            id = account.id,
                            name = account.name,
                            balance = Money(value = account.balance),
                            type = account.type,
                            listPosition = index,
                            updatedAt = account.updatedAt,
                            createdAt = account.createdAt
                        )
                    }
            }
        )
    }
}