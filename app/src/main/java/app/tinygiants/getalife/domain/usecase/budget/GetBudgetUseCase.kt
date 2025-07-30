package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.CalculateCategoryProgressUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetBudgetUseCase @Inject constructor(
    private val groupsRepository: GroupRepository,
    private val categoryRepository: CategoryRepository,
    private val calculateProgress: CalculateCategoryProgressUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<Result<Map<Group, List<Category>>>> = flow {
        val groupsFlow = groupsRepository.getGroupsFlow()
        val categoriesFlow = categoryRepository.getCategoriesFlow()

        groupsFlow.combine(categoriesFlow) { groups, categories ->
            mapToGroupsWithCategories(
                groups = groups,
                categories = categories
            )
        }
            .catch { throwable -> emit(Result.failure(throwable)) }
            .collect { groups -> emit(groups) }
    }

    private suspend fun mapToGroupsWithCategories(groups: List<Group>, categories: List<Category>) =
        Result.success(
            withContext(defaultDispatcher) {

                val groupedCategories = categories.groupBy { category -> category.groupId }
                val unsortedGroups = groups.associateWith { group ->
                    groupedCategories[group.id]
                        ?.sortedBy { it.listPosition }
                        ?: emptyList()
                }
                val sortedGroups = unsortedGroups.toSortedMap(compareBy { group -> group.listPosition })

                sortedGroups.map { (group, categories) ->

                    val updatedGroup = groupWithSumOfAvailableMoney(group = group, categories = categories)
                    val updatedCategory = categoriesWithProgressAndListPosition(categories = categories)

                    updatedGroup to updatedCategory

                }.toMap()
            }
        )

    private fun groupWithSumOfAvailableMoney(group: Group, categories: List<Category>): Group {
        // TODO: Summe muss aus MonthlyBudget berechnet werden
        return group.copy(sumOfAvailableMoney = Money(value = 0.0))
    }

    private suspend fun categoriesWithProgressAndListPosition(categories: List<Category>): List<Category> {
        return categories.mapIndexed { index, category ->
            // TODO: Für vollständige Integration müsste hier MonthlyBudget und spentThisMonth übergeben werden
            // Vorerst verwenden wir EmptyProgress, da diese UseCase nicht direkt auf MonthlyBudget-Daten zugreift
            // Die richtige Progress-Berechnung erfolgt in GetBudgetForMonthUseCase
            val progress = EmptyProgress()

            category.copy(progress = progress, listPosition = index)
        }
    }
}