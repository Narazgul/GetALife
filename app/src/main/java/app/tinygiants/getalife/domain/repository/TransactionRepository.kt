package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity

interface TransactionRepository {

    suspend fun addTransaction(transaction: TransactionEntity, account: AccountEntity, category: CategoryEntity?)
    suspend fun updateTransaction(transaction: TransactionEntity, account: AccountEntity, category: CategoryEntity?)
    suspend fun deleteTransaction(transaction: TransactionEntity, account: AccountEntity, category: CategoryEntity?)
}