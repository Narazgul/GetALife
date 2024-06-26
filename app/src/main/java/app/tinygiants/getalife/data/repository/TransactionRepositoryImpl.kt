package app.tinygiants.getalife.data.repository

import androidx.room.withTransaction
import app.tinygiants.getalife.data.local.GetALifeDatabase
import app.tinygiants.getalife.data.local.dao.AccountsDao
import app.tinygiants.getalife.data.local.dao.CategoryDao
import app.tinygiants.getalife.data.local.dao.TransactionDao
import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val db: GetALifeDatabase,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountsDao,
    private val categoryDao: CategoryDao
) : TransactionRepository {

    override fun getTransactions(accountId: Long): Flow<Result<List<TransactionEntity>>> =
        flow {
            transactionDao.getAccountTransactionsFlow(accountId = accountId)
                .catch { exception -> emit(Result.failure(exception)) }
                .collect { transactions ->
                    emit(Result.success(transactions))}
        }

    override suspend fun addTransaction(transaction: TransactionEntity, account: AccountEntity, category: CategoryEntity?) {
        db.withTransaction {
            transactionDao.addTransaction(transaction = transaction)
            accountDao.updateAccount(accountEntity = account)
            if (category != null) categoryDao.updateCategory(categoryEntity = category)
        }
    }

    override suspend fun updateTransaction(transaction: TransactionEntity, account: AccountEntity, category: CategoryEntity?) {
        db.withTransaction {
            transactionDao.updateTransaction(transaction = transaction)
            accountDao.updateAccount(accountEntity = account)
            if (category != null) categoryDao.updateCategory(categoryEntity = category)
        }
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity, account: AccountEntity, category: CategoryEntity?) {
        db.withTransaction {
            transactionDao.deleteTransaction(transaction = transaction)
            accountDao.updateAccount(accountEntity = account)
            if (category != null) categoryDao.updateCategory(categoryEntity = category)
        }
    }
}