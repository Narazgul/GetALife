package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.R
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.presentation.main_app.shared_composables.UiText.StringResource
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
        val assignedInCategorySum = categories.sumOf { category -> category.assignedMoney.asBigDecimal() }
        return Money(value = assignedInCategorySum)
    }

    private fun getOverspentMoneyInCategoriesSum(categories: List<Category>): Money {
        val overspentInCategorySum = categories
            .filter { category -> category.availableMoney < EmptyMoney() }
            .sumOf { category -> category.availableMoney.positiveMoney().asBigDecimal()  }
            .toDouble()
        return Money(value = overspentInCategorySum)
    }

    private fun getOverspentCategoriesText(assignableMoney: Money, categories: List<Category>): StringResource? {
        if (assignableMoney != EmptyMoney()) return null

        val overspentCategories = categories.filter { categoryEntity -> categoryEntity.availableMoney < EmptyMoney() }
        if (overspentCategories.isEmpty()) return null

        val overspentSum = overspentCategories.sumOf { categoryEntity -> categoryEntity.availableMoney.asBigDecimal() }
        val amountOfOverspentCategories = overspentCategories.count()

        val singularOrPluralCategoryText = if (amountOfOverspentCategories == 1) StringResource(R.string.category)
        else StringResource(R.string.categories)

        return StringResource(
            R.string.overspent_category,
            amountOfOverspentCategories,
            singularOrPluralCategoryText,
            Money(value = overspentSum).formattedPositiveMoney
        )
    }
}