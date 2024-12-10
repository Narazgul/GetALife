package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetTransactionsForAccountUseCase @Inject constructor(
    private val transactions: TransactionRepository,
    private val accounts: AccountRepository,
    private val categories: CategoryRepository
) {

    operator fun invoke(accountId: Long): Flow<Result<List<Transaction>>> {
        return combine(
            flow = transactions.getTransactionsByAccountFlow(accountId = accountId),
            flow2 = accounts.getAccountsFlow(),
            flow3 = categories.getCategoriesFlow()
        ) { transactions, accounts, categories ->
            if (transactions.isEmpty()) Result.failure<Throwable>(Throwable("Transaction is empty"))
            if (accounts.isEmpty()) Result.failure<Throwable>(Throwable("Account is null or empty"))
            if (categories.isEmpty()) Result.failure<Throwable>(Throwable("Category is null or empty"))

            val sortedByTimestampTransactions = transactions.sortedBy { transaction -> transaction.dateOfTransaction }

            Result.success(sortedByTimestampTransactions)
        }
    }
}