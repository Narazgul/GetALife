package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Header
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getBudget(): Flow<Result<List<Group>>>

    suspend fun addHeader(name: String)
    suspend fun toggleIsExpanded(header: Header)
    suspend fun updateHeader(header: Header)
    suspend fun deleteHeader(header: Header)

    suspend fun addCategory(headerId: Long, categoryName: String)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
}