package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.entities.AccountWithTransactionsEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
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

    suspend operator fun invoke(): Flow<Result<List<Account>>> {
        return flow {
            accountRepository.getAccountsWithTransactionsFlow()
                .catch { throwable -> emit(Result.failure(throwable)) }
                .collect { result ->
                    result.onSuccess { accountsWithTransactions -> emit(getAccounts(accountsWithTransactions)) }
                    result.onFailure { throwable -> emit(Result.failure(throwable)) }
                }
        }
    }

    private suspend fun getAccounts(accountsWithTransactions: List<AccountWithTransactionsEntity>): Result<List<Account>> {
        return Result.success(
            withContext(defaultDispatcher) {
                accountsWithTransactions
                    .sortedBy { it.account.listPosition }
                    .mapIndexed { index, accountWithTransaction ->

                        val (account, transactions) = accountWithTransaction
                        val balance = calculateNetAmount(transactions)

                        Account(
                            id = account.id,
                            name = account.name,
                            balance = Money(balance),
                            type = account.type,
                            listPosition = index
                        )
                    }
            }
        )
    }

    private fun calculateNetAmount(transactions: List<TransactionEntity>) =
        transactions.fold(0.0) { sum, transaction ->
            when (transaction.transactionDirection) {
                TransactionDirection.Inflow -> sum + transaction.amount
                TransactionDirection.Outflow -> sum - transaction.amount
                else -> sum
            }
        }
}