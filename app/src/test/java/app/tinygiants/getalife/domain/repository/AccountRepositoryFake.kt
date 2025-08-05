package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class AccountRepositoryFake : AccountRepository {

    val accounts = MutableStateFlow<List<Account>>(emptyList())

    override fun getAccountsFlow(): Flow<List<Account>> = accounts

    override suspend fun getAccount(accountId: Long): Account? = accounts.value.firstOrNull { it.id == accountId }

    override suspend fun addAccount(account: Account) {
        accounts.value = accounts.value.toMutableList().apply { add(account) }
    }

    override suspend fun updateAccount(account: Account) {
        accounts.value = accounts.value.toMutableList().apply {
            val index = indexOfFirst { it.id == account.id }
            if (index != -1) {
                set(index, account)
            }
        }
    }

    override suspend fun deleteAccount(account: Account) {
        accounts.value = accounts.value.toMutableList().apply {
            removeIf { it.id == account.id }
        }
    }
}