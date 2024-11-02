package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.AccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class AccountRepositoryFake : AccountRepository {

    val accountsFlow = MutableStateFlow<List<AccountEntity>>(emptyList())

    override fun getAccountsFlow(): Flow<List<AccountEntity>> = accountsFlow

    override suspend fun getAccount(accountId: Long): AccountEntity = accountsFlow.value.first { it.id == accountId }

    override suspend fun addAccount(accountEntity: AccountEntity) {
        accountsFlow.value = accountsFlow.value.toMutableList().apply {
            add(accountEntity)
        }
    }

    override suspend fun updateAccount(accountEntity: AccountEntity) {
        accountsFlow.value = accountsFlow.value.toMutableList().apply {
            val index = indexOfFirst { it.id == accountEntity.id }
            if (index != -1) {
                set(index, accountEntity)
            }
        }
    }

    override suspend fun deleteAccount(accountEntity: AccountEntity) {
        accountsFlow.value = accountsFlow.value.toMutableList().apply {
            removeIf { it.id == accountEntity.id }
        }
    }
}