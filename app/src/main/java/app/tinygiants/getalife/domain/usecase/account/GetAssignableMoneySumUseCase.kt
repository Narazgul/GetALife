package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAssignableMoneySumUseCase @Inject constructor(
    private val accountsRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<Result<Money>> {
        return combine(
            flow = categoryRepository.getCategoriesFlow(),
            flow2 = accountsRepository.getAccountsFlow()
        ) { categories, accounts ->

            if (categories.isEmpty()) Result.failure<Throwable>(Throwable("Budget is null or empty"))

            val assignableMoney =
                calculateAvailableMoney(accounts = accounts, categories = categories)

            Result.success(Money(assignableMoney))
        }
    }

    private suspend fun calculateAvailableMoney(
        accounts: List<AccountEntity>,
        categories: List<CategoryEntity>?
    ) = withContext(defaultDispatcher) {

        val availableMoneyInAccounts = calculateAvailableMoneyInAccounts(accounts = accounts)
        val availableMoneyInCategories =
            calculateAvailableMoneyInCategories(categories = categories)

        availableMoneyInAccounts - availableMoneyInCategories
    }

    private fun calculateAvailableMoneyInAccounts(accounts: List<AccountEntity>) =
        accounts
            .filter { account ->
                account.type == AccountType.Cash ||
                        account.type == AccountType.Checking ||
                        account.type == AccountType.Savings ||
                        account.type == AccountType.CreditCard
            }
            .sumOf { account -> account.balance }

    private fun calculateAvailableMoneyInCategories(categories: List<CategoryEntity>?): Double {
        if (categories.isNullOrEmpty()) return 0.0

        return categories.sumOf { category -> category.assignedMoney }
    }
}