package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.GroupEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.BudgetPurpose
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs

class GetBudgetUseCase @Inject constructor(
    private val groupsRepository: GroupRepository,
    private val categoryRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<Result<Map<Group, List<Category>>>> {
        return flow {
            val groupsFlow = groupsRepository.getGroupsFlow()
            val categoriesFlow = categoryRepository.getCategoriesFlow()

            groupsFlow.combine(categoriesFlow) { groups, categories ->
                mapToGroupsWithCategories(
                    groupEntities = groups,
                    categoryEntities = categories
                )
            }
                .catch { throwable -> emit(Result.failure(throwable)) }
                .collect { groups -> emit(groups) }
        }
    }

    private suspend fun mapToGroupsWithCategories(groupEntities: List<GroupEntity>, categoryEntities: List<CategoryEntity>) =
        Result.success(
            withContext(defaultDispatcher) {

                val groupedCategories = categoryEntities.groupBy { category -> category.groupId }
                val unsortedGroups = groupEntities.associateWith { groupEntity ->
                    groupedCategories[groupEntity.id]
                        ?.sortedBy { it.listPosition }
                        ?: emptyList()
                }
                val sortedGroups = unsortedGroups.toSortedMap(compareBy { groupEntity -> groupEntity.listPosition })

                sortedGroups.map { grouped ->

                    val group = mapToGroup(group = grouped.key, categories = grouped.value)
                    val categories = mapToCategories(categories = grouped.value)

                    group to categories
                }.toMap()
            }
        )

    private fun mapToGroup(group: GroupEntity, categories: List<CategoryEntity>): Group {

        val sumOfAvailableMoneyInCategory = categories.sumOf { category -> category.availableMoney }

        return Group(
            id = group.id,
            name = group.name,
            sumOfAvailableMoney = Money(value = sumOfAvailableMoneyInCategory),
            listPosition = group.listPosition,
            isExpanded = group.isExpanded
        )
    }

    private fun mapToCategories(categories: List<CategoryEntity>): List<Category> {
        return categories.mapIndexed { index, categoryEntity ->

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
                groupId = categoryEntity.groupId,
                emoji = categoryEntity.emoji,
                name = categoryEntity.name,
                budgetTarget = Money(value = categoryEntity.budgetTarget ?: 0.0),
                budgetPurpose = categoryEntity.budgetPurpose,
                assignedMoney = Money(value = categoryEntity.assignedMoney),
                availableMoney = Money(value = categoryEntity.availableMoney),
                progress = progress,
                spentProgress = spentProgress,
                overspentProgress = overspentProgress,
                budgetTargetProgress = budgetTargetProgress,
                optionalText = categoryEntity.optionalText,
                listPosition = index,
                isInitialCategory = categoryEntity.isInitialCategory,
                updatedAt = categoryEntity.updatedAt,
                createdAt = categoryEntity.createdAt
            )
        }
    }

    private fun getProgress(
        budgetPurpose: BudgetPurpose,
        budgetTarget: Double?,
        assignedMoney: Double,
        availableMoney: Double
    ): Float {

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