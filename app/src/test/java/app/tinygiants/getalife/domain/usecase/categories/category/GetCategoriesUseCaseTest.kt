package app.tinygiants.getalife.domain.usecase.categories.category

import app.cash.turbine.test
import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.personalCareCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class GetCategoriesUseCaseTest {

    private lateinit var getCategories: GetCategoriesUseCase
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        categoryRepositoryFake = CategoryRepositoryFake()

        getCategories = GetCategoriesUseCase(
            repository = categoryRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `Get Categories`(): Unit = runTest {
        categoryRepositoryFake.categories.value = categories

        getCategories().test {
            val categories = awaitItem().getOrThrow()

            assertThat(categories).hasSize(20)
            assertThat(categories.first().name).isEqualTo(rentCategoryEntity().name)
            assertThat(categories.last().name).isEqualTo(personalCareCategoryEntity().name)
        }
    }
}