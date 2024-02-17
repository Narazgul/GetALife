package app.tinygiants.getalife.domain.usecase.header

import app.tinygiants.getalife.data.local.entities.HeaderEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.di.Io
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.usecase.category.AddCategoryUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

class AddHeaderUseCase @Inject constructor(
    private val repository: CategoryRepository,
    private val addCategory: AddCategoryUseCase,
    @Io private val ioDispatcher: CoroutineDispatcher,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(headerName: String) {

        val budget = withContext(ioDispatcher) {
            repository.getBudget().first()
        }

        withContext(defaultDispatcher) {

            budget.onSuccess { headersWithCategories ->

                val highestListPosition = headersWithCategories.maxOfOrNull { it.header.listPosition }
                val endOfListPosition = if (highestListPosition == null) 0 else highestListPosition + 1

                val header = HeaderEntity(
                    id = Random.nextLong(),
                    name = headerName,
                    listPosition = endOfListPosition,
                    isExpanded = true
                )
                repository.addHeader(headerEntity = header)
                addCategory(headerId = header.id, categoryName = "Jetzt Kategorie hinzufügen", isEmptyCategory = true)
            }
        }
    }
}