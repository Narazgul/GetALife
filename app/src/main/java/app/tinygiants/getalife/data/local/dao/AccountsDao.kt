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
interface AccountsDao {

    @Transaction
    @Query("SELECT * FROM accounts")
    fun getAccountsFlow(): Flow<List<AccountEntity>>

    @Transaction
    @Query("SELECT * FROM accounts")
    suspend fun getAccounts(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id == :accountId")
    suspend fun getAccount(accountId: Long): AccountEntity

    @Insert
    suspend fun addAccount(accountEntity: AccountEntity)

    @Update
    suspend fun updateAccount(accountEntity: AccountEntity)

    @Delete
    suspend fun deleteAccount(accountEntity: AccountEntity)
}