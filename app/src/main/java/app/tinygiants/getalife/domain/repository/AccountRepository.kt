package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.AccountWithTransactionsEntity
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    fun getAccountsFlow(): Flow<Result<List<AccountEntity>>>
    suspend fun getAccounts(): List<AccountEntity>

    fun getAccountsWithTransactionsFlow(): Flow<Result<List<AccountWithTransactionsEntity>>>

    suspend fun getAccount(accountId: Long): AccountEntity
    suspend fun addAccount(accountEntity: AccountEntity)
    suspend fun updateAccount(accountEntity: AccountEntity)
    suspend fun deleteAccount(accountEntity: AccountEntity)
}