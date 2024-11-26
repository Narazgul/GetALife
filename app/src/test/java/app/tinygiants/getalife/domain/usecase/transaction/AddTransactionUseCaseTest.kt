package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.BudgetRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Duration.Companion.milliseconds

class AddTransactionUseCaseTest {

    private lateinit var addTransaction: AddTransactionUseCase
    private lateinit var transactionRepositoryFake: TransactionRepositoryFake
    private lateinit var accountRepositoryFake: AccountRepositoryFake
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake
    private lateinit var budgetRepositoryFake: BudgetRepositoryFake

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

        addTransaction = AddTransactionUseCase(
            transactionRepository = transactionRepositoryFake,
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            budgetRepository = budgetRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `add Account creation Transaction`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value = listOf(cashAccount())
        val testBegin = Clock.System.now()
        val shortlyAfterTestBegin = testBegin + 50.milliseconds

        addTransaction(
            amount = Money(value = 10.71),
            direction = TransactionDirection.Inflow,
            accountId = 1L,
            category = null,
            transactionPartner = "",
            description = "Starting balance"
        )
        val transactions = transactionRepositoryFake.transactions.value
        val transaction = transactions.first()

        assertThat(transactions).hasSize(1)
        assertThat(transaction.amount).isEqualTo(10.71)
        assertThat(transaction.transactionDirection).isEqualTo(TransactionDirection.Inflow)
        assertThat(transaction.categoryId).isNull()
        assertThat(transaction.updatedAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)
        assertThat(transaction.createdAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)
    }

    @Test
    fun `add Outflow transaction`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value = accounts
        categoryRepositoryFake.categories.value = categories
        val testBegin = Clock.System.now()
        val shortlyAfterTestBegin = testBegin + 50.milliseconds
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
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }

        addTransaction(
            amount = Money(value = 50.81),
            direction = TransactionDirection.Outflow,
            accountId = 1L,
            category = category,
            transactionPartner = "Landlord",
            description = "Rent"
        )

        val transactionsAfterTest = transactionRepositoryFake.transactions.value
        val transaction = transactionsAfterTest.first()

        assertThat(transactionsAfterTest).hasSize(1)
        assertThat(transaction.amount).isEqualTo(-50.81)
        assertThat(transaction.transactionDirection).isEqualTo(TransactionDirection.Outflow)
        assertThat(transaction.accountId).isEqualTo(1)
        assertThat(transaction.categoryId).isEqualTo(1)
        assertThat(transaction.transactionPartner).isEqualTo("Landlord")
        assertThat(transaction.description).isEqualTo("Rent")
        assertThat(transaction.updatedAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)
        assertThat(transaction.createdAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)

        val accountAfterTransaction = accountRepositoryFake.accountsFlow.value.first()
        assertThat(accountAfterTransaction.name).isEqualTo("Cash Account")
        assertThat(accountAfterTransaction.balance).isEqualTo(449.19)
        assertThat(accountAfterTransaction.updatedAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)

        val categoryAfterTransaction = categoryRepositoryFake.categories.value.first()
        assertThat(categoryAfterTransaction.name).isEqualTo("Rent")
        assertThat(categoryAfterTransaction.budgetTarget).isEqualTo(1200.0)
        assertThat(categoryAfterTransaction.assignedMoney).isEqualTo(1200.0)
        assertThat(categoryAfterTransaction.availableMoney).isEqualTo(1249.19)
    }
}