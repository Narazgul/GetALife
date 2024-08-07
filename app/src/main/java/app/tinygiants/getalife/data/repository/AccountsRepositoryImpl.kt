package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.AccountsDao
import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.AccountWithTransactionsEntity
import app.tinygiants.getalife.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AccountsRepositoryImpl @Inject constructor(
    private val accountsDao: AccountsDao
) : AccountRepository {

    override fun getAccountsFlow(): Flow<Result<List<AccountEntity>>> = flow {
        accountsDao.getAccountsFlow()
            .catch { exception -> emit(Result.failure(exception)) }
            .collect { accounts -> emit(Result.success(accounts))}
    }
    override suspend fun getAccounts() = accountsDao.getAccounts()

    override fun getAccountsWithTransactionsFlow(): Flow<Result<List<AccountWithTransactionsEntity>>> = flow {
            accountsDao.getAccountsWithTransactionsFlow()
                .catch { exception -> emit(Result.failure(exception)) }
                .collect { accountsWithTransactions -> emit(Result.success(accountsWithTransactions)) }
        }

    override suspend fun getAccount(accountId: Long) = accountsDao.getAccount(accountId = accountId)
    override suspend fun addAccount(accountEntity: AccountEntity) { accountsDao.addAccount(accountEntity = accountEntity) }
    override suspend fun updateAccount(accountEntity: AccountEntity) { accountsDao.updateAccount(accountEntity = accountEntity) }
    override suspend fun deleteAccount(accountEntity: AccountEntity) { accountsDao.deleteAccount(accountEntity = accountEntity) }
}