package app.tinygiants.getalife.data.local.dao

import app.tinygiants.getalife.data.local.entities.AccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class AccountDaoFake : AccountDao {

    val accounts = MutableStateFlow<List<AccountEntity>>(emptyList())

    override fun getAccountsFlow(): Flow<List<AccountEntity>> = accounts

    override suspend fun getAccounts(): List<AccountEntity> = accounts.value

    override suspend fun getAccount(accountId: Long): AccountEntity {
        return accounts.value.firstOrNull { it.id == accountId }
            ?: throw IllegalArgumentException("Account with id $accountId not found")
    }

    override suspend fun addAccount(accountEntity: AccountEntity) {
        val updatedAccounts = accounts.value.toMutableList().apply {
            add(accountEntity)
        }
        accounts.value = updatedAccounts
    }

    override suspend fun updateAccount(accountEntity: AccountEntity) {
        val updatedAccounts = accounts.value.toMutableList()
        val index = updatedAccounts.indexOfFirst { it.id == accountEntity.id }
        if (index != -1) {
            updatedAccounts[index] = accountEntity
            accounts.value = updatedAccounts
        } else {
            throw IllegalArgumentException("Account with id ${accountEntity.id} not found")
        }
    }

    override suspend fun deleteAccount(accountEntity: AccountEntity) {
        val updatedAccounts = accounts.value.toMutableList().apply {
            removeIf { it.id == accountEntity.id }
        }
        accounts.value = updatedAccounts
    }

}