package app.tinygiants.getalife.domain.usecase.categories

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.GroupEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.usecase.categories.category.CalculateCategoryProgressUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCategoriesInGroupsUseCase @Inject constructor(
    private val groupsRepository: GroupRepository,
    private val categoryRepository: CategoryRepository,
    private val calculateProgress: CalculateCategoryProgressUseCase,
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

            val progress = calculateProgress(categoryEntity = categoryEntity)

            categoryEntity.run {
                Category(
                    id = id,
                    groupId = groupId,
                    emoji = emoji,
                    name = name,
                    budgetTarget = Money(value = budgetTarget ?: 0.0),
                    budgetPurpose = budgetPurpose,
                    assignedMoney = Money(value = assignedMoney),
                    availableMoney = Money(value = availableMoney),
                    progress = progress,
                    listPosition = index,
                    isInitialCategory = isInitialCategory,
                    updatedAt = updatedAt,
                    createdAt = createdAt
                )
            }
        }
    }
}