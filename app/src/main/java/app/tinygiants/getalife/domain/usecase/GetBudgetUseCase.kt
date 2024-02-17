package app.tinygiants.getalife.domain.usecase

import app.tinygiants.getalife.data.local.entities.HeaderWithCategoriesEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Header
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetBudgetUseCase @Inject constructor(
    private val repository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<Result<Map<Header, List<Category>>>> {
        return flow {
            repository.getBudget()
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

                        val header = mapToHeader(headerWithCategory = headerWithCategoriesEntity, newListPosition = index)
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
                val progress = (categoryEntity.availableMoney / categoryEntity.budgetTarget).toFloat()

                Category(
                    id = categoryEntity.id,
                    headerId = header.id,
                    name = categoryEntity.name,
                    budgetTarget = Money(value = categoryEntity.budgetTarget),
                    availableMoney = Money(value = categoryEntity.availableMoney),
                    progress = progress,
                    optionalText = categoryEntity.optionalText,
                    listPosition = index,
                    isEmptyCategory = categoryEntity.isEmptyCategory
                )
            }
    }
}