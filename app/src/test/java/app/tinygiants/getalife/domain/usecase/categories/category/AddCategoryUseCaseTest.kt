package app.tinygiants.getalife.domain.usecase.categories.category

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.remote.ai.AiRepositoryFake
import app.tinygiants.getalife.domain.model.BudgetPurpose
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.usecase.emoji.AddEmojiToCategoryNameUseCase
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AddCategoryUseCaseTest {

    private lateinit var addCategory: AddCategoryUseCase
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake
    private lateinit var addEmojiToCategory: AddEmojiToCategoryNameUseCase
    private lateinit var aiRepositoryFake: AiRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        categoryRepositoryFake = CategoryRepositoryFake()
        aiRepositoryFake = AiRepositoryFake()
        addEmojiToCategory = AddEmojiToCategoryNameUseCase(
            aiRepository = aiRepositoryFake,
            repository = categoryRepositoryFake
        )

        addCategory = AddCategoryUseCase(
            repository = categoryRepositoryFake,
            addEmoji = addEmojiToCategory,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `Add initial category`(): Unit = runTest {
        addCategory(groupId = 1, categoryName = "", isInitialCategory = true)

        val categories = categoryRepositoryFake.categories.value
        assertThat(categories).hasSize(1)
        assertThat(categories.first().name).isEqualTo("")
        assertThat(categories.first().groupId).isEqualTo(1L)
        assertThat(categories.first().emoji).isEqualTo("")
        assertThat(categories.first().isInitialCategory).isTrue()
        assertThat(categories.first().budgetPurpose).isEqualTo(BudgetPurpose.Unknown)
        assertThat(categories.first().budgetTarget).isNull()
        assertThat(categories.first().assignedMoney).isEqualTo(0.0)
        assertThat(categories.first().availableMoney).isEqualTo(0.0)
        assertThat(categories.first().optionalText).isEqualTo("")
        assertThat(categories.first().listPosition).isEqualTo(0)
    }

    @Test
    fun `Add Rent category to Group 1`(): Unit = runTest {
        addCategory(groupId = 1, categoryName = "Rent")

        val categories = categoryRepositoryFake.categories.value
        assertThat(categories).hasSize(1)
        assertThat(categories.first().name).isEqualTo("Rent")
        assertThat(categories.first().groupId).isEqualTo(1L)
        assertThat(categories.first().emoji).isEqualTo("✅")
        assertThat(categories.first().isInitialCategory).isFalse()
        assertThat(categories.first().budgetPurpose).isEqualTo(BudgetPurpose.Unknown)
        assertThat(categories.first().budgetTarget).isNull()
        assertThat(categories.first().assignedMoney).isEqualTo(0.0)
        assertThat(categories.first().availableMoney).isEqualTo(0.0)
        assertThat(categories.first().optionalText).isEqualTo("")
        assertThat(categories.first().listPosition).isEqualTo(0)
    }

    @Test
    fun `Test second category gets listPosition 1`(): Unit = runTest {
        addCategory(groupId = 1, categoryName = "Rent")
        addCategory(groupId = 1, categoryName = "Gym")

        val categories = categoryRepositoryFake.categories.value

        assertThat(categories).hasSize(2)
        assertThat(categories[0].name).isEqualTo("Rent")
        assertThat(categories[1].name).isEqualTo("Gym")
        assertThat(categories[0].groupId).isEqualTo(1)
        assertThat(categories[1].groupId).isEqualTo(1)
        assertThat(categories[0].listPosition).isEqualTo(0)
        assertThat(categories[1].listPosition).isEqualTo(1)
    }
}