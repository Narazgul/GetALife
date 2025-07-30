package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.presentation.shared_composables.UiText.StringResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

typealias AssignableMoney = Money
typealias OverspentCategoryText = StringResource?
typealias AssignableMoneyBanner = Pair<AssignableMoney, OverspentCategoryText>

data class AssignableMoneyException(override val message: String): Exception(message)

class GetAssignableMoneyUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoriesRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    private val accountTypesAvailableForAssignment = setOf(
        AccountType.Cash, AccountType.Checking, AccountType.Savings, AccountType.CreditCard
    )

    operator fun invoke(): Flow<Result<AssignableMoneyBanner>> = flow {
        val transactionFlow = transactionRepository.getTransactionsFlow()
        val categoriesFlow = categoriesRepository.getCategoriesFlow()

        transactionFlow.combine(categoriesFlow) { transactions, categories ->
            getAssignableBanner(transactions = transactions, categories = categories)
        }
            .catch { throwable ->
                emit(Result.failure(AssignableMoneyException(message = throwable.message ?: "")))
            }
            .collect { assignableMoneyBanner -> emit(assignableMoneyBanner) }
    }

    private suspend fun getAssignableBanner(transactions: List<Transaction>, categories: List<Category>) =
        withContext(defaultDispatcher) {
            val sumInflow = getInflowSum(transactions = transactions)
            val sumAssignedMoneyInCategories = getAssignedMoneyInCategoriesSum(categories = categories)
            val sumOverspentMoneyInCategories = getOverspentMoneyInCategoriesSum(categories = categories)

            val availableMoneyForAssignment = sumInflow - sumAssignedMoneyInCategories - sumOverspentMoneyInCategories

            val overspentCategoriesText = getOverspentCategoriesText(
                assignableMoney = availableMoneyForAssignment,
                categories = categories
            )

            Result.success(
                AssignableMoneyBanner(
                    first = availableMoneyForAssignment,
                    second = overspentCategoriesText
                )
            )
        }

    private fun getInflowSum(transactions: List<Transaction>): Money {
        val sumInflow = transactions
            .filter { transaction ->
                transaction.account.type in accountTypesAvailableForAssignment &&
                        transaction.transactionDirection == TransactionDirection.Inflow
            }
            .sumOf { transaction -> transaction.amount.asBigDecimal() }
            .toDouble()

        return Money(value = sumInflow)
    }

    private fun getAssignedMoneyInCategoriesSum(categories: List<Category>): Money {
        // Zukünftig: Summe aus MonthlyBudget beziehen!
        return Money(0.0)
    }

    private fun getOverspentMoneyInCategoriesSum(categories: List<Category>): Money {
        // Zukünftig: Summe aus MonthlyBudget beziehen!
        return Money(0.0)
    }

    private fun getOverspentCategoriesText(assignableMoney: Money, categories: List<Category>): StringResource? {
        // Zukünftig: Überschuldete Kategorien-Hinweise aus MonthlyBudget ableiten!
        return null
    }
}