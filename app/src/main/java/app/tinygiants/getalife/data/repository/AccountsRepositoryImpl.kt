package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.AccountDao
import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountsRepositoryImpl @Inject constructor(private val accountDao: AccountDao) : AccountRepository {

    override fun getAccountsFlow(): Flow<List<Account>> = accountDao.getAccountsFlow().map { accountEntities ->
        accountEntities.map { accountEntity -> accountEntity.toDomain() }
    }

    override suspend fun getAccount(accountId: Long): Account? =
        accountDao.getAccount(accountId = accountId)?.toDomain()

    override suspend fun addAccount(account: Account) =
        accountDao.addAccount(accountEntity = AccountEntity.fromDomain(account))

    override suspend fun updateAccount(account: Account) =
        accountDao.updateAccount(accountEntity = AccountEntity.fromDomain(account))

    override suspend fun deleteAccount(account: Account) =
        accountDao.deleteAccount(accountEntity = AccountEntity.fromDomain(account))
}