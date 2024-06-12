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
    private val repository: AccountRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(): Flow<Result<List<Account>>> {
        return flow {
            repository.getAccountsFlow()
                .catch { throwable -> emit(Result.failure(throwable)) }
                .collect { result ->
                    result.onSuccess { accountEntities -> emit(mapToAccounts(accountEntities)) }
                    result.onFailure { throwable -> emit(Result.failure(throwable)) }
                }
        }
    }

    private suspend fun mapToAccounts(accountEntities: List<AccountEntity>) =
        Result.success(
            withContext(defaultDispatcher) {
                accountEntities
                    .sortedBy { accountEntity -> accountEntity.listPosition }
                    .mapIndexed { index, accountEntity ->
                        Account(
                            id = accountEntity.id,
                            name = accountEntity.name,
                            balance = Money(value = accountEntity.balance),
                            type = accountEntity.type,
                            listPosition = index
                        )
                    }
            }
        )
}