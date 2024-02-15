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

                headersWithCategories.associate { headerWithCategory ->

                    val header = mapToHeader(headerWithCategory = headerWithCategory)
                    val categories = mapToCategories(headerWithCategory = headerWithCategory)

                    header to categories
                }
            }
        )

    private fun mapToHeader(headerWithCategory: HeaderWithCategoriesEntity): Header {

        val header = headerWithCategory.header
        val sumOfAvailableMoneyInCategory =
            headerWithCategory.categories.sumOf { category -> category.availableMoney }

        return Header(
            id = header.id,
            name = header.name,
            sumOfAvailableMoney = Money(value = sumOfAvailableMoneyInCategory),
            isExpanded = header.isExpanded
        )
    }

    private fun mapToCategories(headerWithCategory: HeaderWithCategoriesEntity): List<Category> {
        val header = headerWithCategory.header

        return headerWithCategory.categories.map { category ->
            val progress = (category.availableMoney / category.budgetTarget).toFloat()

            Category(
                id = category.id,
                headerId = header.id,
                name = category.name,
                budgetTarget = Money(value = category.budgetTarget),
                availableMoney = Money(value = category.availableMoney),
                progress = progress,
                optionalText = category.optionalText
            )
        }
    }
}