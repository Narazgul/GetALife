package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.account.GetAccountsUseCase
import app.tinygiants.getalife.domain.usecase.budget.category.GetCategoriesUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetTransactionsForAccountUseCase @Inject constructor(
    private val transactions: TransactionRepository,
    private val accounts: GetAccountsUseCase,
    private val categories: GetCategoriesUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(accountId: Long): Flow<Result<List<Transaction>>> {
        return combine(
            flow = transactions.getTransactionsByAccount(accountId = accountId),
            flow2 = accounts(),
            flow3 = categories()
        ) { transactions, accountsResult, categoriesResult ->
            val accounts = accountsResult.getOrNull()
            val categories = categoriesResult.getOrNull()

            if (transactions.isEmpty()) Result.failure<Throwable>(Throwable("Transaction is empty"))
            if (accounts.isNullOrEmpty()) Result.failure<Throwable>(Throwable("Account is null or empty"))
            if (categories.isNullOrEmpty()) Result.failure<Throwable>(Throwable("Category is null or empty"))

            val sortedByTimestampTransactions = sortTransactionsByTimestamp(transactions)

            Result.success(
                mapToTransactions(
                    transactions = sortedByTimestampTransactions!!,
                    accounts = accounts!!,
                    categories = categories!!
                )
            )
        }
    }

    private fun sortTransactionsByTimestamp(transactions: List<TransactionEntity>?) = transactions?.sortedBy { it.timestamp }

    private suspend fun mapToTransactions(
        transactions: List<TransactionEntity>,
        accounts: List<Account>,
        categories: List<Category>
    ): List<Transaction> {

        return withContext(defaultDispatcher) {

            transactions.mapNotNull { transactionEntity ->

                val account = accounts.find { transactionEntity.accountId == it.id } ?: return@mapNotNull null
                val category = categories.find { transactionEntity.categoryId == it.id }

                Transaction(
                    id = transactionEntity.id,
                    amount = Money(value = transactionEntity.amount),
                    account = account,
                    category = category,
                    transactionPartner = transactionEntity.transactionPartner,
                    direction = transactionEntity.transactionDirection,
                    description = transactionEntity.description,
                    timestamp = transactionEntity.timestamp
                )
            }
        }
    }
}