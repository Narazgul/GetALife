package app.tinygiants.getalife.domain.usecase.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

class AddCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(headerId: Long, categoryName: String) {

        val categoryEntity = withContext(defaultDispatcher) {
            CategoryEntity(
                id = Random.nextLong(),
                headerId = headerId,
                name = categoryName,
                budgetTarget = 0.00,
                availableMoney = 0.00,
                optionalText = ""
            )
        }

        repository.addCategory(categoryEntity = categoryEntity)
    }
}