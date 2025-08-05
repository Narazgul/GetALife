package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getCategoriesFlow(): Flow<List<Category>>
    suspend fun getCategoriesInGroup(groupId: Long): List<Category>
    suspend fun getCategory(categoryId: Long): Category?
    suspend fun addCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
    suspend fun getCreditCardPaymentCategory(accountId: Long): Category?

}