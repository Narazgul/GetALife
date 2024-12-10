package app.tinygiants.getalife.data.repository

import app.cash.turbine.test
import app.tinygiants.getalife.data.local.dao.CategoryDaoFake
import app.tinygiants.getalife.data.local.dao.TransactionDaoFake
import app.tinygiants.getalife.data.local.datagenerator.categoryEntities
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CategoryRepositoryImplTest {

    private lateinit var repository: CategoryRepositoryImpl
    private lateinit var categoryDaoFake: CategoryDaoFake
    private lateinit var transactionDaoFake: TransactionDaoFake

    @BeforeEach
    fun setUp() {
        categoryDaoFake = CategoryDaoFake()
        transactionDaoFake = TransactionDaoFake()
        repository = CategoryRepositoryImpl(categoryDao = categoryDaoFake)
    }

    @Test
    fun `Test CategoriesFlow`(): Unit = runTest {
        repository.getCategoriesFlow().test {
            val initialEmission = awaitItem()
            assertThat(initialEmission).hasSize(0)

            categoryDaoFake.addCategory(rentCategoryEntity())
            val emission1 = awaitItem()
            assertThat(emission1).hasSize(1)
            assertThat(emission1.first().name).isEqualTo(rentCategoryEntity().name)

            categoryDaoFake.updateCategory(rentCategoryEntity().copy(name = "UpdatedRent"))
            val emission2 = awaitItem()
            assertThat(emission2).hasSize(1)
            assertThat(emission2.first().name).isEqualTo("UpdatedRent")

            categoryDaoFake.deleteCategory(rentCategoryEntity())
            val emission3 = awaitItem()
            assertThat(emission3).isEmpty()

            categoryDaoFake.categories.value = categoryEntities
            val emission4 = awaitItem()
            assertThat(emission4).hasSize(20)
        }
    }


    @Test
    fun `Get Categories in Group`(): Unit = runTest {
        val emptyList = repository.getCategoriesInGroup(groupId = 1)
        assertThat(emptyList).isEmpty()

        categoryDaoFake.categories.value = categoryEntities
        val categoriesFromGroup1 = categoryDaoFake.getCategoriesInGroup(groupId = 1)

        assertThat(categoriesFromGroup1).isNotNull()
        assertThat(categoriesFromGroup1).hasSize(5)
        assertThat(categoriesFromGroup1.first()).isEqualTo(rentCategoryEntity())
    }

    @Test
    fun `Add Category`(): Unit = runTest {
        repository.addCategory(rentCategoryEntity().toDomain())

        val categories = categoryDaoFake.categories.value

        assertThat(categories).hasSize(1)
        assertThat(categories.first()).isEqualTo(rentCategoryEntity())
    }

    @Test
    fun `Update Category`(): Unit = runTest {
        categoryDaoFake.categories.value = categoryEntities

        val updatedCategory = rentCategoryEntity().copy(name = "UpdatedRent").toDomain()
        repository.updateCategory(category = updatedCategory)

        val categoryUnderTest = categoryDaoFake.categories.value.find { it.id == rentCategoryEntity().id }
        assertThat(categoryUnderTest?.name).isEqualTo("UpdatedRent")
    }

    @Test
    fun `Delete Category`(): Unit = runTest {
        categoryDaoFake.categories.value = categoryEntities
        val categoriesBeforeDeletion = categoryDaoFake.categories.value
        assertThat(categoriesBeforeDeletion).hasSize(20)
        assertThat(categoriesBeforeDeletion.find { it.id == rentCategoryEntity().id }).isEqualTo(rentCategoryEntity())

        repository.deleteCategory(rentCategoryEntity().toDomain())

        val categories = categoryDaoFake.categories.value
        assertThat(categories).hasSize(19)
        assertThat(categories.find { it.id == rentCategoryEntity().id }).isNull()
        assertThat(categories.first().id).isEqualTo(2L)
    }

}