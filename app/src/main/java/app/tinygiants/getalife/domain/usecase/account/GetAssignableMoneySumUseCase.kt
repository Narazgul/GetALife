package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.entities.AccountWithTransactionsEntity
import app.tinygiants.getalife.data.local.entities.HeaderWithCategoriesEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.BudgetRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAssignableMoneySumUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(): Flow<Result<Money>> {
        return combine(
            flow = budgetRepository.getBudgetFlow(),
            flow2 = accountRepository.getAccountsWithTransactionsFlow(),
            flow3 = transactionRepository.getTransactions()
        ) { budgetResult, accountsResult, transactionsResult ->

            val budget = budgetResult.getOrNull()
            val accounts = accountsResult.getOrNull()
            val transactions = transactionsResult.getOrNull()

            if (budget.isNullOrEmpty()) Result.failure<Throwable>(Throwable("Budget is null or empty"))
            if (accounts.isNullOrEmpty()) Result.failure<Throwable>(Throwable("Account is null or empty"))

            val assignableMoney =
                calculateAvailableMoney(accounts = accounts!!, budget = budget!!, transactions = transactions)

            Result.success(Money(assignableMoney))
        }
    }

    private suspend fun calculateAvailableMoney(
        accounts: List<AccountWithTransactionsEntity>,
        budget: List<HeaderWithCategoriesEntity>,
        transactions: List<TransactionEntity>?
    ) = withContext(defaultDispatcher) {

        val availableMoneyInAccounts = calculateAvailableMoneyInAccounts(accountEntities = accounts)
        val availableMoneyInCategories =
            calculateAvailableMoneyInCategories(budget = budget, transactions = transactions)

        availableMoneyInAccounts - availableMoneyInCategories
    }

    private fun calculateAvailableMoneyInAccounts(accountEntities: List<AccountWithTransactionsEntity>) =
        accountEntities
            .filter { accountEntity ->
                accountEntity.account.type == AccountType.Cash ||
                        accountEntity.account.type == AccountType.Checking ||
                        accountEntity.account.type == AccountType.Savings ||
                        accountEntity.account.type == AccountType.CreditCard
            }
            .sumOf { accountEntity -> calculateNetAmount(accountEntity.transactions) }

    private fun calculateNetAmount(transactions: List<TransactionEntity>) =
        transactions.fold(0.0) { sum, transaction ->
            when (transaction.transactionDirection) {
                TransactionDirection.Inflow -> sum + transaction.amount
                TransactionDirection.Outflow -> sum - transaction.amount
                else -> sum
            }
        }

//    private fun calculateAvailableMoneyInCategories(budget: List<HeaderWithCategoriesEntity>, transactions: List<TransactionEntity>?) {
//        budget
//            .flatMap { headerWithCategories -> headerWithCategories.categories }
//            .sumOf { category -> category.availableMoney }
//    }

    private fun calculateAvailableMoneyInCategories(
        budget: List<HeaderWithCategoriesEntity>,
        transactions: List<TransactionEntity>?
    ): Double {
        val categoryAmountMap = mutableMapOf<Long, Double>()

        transactions?.forEach { transaction ->
            transaction.categoryId?.let { categoryId ->
                val amount = if (transaction.transactionDirection == TransactionDirection.Inflow) {
                    transaction.amount
                } else {
                    -transaction.amount
                }
                categoryAmountMap[categoryId] = categoryAmountMap.getOrDefault(categoryId, 0.0) + amount
            }
        }

        return budget
            .flatMap { headerWithCategories -> headerWithCategories.categories }
            .sumOf { category ->
                //val amount = categoryAmountMap[category.id] ?: return@sumOf
                category.availableMoney
            }
    }

}

