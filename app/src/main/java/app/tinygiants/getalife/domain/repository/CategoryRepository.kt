package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.CategoryHeader
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun fetchCategories(): Flow<Map<CategoryHeader, List<Category>>>

    suspend fun toggleIsExpended(categoryId: Int)
}