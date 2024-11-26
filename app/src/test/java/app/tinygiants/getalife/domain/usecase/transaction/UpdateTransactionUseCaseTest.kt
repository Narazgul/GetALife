package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.aldiGroceriesJanuary
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.checkingAccount
import app.tinygiants.getalife.data.local.datagenerator.groceriesCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.techCorpSalaryJanuary
import app.tinygiants.getalife.data.local.datagenerator.toAccount
import app.tinygiants.getalife.data.local.datagenerator.toCategory
import app.tinygiants.getalife.data.local.datagenerator.toTransaction
import app.tinygiants.getalife.data.local.datagenerator.transactions
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.BudgetRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import app.tinygiants.getalife.domain.usecase.budget.UpdateAssignableMoneyUseCase
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
    private lateinit var budgetRepositoryFake: BudgetRepositoryFake
    private lateinit var updateAssignableMoney: UpdateAssignableMoneyUseCase

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        transactionRepositoryFake = TransactionRepositoryFake()
        accountRepositoryFake = AccountRepositoryFake()
        categoryRepositoryFake = CategoryRepositoryFake()
        budgetRepositoryFake = BudgetRepositoryFake()
        updateAssignableMoney = UpdateAssignableMoneyUseCase(
            repository = budgetRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )

        updateTransaction = UpdateTransactionUseCase(
            updateAssignableMoney = updateAssignableMoney,
            transactionRepository = transactionRepositoryFake,
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )

        transactionRepositoryFake.transactions.value = transactions
        accountRepositoryFake.accountsFlow.value = accounts
        categoryRepositoryFake.categories.value = categories
    }

    @Test
    fun `Test changing nothing on the update`(): Unit = runTest {
        val account = cashAccount().toAccount()
        val category = groceriesCategoryEntity().toCategory()
        val updatedTransaction = aldiGroceriesJanuary().toTransaction(account = account, category = category)

        updateTransaction(updatedTransaction)

        assertThat(transactionRepositoryFake.transactions.value).contains(aldiGroceriesJanuary())
        assertThat(transactionRepositoryFake.transactions.value[updatedTransaction.id.toInt() - 1].amount).isEqualTo(
            aldiGroceriesJanuary().amount
        )
        assertThat(accountRepositoryFake.accountsFlow.value[account.id.toInt() - 1].balance).isEqualTo(cashAccount().balance)
        assertThat(categoryRepositoryFake.categories.value[category.id.toInt() - 1].availableMoney).isEqualTo(
            groceriesCategoryEntity().availableMoney
        )
    }

    @Test
    fun `Spent less on Outflow Transaction Amount`(): Unit = runTest {
        val account = cashAccount().toAccount()
        val category = rentCategoryEntity().toCategory()
        val updatedTransaction =
            aldiGroceriesJanuary().toTransaction(account = account, category = category).copy(amount = Money(-40.0))

        updateTransaction(updatedTransaction)

        val accountBalance = accountRepositoryFake.accountsFlow.value.find { it.id == cashAccount().id }!!.balance
        val categoryAvailableMoney =
            categoryRepositoryFake.categories.value.find { it.id == rentCategoryEntity().id }!!.availableMoney
        val transactionAmount = transactionRepositoryFake.transactions.value.find { it.id == aldiGroceriesJanuary().id }!!.amount

        assertThat(accountBalance).isEqualTo(510.0)
        assertThat(categoryAvailableMoney).isEqualTo(1310.0)
        assertThat(transactionAmount).isEqualTo(-40.0)
    }

    @Test
    fun `Spent more on Outflow Transaction Amount`(): Unit = runTest {
        val account = cashAccount().toAccount()
        val category = rentCategoryEntity().toCategory()
        val updatedTransaction = aldiGroceriesJanuary().toTransaction(account = account, category = category).copy(amount = Money(-60.0))

        updateTransaction(updatedTransaction)

        val accountBalance = accountRepositoryFake.accountsFlow.value.find { it.id == cashAccount().id }!!.balance
        val categoryAvailableMoney =
            categoryRepositoryFake.categories.value.find { it.id == rentCategoryEntity().id }!!.availableMoney
        val transactionAmount = transactionRepositoryFake.transactions.value.find { it.id == aldiGroceriesJanuary().id }!!.amount

        assertThat(accountBalance).isEqualTo(490.0)
        assertThat(categoryAvailableMoney).isEqualTo(1290.0)
        assertThat(transactionAmount).isEqualTo(-60.0)
    }

    @Test
    fun `Raise Inflow Transaction Amount`(): Unit = runTest {
        budgetRepositoryFake.budgetsFlow.value = listOf(budgetRepositoryFake.initialBudget.copy(readyToAssign = 2000.0))
        val account = checkingAccount().toAccount()
        val updatedTransaction = techCorpSalaryJanuary().toTransaction(account = account).copy(amount = Money(2500.0))

        updateTransaction(updatedTransaction)

        val assignableMoney = budgetRepositoryFake.budgetsFlow.value.first().readyToAssign
        assertThat(assignableMoney).isEqualTo(2500.0)
    }

    @Test
    fun `Lower Inflow transaction amount`(): Unit = runTest {
        budgetRepositoryFake.budgetsFlow.value = listOf(budgetRepositoryFake.initialBudget.copy(readyToAssign = 2000.0))
        val account = checkingAccount().toAccount()
        val updatedTransaction = techCorpSalaryJanuary().toTransaction(account = account).copy(amount = Money(1500.0))

        updateTransaction(updatedTransaction)

        val assignableMoney = budgetRepositoryFake.budgetsFlow.value.first().readyToAssign
        assertThat(assignableMoney).isEqualTo(1500.0)
    }
}