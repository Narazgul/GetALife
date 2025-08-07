package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.tinygiants.getalife.data.local.entities.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Transaction
    @Query("SELECT * FROM accounts WHERE isClosed = 0 AND budgetId = :budgetId")
    fun getAccountsFlow(budgetId: String): Flow<List<AccountEntity>>

    @Transaction
    @Query("SELECT * FROM accounts WHERE budgetId = :budgetId")
    fun getAllAccountsFlow(budgetId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id == :accountId AND budgetId = :budgetId")
    suspend fun getAccount(accountId: Long, budgetId: String): AccountEntity?

    @Insert
    suspend fun addAccount(accountEntity: AccountEntity)

    @Update
    suspend fun updateAccount(accountEntity: AccountEntity)

    @Delete
    suspend fun deleteAccount(accountEntity: AccountEntity)

    // Offline-first sync queries
    @Query("SELECT * FROM accounts WHERE isSynced = 0 AND budgetId = :budgetId")
    suspend fun getUnsyncedAccounts(budgetId: String): List<AccountEntity>

    @Query("UPDATE accounts SET isSynced = 1 WHERE id = :accountId AND budgetId = :budgetId")
    suspend fun markAccountAsSynced(accountId: Long, budgetId: String)
}