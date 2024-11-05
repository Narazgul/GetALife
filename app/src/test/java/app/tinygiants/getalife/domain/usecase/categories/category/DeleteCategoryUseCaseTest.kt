package app.tinygiants.getalife.domain.usecase.categories.category

import app.tinygiants.getalife.MainCoroutineExtension
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class DeleteCategoryUseCaseTest {

    private lateinit var deleteCategory: DeleteCategoryUseCase
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val mainCoroutineExtension: MainCoroutineExtension = MainCoroutineExtension()
    }

    @BeforeEach
    fun setUp() {
        categoryRepositoryFake = CategoryRepositoryFake()

        deleteCategory = DeleteCategoryUseCase(
            repository = categoryRepositoryFake,
            defaultDispatcher = mainCoroutineExtension.testDispatcher
        )

        categoryRepositoryFake.categories.value = categories
    }

    @Test
    fun `Remove category`(): Unit = runTest {

        assertThat(categoryRepositoryFake.categories.value).hasSize(20)

        val category = rentCategoryEntity().run {
            Category(
                id = id,
                groupId = groupId,
                emoji = emoji,
                name = name,
                budgetTarget = Money(budgetTarget ?: 0.0),
                budgetPurpose = budgetPurpose,
                assignedMoney = Money(assignedMoney),
                availableMoney = Money(availableMoney),
                optionalText = optionalText,
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = updatedAt,
                createdAt = createdAt,
                progress = 0f,
                spentProgress = 0f,
                overspentProgress = 0f,
                budgetTargetProgress = 0f
            )
        }

        deleteCategory(category = category)

        val categoriesAfterDeletion = categoryRepositoryFake.categories.value
        assertThat(categoriesAfterDeletion).hasSize(19)
        assertThat(categoriesAfterDeletion.first().name).isEqualTo("Studienkredittilgung")
    }
}