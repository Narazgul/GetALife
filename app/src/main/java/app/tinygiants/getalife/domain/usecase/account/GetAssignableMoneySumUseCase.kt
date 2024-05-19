package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.HeaderWithCategoriesEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.BudgetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAssignableMoneySumUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(): Flow<Result<Money>> {
        return budgetRepository.getBudgetFlow()
            .combine(accountRepository.getAccountsFlow()) { budgetResult, accountsResult ->
                val budget = budgetResult.getOrNull()
                val accounts = accountsResult.getOrNull()

                if (budget.isNullOrEmpty()) Result.failure<Throwable>(Throwable("Budget is null or empty"))
                if (accounts.isNullOrEmpty()) Result.failure<Throwable>(Throwable("Account is null or empty"))

                val assignableMoney = calculateAvailableMoney(accounts = accounts!!, budget = budget!!)

                Result.success(Money(assignableMoney))
            }
    }

    private suspend fun calculateAvailableMoney(
        accounts: List<AccountEntity>,
        budget: List<HeaderWithCategoriesEntity>
    ) = withContext(defaultDispatcher) {
        val availableMoneyInAccounts = calculateAvailableMoneyInAccounts(accountEntities = accounts)
        val availableMoneyInCategories = calculateAvailableMoneyInCategories(budget = budget)

        availableMoneyInAccounts - availableMoneyInCategories
    }

    private fun calculateAvailableMoneyInAccounts(accountEntities: List<AccountEntity>) =
        accountEntities
            .filter { accountEntity ->
                accountEntity.type == AccountType.Cash ||
                        accountEntity.type == AccountType.Checking ||
                        accountEntity.type == AccountType.Savings ||
                        accountEntity.type == AccountType.CreditCard
            }
            .sumOf { accountEntity -> accountEntity.balance }

    private fun calculateAvailableMoneyInCategories(budget: List<HeaderWithCategoriesEntity>) =
        budget
            .flatMap { it.categories }
            .sumOf { it.availableMoney }
}