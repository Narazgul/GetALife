package app.tinygiants.getalife.domain.usecase.categories.category

import app.tinygiants.getalife.MainCoroutineExtension
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.data.remote.ai.AiRepositoryFake
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.usecase.emoji.AddEmojiToCategoryNameUseCase
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class UpdateCategoryUseCaseTest {

    private lateinit var updateCategory: UpdateCategoryUseCase
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake
    private lateinit var addEmoji: AddEmojiToCategoryNameUseCase

    companion object {
        @JvmField
        @RegisterExtension
        val mainCoroutineExtension: MainCoroutineExtension = MainCoroutineExtension()
    }

    @BeforeEach
    fun setUp() {
        categoryRepositoryFake = CategoryRepositoryFake()
        addEmoji = AddEmojiToCategoryNameUseCase(
            aiRepository = AiRepositoryFake(),
            repository = categoryRepositoryFake
        )

        updateCategory = UpdateCategoryUseCase(
            repository = categoryRepositoryFake,
            addEmoji = addEmoji,
            defaultDispatcher = mainCoroutineExtension.testDispatcher
        )
    }

    @Test
    fun `Reduce assigned money in rent category`(): Unit = runTest {
        categoryRepositoryFake.categories.value = categories

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
                fundBudgetTargetProgress = 0f,
                spentProgress = 0f,
                overspentProgress = 0f,
                budgetTargetProgress = 0f,
                optionalText = optionalText,
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }

        val updatedCategory = category.copy(assignedMoney = Money(100.0))
        updateCategory(updatedCategory)

        val categories = categoryRepositoryFake.categories.value
        assertThat(categories).hasSize(20)
        assertThat(categories.first().name).isEqualTo(rentCategoryEntity().name)
        assertThat(categories.first().assignedMoney).isEqualTo(100.0)
    }

    @Test
    fun `Keep assigned money in rent category`(): Unit = runTest {
        categoryRepositoryFake.categories.value = categories

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
                fundBudgetTargetProgress = 0f,
                spentProgress = 0f,
                overspentProgress = 0f,
                budgetTargetProgress = 0f,
                optionalText = optionalText,
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }

        val updatedCategory = category.copy(assignedMoney = Money(value = category.assignedMoney.value))
        updateCategory(updatedCategory)

        val categories = categoryRepositoryFake.categories.value
        assertThat(categories).hasSize(20)
        assertThat(categories.first().name).isEqualTo(rentCategoryEntity().name)
        assertThat(categories.first().assignedMoney).isEqualTo(1200.0)
    }

    @Test
    fun `Raise assigned money in rent category`(): Unit = runTest {
        categoryRepositoryFake.categories.value = categories

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
                fundBudgetTargetProgress = 0f,
                spentProgress = 0f,
                overspentProgress = 0f,
                budgetTargetProgress = 0f,
                optionalText = optionalText,
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }

        val updatedCategory = category.copy(assignedMoney = Money(value = 1500.0))
        updateCategory(updatedCategory)

        val categories = categoryRepositoryFake.categories.value
        assertThat(categories).hasSize(20)
        assertThat(categories.first().name).isEqualTo(rentCategoryEntity().name)
        assertThat(categories.first().assignedMoney).isEqualTo(1500.0)
    }
}