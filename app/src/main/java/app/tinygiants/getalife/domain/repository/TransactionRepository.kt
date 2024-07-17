package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getTransactionsByAccountFlow(accountId: Long): Flow<Result<List<TransactionEntity>>>
    suspend fun getTransactionsByCategory(categoryId: Long): List<TransactionEntity>
    suspend fun addTransaction(transaction: TransactionEntity, account: AccountEntity, category: CategoryEntity?)
    suspend fun updateTransaction(transaction: TransactionEntity, account: AccountEntity, category: CategoryEntity?)
    suspend fun deleteTransaction(transaction: TransactionEntity, account: AccountEntity, category: CategoryEntity?)
}