package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.AccountEntity
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    fun getAccountsFlow(): Flow<List<AccountEntity>>
    suspend fun getAccount(accountId: Long): AccountEntity
    suspend fun addAccount(accountEntity: AccountEntity)
    suspend fun updateAccount(accountEntity: AccountEntity)
    suspend fun deleteAccount(accountEntity: AccountEntity)

}