package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.R
import app.tinygiants.getalife.data.local.entities.BudgetEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.BudgetRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.presentation.UiText.StringResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

typealias AssignableMoney = Money
typealias OverspentCategoryText = StringResource?
typealias AssignableBanner = Pair<AssignableMoney, OverspentCategoryText>

class GetAssignableMoneyUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<Result<AssignableBanner>> = flow {
        val categoriesFlow = categoryRepository.getCategoriesFlow()
        val budgetsFlow = budgetRepository.getBudgets()

        if (budgetsFlow.first().isEmpty()) emit(Result.failure(NoSuchElementException("Budget still empty")))

        categoriesFlow.combine(budgetsFlow) { categories, budgets ->
            getAssignableBanner(categories = categories, budget = budgets.first())
        }
            .catch { throwable -> emit(Result.failure(throwable)) }
            .collect { assignableBanner -> emit(assignableBanner) }
    }

    private suspend fun getAssignableBanner(categories: List<CategoryEntity>, budget: BudgetEntity): Result<AssignableBanner> {
        val availableMoneyForAssignment = budget.readyToAssign

        return withContext(defaultDispatcher) {
            val availableMoneyInCategories = categories.sumOf { category -> category.assignedMoney }
            val assignableMoney = Money(value = availableMoneyForAssignment - availableMoneyInCategories)
            val overspentCategoriesText =
                if (assignableMoney.value == 0.0) getOverspentCategoriesText(categories = categories) else null

            Result.success(
                AssignableBanner(
                    first = assignableMoney,
                    second = overspentCategoriesText
                )
            )
        }
    }

    private fun getOverspentCategoriesText(categories: List<CategoryEntity>): StringResource? {
        val overspentCategories = categories.filter { categoryEntity -> categoryEntity.availableMoney < 0.0 }
        if (overspentCategories.isEmpty()) return null

        val overspentSum = overspentCategories.sumOf { categoryEntity -> categoryEntity.availableMoney }
        val amountOfOverspentCategories = overspentCategories.count()

        val singularOrPluralCategoryText = if (amountOfOverspentCategories == 1) StringResource(R.string.category)
        else StringResource(R.string.categories)

        return StringResource(
            R.string.overspent_category,
            amountOfOverspentCategories,
            singularOrPluralCategoryText,
            Money(overspentSum).formattedPositiveMoney
        )
    }
}