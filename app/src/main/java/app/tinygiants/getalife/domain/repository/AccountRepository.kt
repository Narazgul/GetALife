package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    fun getAccountsFlow(): Flow<List<Account>>
    suspend fun getAccount(accountId: Long): Account?
    suspend fun addAccount(account: Account)
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(account: Account)

}