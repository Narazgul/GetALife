package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.aldiGroceriesJanuary
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.groceriesCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.transactions
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import app.tinygiants.getalife.domain.usecase.budget.RecalculateCategoryMonthlyStatusUseCase
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class UpdateTransactionUseCaseTest {
    private lateinit var updateTransaction: UpdateTransactionUseCase
    private lateinit var transactionRepositoryFake: TransactionRepositoryFake
    private lateinit var accountRepositoryFake: AccountRepositoryFake
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        accountRepositoryFake = AccountRepositoryFake()
        categoryRepositoryFake = CategoryRepositoryFake()
        val statusRepositoryFake = CategoryMonthlyStatusRepositoryFake()
        transactionRepositoryFake = TransactionRepositoryFake(accountRepositoryFake, categoryRepositoryFake)
        val recalculateCategoryMonthlyStatusUseCase = RecalculateCategoryMonthlyStatusUseCase(
            statusRepository = statusRepositoryFake,
            transactionRepository = transactionRepositoryFake,
            categoryRepository = categoryRepositoryFake
        )

        updateTransaction = UpdateTransactionUseCase(
            transactionRepository = transactionRepositoryFake,
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher,
            recalculateCategoryMonthlyStatusUseCase = recalculateCategoryMonthlyStatusUseCase
        )

        transactionRepositoryFake.transactions.value = transactions
        accountRepositoryFake.accounts.value = listOf(cashAccount().toDomain())
        categoryRepositoryFake.categories.value = listOf(groceriesCategoryEntity().toDomain(), rentCategoryEntity().toDomain())
    }

    @Test
    fun `Test changing nothing on the update`() = runTest {
        val account = cashAccount().toDomain()
        val category = groceriesCategoryEntity().toDomain()
        val transaction = aldiGroceriesJanuary().toDomain(account, category)
        updateTransaction(transaction)

        assertThat(transactionRepositoryFake.transactions.value).contains(transaction)
        assertThat(
            transactionRepositoryFake.transactions.value.find { it.id == aldiGroceriesJanuary().id }!!.amount
        ).isEqualTo(
            aldiGroceriesJanuary().toDomain(account, category).amount
        )
        assertThat(accountRepositoryFake.accounts.value.find { it.id == account.id }!!.balance).isEqualTo(cashAccount().toDomain().balance)
    }

    @Test
    fun `Update transaction with new amount and new category`(): Unit = runTest {
        val cashAccount = cashAccount().toDomain()
        val groceriesCategory = groceriesCategoryEntity().toDomain()
        val rentCategory = rentCategoryEntity().toDomain()
        val originalTransaction = aldiGroceriesJanuary().toDomain(cashAccount, groceriesCategory)
        val updatedTransaction = originalTransaction.copy(amount = Money(-40.0), category = rentCategory)

        updateTransaction(updatedTransaction)

        val accountBalance = accountRepositoryFake.accounts.value.find { it.id == cashAccount.id }!!.balance
        val transactionAmount = transactionRepositoryFake.transactions.value.find { it.id == originalTransaction.id }!!.amount

        assertThat(accountBalance).isEqualTo(Money(510.0))
        assertThat(transactionAmount).isEqualTo(Money(-40.0))
    }

    @Test
    fun `Update transaction with new amount and same category`(): Unit = runTest {
        val cashAccount = cashAccount().toDomain()
        val groceriesCategory = groceriesCategoryEntity().toDomain()
        val originalTransaction = aldiGroceriesJanuary().toDomain(cashAccount, groceriesCategory)
        val updatedTransaction = originalTransaction.copy(amount = Money(-60.0))

        updateTransaction(updatedTransaction)

        val accountBalance = accountRepositoryFake.accounts.value.find { it.id == cashAccount.id }!!.balance
        val transactionAmount = transactionRepositoryFake.transactions.value.find { it.id == originalTransaction.id }!!.amount

        assertThat(accountBalance).isEqualTo(Money(490.0))
        assertThat(transactionAmount).isEqualTo(Money(-60.0))
    }
}