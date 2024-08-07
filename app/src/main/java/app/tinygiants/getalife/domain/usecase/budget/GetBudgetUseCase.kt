package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.data.local.entities.HeaderWithCategoriesEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.BudgetPurpose
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Header
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.BudgetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs

class GetBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<Result<Map<Header, List<Category>>>> {
        return flow {
            repository.getBudgetFlow()
                .catch { throwable -> emit(Result.failure(throwable)) }
                .collect { result ->
                    result.onSuccess { list -> emit(mapToGroups(list)) }
                    result.onFailure { throwable -> emit(Result.failure(throwable)) }
                }
        }
    }

    private suspend fun mapToGroups(headersWithCategories: List<HeaderWithCategoriesEntity>) =
        Result.success(
            withContext(defaultDispatcher) {

                headersWithCategories
                    .sortedBy { headerWithCategory -> headerWithCategory.header.listPosition }
                    .mapIndexed { index, headerWithCategoriesEntity ->

                        val header =
                            mapToHeader(headerWithCategory = headerWithCategoriesEntity, newListPosition = index)
                        val categories = mapToCategories(headerWithCategory = headerWithCategoriesEntity)

                        header to categories
                    }
                    .toMap()
            }
        )

    private fun mapToHeader(headerWithCategory: HeaderWithCategoriesEntity, newListPosition: Int): Header {

        val header = headerWithCategory.header
        val sumOfAvailableMoneyInCategory =
            headerWithCategory.categories.sumOf { category -> category.availableMoney }

        return Header(
            id = header.id,
            name = header.name,
            sumOfAvailableMoney = Money(value = sumOfAvailableMoneyInCategory),
            listPosition = newListPosition,
            isExpanded = header.isExpanded
        )
    }

    private fun mapToCategories(headerWithCategory: HeaderWithCategoriesEntity): List<Category> {
        val header = headerWithCategory.header

        return headerWithCategory.categories
            .sortedBy { category -> category.listPosition }
            .mapIndexed { index, categoryEntity ->

                val progress = getProgress(
                    budgetPurpose = categoryEntity.budgetPurpose,
                    budgetTarget = categoryEntity.budgetTarget,
                    assignedMoney = categoryEntity.assignedMoney,
                    availableMoney = categoryEntity.availableMoney
                )
                val spentProgress = getSpentProgress(
                    budgetPurpose = categoryEntity.budgetPurpose,
                    availableMoney = categoryEntity.availableMoney,
                    assignedMoney = categoryEntity.assignedMoney
                )
                val overspentProgress = getOverspentProgress(
                    availableMoney = categoryEntity.availableMoney,
                    assignedMoney = categoryEntity.assignedMoney
                )
                val budgetTargetProgress = getBudgetTargetProgress(
                    budgetTarget = categoryEntity.budgetTarget,
                    availableMoney = categoryEntity.availableMoney,
                    assignedMoney = categoryEntity.assignedMoney
                )

                Category(
                    id = categoryEntity.id,
                    headerId = header.id,
                    emoji = categoryEntity.emoji,
                    name = categoryEntity.name,
                    budgetTarget = if (categoryEntity.budgetTarget != null) Money(value = categoryEntity.budgetTarget) else null,
                    budgetPurpose = categoryEntity.budgetPurpose,
                    assignedMoney = Money(value = categoryEntity.assignedMoney),
                    availableMoney = Money(value = categoryEntity.availableMoney),
                    progress = progress,
                    spentProgress = spentProgress,
                    overspentProgress = overspentProgress,
                    budgetTargetProgress = budgetTargetProgress,
                    optionalText = categoryEntity.optionalText,
                    listPosition = index,
                    isInitialCategory = categoryEntity.isInitialCategory
                )
            }
    }

    private fun getProgress(budgetPurpose: BudgetPurpose, budgetTarget: Double?, assignedMoney: Double, availableMoney: Double): Float {

        fun calculateSpendingProgress() =
            when {
                budgetTarget == null && assignedMoney <= 0.00 -> 0f
                budgetTarget == null -> 1f
                else -> (assignedMoney / budgetTarget).toFloat()
            }

        fun calculateSavingProgress() =
            when {
                budgetTarget == null && assignedMoney <= 0.00 -> 0f
                budgetTarget == null -> 1f
                else -> (availableMoney / budgetTarget).toFloat()
            }

        return when (budgetPurpose) {
            BudgetPurpose.Unknown -> 1f
            BudgetPurpose.Spending -> calculateSpendingProgress()
            BudgetPurpose.Saving -> calculateSavingProgress()
        }

    }

    private fun getSpentProgress(budgetPurpose: BudgetPurpose, availableMoney: Double, assignedMoney: Double): Float {
        if (budgetPurpose != BudgetPurpose.Spending) return 0f

        return (1 - (availableMoney / assignedMoney)).toFloat()
    }

    private fun getOverspentProgress(availableMoney: Double, assignedMoney: Double): Float {
        if (availableMoney >= 0) return 0f

        val absoluteAvailableMoneyValue = abs(availableMoney)
        val overallSpentMoney = absoluteAvailableMoneyValue + assignedMoney
        return (absoluteAvailableMoneyValue / overallSpentMoney).toFloat()
    }

    private fun getBudgetTargetProgress(budgetTarget: Double?, availableMoney: Double, assignedMoney: Double): Float? {
        if (budgetTarget == null) return null

        return if (budgetTarget < assignedMoney) (budgetTarget / availableMoney).toFloat() else null
    }
}