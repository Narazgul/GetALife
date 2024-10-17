package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.AccountDao
import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccountsRepositoryImpl @Inject constructor(private val accountDao: AccountDao) : AccountRepository {

    override fun getAccountsFlow(): Flow<List<AccountEntity>> = accountDao.getAccountsFlow()
    override suspend fun getAccounts() = accountDao.getAccounts()
    override suspend fun getAccount(accountId: Long) = accountDao.getAccount(accountId = accountId)
    override suspend fun addAccount(accountEntity: AccountEntity) = accountDao.addAccount(accountEntity = accountEntity)
    override suspend fun updateAccount(accountEntity: AccountEntity) = accountDao.updateAccount(accountEntity = accountEntity)
    override suspend fun deleteAccount(accountEntity: AccountEntity) = accountDao.deleteAccount(accountEntity = accountEntity)

}