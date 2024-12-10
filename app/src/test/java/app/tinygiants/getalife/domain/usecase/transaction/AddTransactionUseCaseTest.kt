package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AddTransactionUseCaseTest {

    private lateinit var addTransaction: AddTransactionUseCase
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
        transactionRepositoryFake = TransactionRepositoryFake(accountRepositoryFake, categoryRepositoryFake)

        addTransaction = AddTransactionUseCase(
            transactionRepository = transactionRepositoryFake,
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `add Account creation Transaction`(): Unit = runTest {
        accountRepositoryFake.accounts.value = listOf(cashAccount().toDomain())

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
        assertThat(transaction.amount).isEqualTo(Money(10.71))
        assertThat(transaction.transactionDirection).isEqualTo(TransactionDirection.Inflow)
        assertThat(transaction.category).isNull()
    }

    @Test
    fun `add Outflow transaction`(): Unit = runTest {
        accountRepositoryFake.accounts.value = accounts
        categoryRepositoryFake.categories.value = categories
        val category = rentCategoryEntity().toDomain()

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
        assertThat(transaction.amount).isEqualTo(Money(-50.81))
        assertThat(transaction.transactionDirection).isEqualTo(TransactionDirection.Outflow)
        assertThat(transaction.account.id).isEqualTo(1)
        assertThat(transaction.category?.id).isEqualTo(1)
        assertThat(transaction.transactionPartner).isEqualTo("Landlord")
        assertThat(transaction.description).isEqualTo("Rent")

        val accountAfterTransaction = accountRepositoryFake.accounts.value.first()
        assertThat(accountAfterTransaction.name).isEqualTo("Cash Account")
        assertThat(accountAfterTransaction.balance).isEqualTo(Money(449.19))

        val categoryAfterTransaction = categoryRepositoryFake.categories.value.first()
        assertThat(categoryAfterTransaction.name).isEqualTo("Rent")
        assertThat(categoryAfterTransaction.budgetTarget).isEqualTo(Money(1200.0))
        assertThat(categoryAfterTransaction.assignedMoney).isEqualTo(Money(1200.0))
        assertThat(categoryAfterTransaction.availableMoney).isEqualTo(Money(1249.19))
    }
}