package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.HeaderEntity
import app.tinygiants.getalife.data.local.entities.HeaderWithCategoriesEntity
import app.tinygiants.getalife.domain.model.BudgetPurpose
import app.tinygiants.getalife.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BudgetRepositoryFake : BudgetRepository {

    private val headersWithCategoriesEntity = mutableListOf<HeaderWithCategoriesEntity>()

    override fun getBudgetFlow(): Flow<Result<List<HeaderWithCategoriesEntity>>> = flow {
        headersWithCategoriesEntity
    }

    override fun getCategoriesFlow(): Flow<Result<List<CategoryEntity>>> {
        return flow { emit(Result.success(headersWithCategoriesEntity.flatMap { it.categories })) }
    }

    override suspend fun getCategoriesOfHeader(headerId: Long): List<CategoryEntity> {
        val categories = mutableListOf<CategoryEntity>()

        headersWithCategoriesEntity.forEach { headerWithCategories ->
            if (headerWithCategories.header.id == headerId) categories.addAll(headerWithCategories.categories)
        }

        return categories
    }

    override suspend fun addHeader(headerEntity: HeaderEntity) {
        val entity = HeaderWithCategoriesEntity(header = headerEntity, categories = emptyList())
        headersWithCategoriesEntity.add(entity)
    }

    override suspend fun updateHeader(headerEntity: HeaderEntity) {
        headersWithCategoriesEntity.forEachIndexed { index, headerWithCategories ->
            if (headerWithCategories.header.id == headerEntity.id) {
                val updatedHeaderWithCategories = headerWithCategories.copy(header = headerEntity)
                headersWithCategoriesEntity[index] = updatedHeaderWithCategories
                return@forEachIndexed
            }
        }
    }

    override suspend fun deleteHeader(headerEntity: HeaderEntity) {
        headersWithCategoriesEntity.removeAll { it.header.id == headerEntity.id }
    }

    override suspend fun getCategory(categoryId: Long) = headersWithCategoriesEntity
            .flatMap { headersWithCategories -> headersWithCategories.categories }
            .find { category -> category.id == categoryId } ?: CategoryEntity(
                id = 1L,
                headerId = 1L,
                emoji = "",
                name = "",
                budgetPurpose = BudgetPurpose.Unknown,
                budgetTarget = 0.00,
                assignedMoney = 0.00,
                availableMoney = 0.00,
                optionalText = "",
                listPosition = 0,
                isInitialCategory = false
            )

    override suspend fun addCategory(categoryEntity: CategoryEntity) {
        val headerWithCategories = headersWithCategoriesEntity.find { it.header.id == categoryEntity.headerId }
        if (headerWithCategories != null) {

            val updatedCategories = headerWithCategories.categories.toMutableList()
            updatedCategories.add(categoryEntity)

            val updatedHeaderWithCategories = headerWithCategories.copy(categories = updatedCategories)

            headersWithCategoriesEntity.remove(headerWithCategories)
            headersWithCategoriesEntity.add(updatedHeaderWithCategories)
        }
    }

    override suspend fun updateCategory(categoryEntity: CategoryEntity) {
        headersWithCategoriesEntity.forEach { headerWithCategories ->

            val updatedCategories = headerWithCategories.categories.map { if (it.id == categoryEntity.id) categoryEntity else it }
            val updatedHeaderWithCategories = headerWithCategories.copy(categories = updatedCategories)

            headersWithCategoriesEntity.remove(headerWithCategories)
            headersWithCategoriesEntity.add(updatedHeaderWithCategories)
        }
    }

    override suspend fun deleteCategory(categoryEntity: CategoryEntity) {
        headersWithCategoriesEntity.forEach { headerWithCategories ->

            val updatedCategories = headerWithCategories.categories.filter { it.id != categoryEntity.id }
            val updatedHeaderWithCategories = headerWithCategories.copy(categories = updatedCategories)

            headersWithCategoriesEntity.remove(headerWithCategories)
            headersWithCategoriesEntity.add(updatedHeaderWithCategories)
        }
    }
}