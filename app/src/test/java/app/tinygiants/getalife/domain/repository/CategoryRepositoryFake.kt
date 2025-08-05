package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class CategoryRepositoryFake : CategoryRepository {

    val categories = MutableStateFlow<List<Category>>(emptyList())

    override fun getCategoriesFlow(): Flow<List<Category>> = categories

    override suspend fun getCategoriesInGroup(groupId: Long) =
        categories.value.filter { it.groupId == groupId }

    override suspend fun getCategory(categoryId: Long) =
        categories.value.find { it.id == categoryId }

    override suspend fun addCategory(category: Category) =
        categories.update { it + category }

    override suspend fun updateCategory(category: Category) =
        categories.update { current ->
            current.map { if (it.id == category.id) category else it }
        }

    override suspend fun deleteCategory(category: Category) =
        categories.update { it.filterNot { entity -> entity.id == category.id } }

    override suspend fun getCreditCardPaymentCategory(accountId: Long): Category? =
        categories.value.find { it.linkedAccountId == accountId }
}