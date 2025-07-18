package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import app.tinygiants.getalife.presentation.shared_composables.UiText
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
    private lateinit var accountRepositoryFake: AccountRepositoryFake
    private lateinit var transactionRepositoryFake: TransactionRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        accountRepositoryFake = AccountRepositoryFake()
        categoryRepositoryFake = CategoryRepositoryFake()
        transactionRepositoryFake = TransactionRepositoryFake(
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake
        )

        deleteCategory = DeleteCategoryUseCase(
            categoryRepository = categoryRepositoryFake,
            transactionRepository = transactionRepositoryFake
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
                budgetTarget = Money(budgetTarget),
                assignedMoney = Money(assignedMoney),
                availableMoney = Money(availableMoney),
                progress = EmptyProgress(),
                optionalText = UiText.DynamicString(value = ""),
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = updatedAt,
                createdAt = createdAt,
            )
        }

        deleteCategory(category = category)

        val categoriesAfterDeletion = categoryRepositoryFake.categories.value
        assertThat(categoriesAfterDeletion).hasSize(19)
        assertThat(categoriesAfterDeletion.first().name).isEqualTo("Student loan repayment")
    }
}