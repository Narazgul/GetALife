package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetTransactionsForAccountUseCase @Inject constructor(
    private val transactions: TransactionRepository,
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(accountId: Long): Flow<Result<List<Transaction>>> {
        return combine(
            flow = transactions.getTransactionsByAccount(accountId = accountId),
            flow2 = accounts.getAccountsFlow(),
            flow3 = categories.getCategoriesFlow()
        ) { transactions, accounts, categories ->
            if (transactions.isEmpty()) Result.failure<Throwable>(Throwable("Transaction is empty"))
            if (accounts.isEmpty()) Result.failure<Throwable>(Throwable("Account is null or empty"))
            if (categories.isEmpty()) Result.failure<Throwable>(Throwable("Category is null or empty"))

            val sortedByTimestampTransactions = sortTransactionsByTimestamp(transactions)

            Result.success(
                mapToTransactions(
                    transactions = sortedByTimestampTransactions!!,
                    accounts = accounts,
                    categories = categories
                )
            )
        }
    }

    private fun sortTransactionsByTimestamp(transactions: List<TransactionEntity>?) = transactions?.sortedBy { it.createdAt }

    private suspend fun mapToTransactions(
        transactions: List<TransactionEntity>,
        accounts: List<AccountEntity>,
        categories: List<CategoryEntity>
    ): List<Transaction> {

        return withContext(defaultDispatcher) {

            transactions.mapNotNull { transactionEntity ->

                val accountEntity =
                    accounts.find { account -> transactionEntity.accountId == account.id } ?: return@mapNotNull null
                val categoryEntity = categories.find { category -> transactionEntity.categoryId == category.id }

                val account = accountEntity.run {
                    Account(
                        id = id,
                        name = name,
                        balance = Money(value = balance),
                        type = type,
                        listPosition = listPosition,
                        updatedAt = updatedAt,
                        createdAt = createdAt
                    )
                }

                val category = if (categoryEntity == null) null else {
                    val budgetTarget = categoryEntity.budgetTarget ?: 0.00

                    categoryEntity.run {
                        Category(
                            id = id,
                            groupId = groupId,
                            emoji = emoji,
                            name = name,
                            budgetTarget = Money(budgetTarget),
                            budgetPurpose = budgetPurpose,
                            assignedMoney = Money(assignedMoney),
                            availableMoney = Money(availableMoney),
                            progress = EmptyProgress(),
                            optionalText = optionalText,
                            listPosition = listPosition,
                            isInitialCategory = isInitialCategory,
                            updatedAt = updatedAt,
                            createdAt = createdAt,
                        )
                    }
                }


                Transaction(
                    id = transactionEntity.id,
                    amount = Money(value = transactionEntity.amount),
                    account = account,
                    category = category,
                    transactionPartner = transactionEntity.transactionPartner,
                    transactionDirection = transactionEntity.transactionDirection,
                    description = transactionEntity.description,
                    updatedAt = transactionEntity.updatedAt,
                    createdAt = transactionEntity.createdAt
                )
            }
        }
    }
}