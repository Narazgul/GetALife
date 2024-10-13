package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<Result<List<Account>>> = flow {
        val accountsFlow = accountRepository.getAccountsFlow()
        val transactionsFlow = transactionRepository.getTransactions()
        accountsFlow.combine(transactionsFlow) { accounts, transactions -> getAccounts(accounts, transactions) }
            .catch { throwable -> emit(Result.failure(throwable)) }
            .collect { accounts -> emit(accounts) }
    }

    private suspend fun getAccounts(accounts: List<AccountEntity>, transactions: List<TransactionEntity>): Result<List<Account>> {
        return Result.success(
            withContext(defaultDispatcher) {
                accounts
                    .sortedBy { account -> account.listPosition }
                    .mapIndexed { index, account ->

                        val transactionsForAccount = transactions.filter { transaction -> transaction.accountId == account.id }
                        val balance = calculateNetAmount(transactionsForAccount)

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