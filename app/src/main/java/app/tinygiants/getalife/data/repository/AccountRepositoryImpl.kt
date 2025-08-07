package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.AccountDao
import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.remote.FirestoreDataSource
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.usecase.GetCurrentBudgetUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Offline-first repository for Accounts.
 * Room = Single Source of Truth, Firestore = automatic sync layer.
 */
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val getCurrentBudget: GetCurrentBudgetUseCase,
    private val firestore: FirestoreDataSource,
    private val externalScope: CoroutineScope
) : AccountRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAccountsFlow(): Flow<List<Account>> =
        getCurrentBudget.currentBudgetIdOrDefaultFlow.flatMapLatest { budgetId ->
            accountDao.getAllAccountsFlow(budgetId).map { accountEntities ->
                accountEntities.map { accountEntity -> accountEntity.toDomain() }
            }
        }

    override suspend fun getAccount(accountId: Long): Account? {
        val budgetId = getCurrentBudget.getCurrentBudgetIdOrDefault()
        return accountDao.getAccount(accountId = accountId, budgetId = budgetId)?.toDomain()
    }

    override suspend fun addAccount(account: Account) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entity = AccountEntity.fromDomain(account, budgetId)
        accountDao.addAccount(accountEntity = entity.copy(isSynced = false))
        syncAccountInBackground(entity)
    }

    override suspend fun updateAccount(account: Account) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entity = AccountEntity.fromDomain(account, budgetId)
        accountDao.updateAccount(accountEntity = entity.copy(isSynced = false))
        syncAccountInBackground(entity)
    }

    override suspend fun deleteAccount(account: Account) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entity = AccountEntity.fromDomain(account, budgetId)
        accountDao.deleteAccount(accountEntity = entity)
        // TODO: firestore deletion
    }

    private fun syncAccountInBackground(entity: AccountEntity) {
        externalScope.async {
            try {
                firestore.saveAccount(entity)
                // Mark as synced in local database
                accountDao.updateAccount(entity.copy(isSynced = true))
            } catch (_: Exception) {
                // Firestore handles offline persistence automatically
            }
        }
    }

    suspend fun syncWithFirestore(budgetId: String) {
        try {
            val remote = firestore.getAccounts(budgetId)
            val local = accountDao.getAllAccountsFlow(budgetId).first()

            remote.forEach { remoteAccount ->
                val localAccount = local.find { it.id == remoteAccount.id }
                if (localAccount == null || remoteAccount.updatedAt > localAccount.updatedAt) {
                    accountDao.updateAccount(remoteAccount.copy(isSynced = true))
                }
            }
        } catch (_: Exception) {
            // Handle gracefully - local remains source of truth
        }
    }
}