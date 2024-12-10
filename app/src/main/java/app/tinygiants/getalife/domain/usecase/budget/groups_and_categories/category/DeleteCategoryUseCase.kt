package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category

import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {

    suspend operator fun invoke(category: Category) =
        repository.deleteCategory(category = category)
}