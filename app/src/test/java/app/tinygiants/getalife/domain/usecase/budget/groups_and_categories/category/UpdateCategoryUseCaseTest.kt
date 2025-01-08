package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.transportCategoryEntity
import app.tinygiants.getalife.data.remote.ai.AiRepositoryFake
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
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
    private lateinit var accountRepositoryFake: AccountRepositoryFake
    private lateinit var transactionRepositoryFake: TransactionRepositoryFake
    private lateinit var addEmoji: AddEmojiToCategoryNameUseCase

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        categoryRepositoryFake = CategoryRepositoryFake()
        accountRepositoryFake = AccountRepositoryFake()
        transactionRepositoryFake = TransactionRepositoryFake(
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake
        )
        addEmoji = AddEmojiToCategoryNameUseCase(
            aiRepository = AiRepositoryFake(),
            repository = categoryRepositoryFake
        )

        updateCategory = UpdateCategoryUseCase(
            categoryRepository = categoryRepositoryFake,
            addEmoji = addEmoji,
            defaultDispatcher = testDispatcherExtension.testDispatcher,
        )
    }

    @Test
    fun `Reduce assigned money in rent category`(): Unit = runTest {
        categoryRepositoryFake.categories.value = categories

        val category = rentCategoryEntity().toDomain()
        val updatedCategory = category.copy(assignedMoney = Money(100.0))


        updateCategory(updatedCategory)

        val categories = categoryRepositoryFake.categories.value
        assertThat(categories).hasSize(20)
        assertThat(categories.first().name).isEqualTo(rentCategoryEntity().name)
        assertThat(categories.first().assignedMoney).isEqualTo(Money(100.0))
        assertThat(categories.first().availableMoney).isEqualTo(Money(200.0))
    }

    @Test
    fun `Reduce assigned money twice`(): Unit = runTest {
        categoryRepositoryFake.categories.value = categories
        val category = rentCategoryEntity().toDomain()
        val updatedCategory = category.copy(assignedMoney = Money(value = 120.0))

        updateCategory(updatedCategory)

        val categoryAfterFirstUpdate = categoryRepositoryFake.categories.value.find { it.id == rentCategoryEntity().id }!!
        assertThat(categoryAfterFirstUpdate.assignedMoney).isEqualTo(Money(120.0))
        assertThat(categoryAfterFirstUpdate.availableMoney).isEqualTo(Money(220.0))

        val updatedCategory2 = updatedCategory.copy(assignedMoney = Money(12.0))
        updateCategory(updatedCategory2)

        val categoryAfterSecondUpdate = categoryRepositoryFake.categories.value.find { it.id == rentCategoryEntity().id }!!
        assertThat(categoryAfterSecondUpdate.assignedMoney).isEqualTo(Money(12.0))
        assertThat(categoryAfterSecondUpdate.availableMoney).isEqualTo(Money(112.0))
    }

    @Test
    fun `Keep assigned money in rent category`(): Unit = runTest {
        categoryRepositoryFake.categories.value = categories
        val category = rentCategoryEntity().toDomain()
        val updatedCategory = category.copy(assignedMoney = category.assignedMoney)
        updateCategory(updatedCategory)

        val categories = categoryRepositoryFake.categories.value
        assertThat(categories).hasSize(20)
        assertThat(categories.first().name).isEqualTo(rentCategoryEntity().name)
        assertThat(categories.first().assignedMoney).isEqualTo(Money(1200.0))
        assertThat(categories.first().availableMoney).isEqualTo(Money(1300.0))
    }

    @Test
    fun `Raise assigned money in rent category`(): Unit = runTest {
        categoryRepositoryFake.categories.value = categories
        val category = rentCategoryEntity().toDomain()
        val updatedCategory = category.copy(assignedMoney = Money(value = 1500.0))

        updateCategory(updatedCategory)

        val categories = categoryRepositoryFake.categories.value
        assertThat(categories).hasSize(20)
        assertThat(categories.first().name).isEqualTo(rentCategoryEntity().name)
        assertThat(categories.first().assignedMoney).isEqualTo(Money(1500.0))
        assertThat(categories.first().availableMoney).isEqualTo(Money(1600.0))
    }

    @Test
    fun `Raise assigned money twice`(): Unit = runTest {
        categoryRepositoryFake.categories.value = categories
        val category = transportCategoryEntity().toDomain()
        val updatedCategory = category.copy(assignedMoney = Money(value = 1.0))

        updateCategory(updatedCategory)

        val categoryAfterFirstUpdate = categoryRepositoryFake.categories.value.find { it.id == transportCategoryEntity().id }!!
        assertThat(categoryAfterFirstUpdate.assignedMoney).isEqualTo(Money(1.0))
        assertThat(categoryAfterFirstUpdate.availableMoney).isEqualTo(Money(1.0))

        val updatedCategory2 = updatedCategory.copy(assignedMoney = Money(10.0))
        updateCategory(updatedCategory2)

        val categoryAfterSecondUpdate = categoryRepositoryFake.categories.value.find { it.id == transportCategoryEntity().id }!!
        assertThat(categoryAfterSecondUpdate.assignedMoney).isEqualTo(Money(10.0))
        assertThat(categoryAfterSecondUpdate.availableMoney).isEqualTo(Money(10.0))
    }
}