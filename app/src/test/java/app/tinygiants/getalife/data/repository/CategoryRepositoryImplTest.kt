package app.tinygiants.getalife.data.repository

import app.cash.turbine.test
import app.tinygiants.getalife.data.local.dao.CategoryDaoFake
import app.tinygiants.getalife.data.local.datagenerator.categories
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
    private lateinit var fakeDao: CategoryDaoFake

    @BeforeEach
    fun setUp() {
        fakeDao = CategoryDaoFake()
        repository = CategoryRepositoryImpl(fakeDao)
    }

    @Test
    fun `Test CategoriesFlow`(): Unit = runTest {
        repository.getCategoriesFlow().test {
            val initialEmission = awaitItem()
            assertThat(initialEmission).hasSize(0)

            fakeDao.addCategory(rentCategoryEntity())
            val emission1 = awaitItem()
            assertThat(emission1).hasSize(1)
            assertThat(emission1.first().name).isEqualTo(rentCategoryEntity().name)

            fakeDao.updateCategory(rentCategoryEntity().copy(name = "UpdatedRent"))
            val emission2 = awaitItem()
            assertThat(emission2).hasSize(1)
            assertThat(emission2.first().name).isEqualTo("UpdatedRent")

            fakeDao.deleteCategory(rentCategoryEntity())
            val emission3 = awaitItem()
            assertThat(emission3).isEmpty()

            fakeDao.categories.value = categories
            val emission4 = awaitItem()
            assertThat(emission4).hasSize(20)
        }
    }


    @Test
    fun `Get Categories in Group`(): Unit = runTest {
        val emptyList = repository.getCategoriesInGroup(groupId = 1)
        assertThat(emptyList).isEmpty()

        fakeDao.categories.value = categories
        val categoriesFromGroup1 = fakeDao.getCategoriesInGroup(groupId = 1)

        assertThat(categoriesFromGroup1).isNotNull()
        assertThat(categoriesFromGroup1).hasSize(5)
        assertThat(categoriesFromGroup1.first()).isEqualTo(rentCategoryEntity())
    }

    @Test
    fun `Add Category`(): Unit = runTest {
        repository.addCategory(rentCategoryEntity())

        val categories = fakeDao.categories.value

        assertThat(categories).hasSize(1)
        assertThat(categories.first()).isEqualTo(rentCategoryEntity())
    }

    @Test
    fun `Update Category`(): Unit = runTest {
        fakeDao.categories.value = categories

        val updatedCategory = rentCategoryEntity().copy(name = "UpdatedRent")
        repository.updateCategory(categoryEntity = updatedCategory)

        val categoryUnderTest = fakeDao.categories.value.find { it.id == rentCategoryEntity().id }
        assertThat(categoryUnderTest?.name).isEqualTo("UpdatedRent")
    }

    @Test
    fun `Delete Category`(): Unit = runTest {
        fakeDao.categories.value = categories
        val categoriesBeforeDeletion = fakeDao.categories.value
        assertThat(categoriesBeforeDeletion).hasSize(20)
        assertThat(categoriesBeforeDeletion.find {  it.id == rentCategoryEntity().id }).isEqualTo(rentCategoryEntity())

        repository.deleteCategory(rentCategoryEntity())

        val categories = fakeDao.categories.value
        assertThat(categories).hasSize(19)
        assertThat(categories.find { it.id == rentCategoryEntity().id }).isNull()
        assertThat(categories.first().id).isEqualTo(2L)
    }

}