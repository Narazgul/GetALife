package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.GroupEntity
import app.tinygiants.getalife.domain.model.BudgetPurpose
import app.tinygiants.getalife.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CategoryRepositoryFake : CategoryRepository {

    private val headersWithCategoriesEntity = mutableListOf<GroupWithCategoriesEntity>()

    override fun getBudgetFlow(): Flow<Result<List<GroupWithCategoriesEntity>>> = flow {
        headersWithCategoriesEntity
    }

    override fun getCategories(): Flow<Result<List<CategoryEntity>>> {
        return flow { emit(Result.success(headersWithCategoriesEntity.flatMap { it.categories })) }
    }

    override suspend fun getCategoriesInGroup(groupId: Long): List<CategoryEntity> {
        val categories = mutableListOf<CategoryEntity>()

        headersWithCategoriesEntity.forEach { headerWithCategories ->
            if (headerWithCategories.header.id == groupId) categories.addAll(headerWithCategories.categories)
        }

        return categories
    }

    override suspend fun addHeader(groupEntity: GroupEntity) {
        val entity = GroupWithCategoriesEntity(header = groupEntity, categories = emptyList())
        headersWithCategoriesEntity.add(entity)
    }

    override suspend fun updateHeader(groupEntity: GroupEntity) {
        headersWithCategoriesEntity.forEachIndexed { index, headerWithCategories ->
            if (headerWithCategories.header.id == groupEntity.id) {
                val updatedHeaderWithCategories = headerWithCategories.copy(header = groupEntity)
                headersWithCategoriesEntity[index] = updatedHeaderWithCategories
                return@forEachIndexed
            }
        }
    }

    override suspend fun deleteHeader(groupEntity: GroupEntity) {
        headersWithCategoriesEntity.removeAll { it.header.id == groupEntity.id }
    }

    override suspend fun getCategory(categoryId: Long) = headersWithCategoriesEntity
            .flatMap { headersWithCategories -> headersWithCategories.categories }
            .find { category -> category.id == categoryId } ?: CategoryEntity(
                id = 1L,
                groupId = 1L,
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
        val headerWithCategories = headersWithCategoriesEntity.find { it.header.id == categoryEntity.groupId }
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