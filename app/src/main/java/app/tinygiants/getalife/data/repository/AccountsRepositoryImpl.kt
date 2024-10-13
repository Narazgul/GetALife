package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.AccountsDao
import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccountsRepositoryImpl @Inject constructor(private val accountsDao: AccountsDao) : AccountRepository {

    override fun getAccountsFlow(): Flow<List<AccountEntity>> = accountsDao.getAccountsFlow()
    override suspend fun getAccounts() = accountsDao.getAccounts()
    override suspend fun getAccount(accountId: Long) = accountsDao.getAccount(accountId = accountId)
    override suspend fun addAccount(accountEntity: AccountEntity) = accountsDao.addAccount(accountEntity = accountEntity)
    override suspend fun updateAccount(accountEntity: AccountEntity) = accountsDao.updateAccount(accountEntity = accountEntity)
    override suspend fun deleteAccount(accountEntity: AccountEntity) = accountsDao.deleteAccount(accountEntity = accountEntity)

}