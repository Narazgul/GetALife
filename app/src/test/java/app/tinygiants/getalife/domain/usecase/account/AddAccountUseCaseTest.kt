package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.GroupRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import app.tinygiants.getalife.domain.usecase.budget.RecalculateCategoryMonthlyStatusUseCase
import app.tinygiants.getalife.domain.usecase.transaction.AddTransactionUseCase
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class AddAccountUseCaseTest {

    private lateinit var addAccount: AddAccountUseCase
    private lateinit var addTransaction: AddTransactionUseCase
    private lateinit var accountRepositoryFake: AccountRepositoryFake
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake
    private lateinit var transactionRepositoryFake: TransactionRepositoryFake
    private lateinit var groupRepositoryFake: GroupRepositoryFake
    private lateinit var categoryMonthlyStatusRepositoryFake: CategoryMonthlyStatusRepositoryFake
    private lateinit var recalculateCategoryMonthlyStatusUseCase: RecalculateCategoryMonthlyStatusUseCase

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        accountRepositoryFake = AccountRepositoryFake()
        categoryRepositoryFake = CategoryRepositoryFake()
        groupRepositoryFake = GroupRepositoryFake()
        categoryMonthlyStatusRepositoryFake = CategoryMonthlyStatusRepositoryFake()
        transactionRepositoryFake = TransactionRepositoryFake(
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake
        )
        recalculateCategoryMonthlyStatusUseCase = RecalculateCategoryMonthlyStatusUseCase(
            statusRepository = categoryMonthlyStatusRepositoryFake,
            transactionRepository = transactionRepositoryFake,
            categoryRepository = categoryRepositoryFake
        )

        addTransaction = AddTransactionUseCase(
            transactionRepository = transactionRepositoryFake,
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            groupRepository = groupRepositoryFake,
            recalculateCategoryMonthlyStatus = recalculateCategoryMonthlyStatusUseCase,
            defaultDispatcher = testDispatcherExtension.testDispatcher,
            categoryMonthlyStatusRepository = categoryMonthlyStatusRepositoryFake
        )

        addAccount = AddAccountUseCase(
            accountRepository = accountRepositoryFake,
            addTransaction = addTransaction,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `Add new account with positive balance and AccountType and has positive starting balance`(): Unit = runTest {
        val testBegin = Clock.System.now()
        val shortlyAfterTestBegin = testBegin + 50.milliseconds
        addAccount(
            name = "Bargeld",
            balance = Money(value = 1.00),
            type = AccountType.Cash,
            startingBalanceName = "Starting balance",
            startingBalanceDescription = "Starting balance for this account"
        )

        advanceUntilIdle()

        val insertedAccount = accountRepositoryFake.accounts.value.first()
        assertThat(accountRepositoryFake.accounts.value).hasSize(1)
        assertThat(insertedAccount.name).isEqualTo("Bargeld")
        assertThat(insertedAccount.balance).isEqualTo(Money(1.00))
        assertThat(insertedAccount.type).isEqualTo(AccountType.Cash)
        assertThat(insertedAccount.listPosition).isEqualTo(0)
        assertThat(insertedAccount.updatedAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)
        assertThat(insertedAccount.createdAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)

        val startingBalanceTransaction = transactionRepositoryFake.transactions.value.first()
        assertThat(startingBalanceTransaction.transactionPartner).isEqualTo("Starting balance")
        assertThat(startingBalanceTransaction.amount).isEqualTo(Money(1.00))
    }

    @Test
    fun `Add new account with negative balance and AccountType and has negative starting balance`(): Unit = runTest {
        val testBegin = Clock.System.now()
        val shortlyAfterTestBegin = testBegin + 50.milliseconds
        addAccount(
            name = "Bargeld",
            balance = Money(value = -1.00),
            type = AccountType.Cash,
            startingBalanceName = "Starting balance",
            startingBalanceDescription = "Starting balance for this account"
        )

        advanceUntilIdle()

        val insertedAccount = accountRepositoryFake.accounts.value.first()
        assertThat(accountRepositoryFake.accounts.value).hasSize(1)
        assertThat(insertedAccount.name).isEqualTo("Bargeld")
        assertThat(insertedAccount.balance).isEqualTo(Money(-1.00))
        assertThat(insertedAccount.type).isEqualTo(AccountType.Cash)
        assertThat(insertedAccount.listPosition).isEqualTo(0)
        assertThat(insertedAccount.updatedAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)
        assertThat(insertedAccount.createdAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)

        val startingBalanceTransaction = transactionRepositoryFake.transactions.value.first()
        assertThat(startingBalanceTransaction.transactionPartner).isEqualTo("Starting balance")
        assertThat(startingBalanceTransaction.amount).isEqualTo(Money(-1.00))
    }

    @Test
    fun `Test zero available starting balance`(): Unit = runTest {
        val testBegin = Clock.System.now()
        val shortlyAfterTestBegin = testBegin + 50.milliseconds
        addAccount(
            name = "Bargeld",
            balance = Money(0.00),
            type = AccountType.Cash,
            startingBalanceName = "Starting balance",
            startingBalanceDescription = "Starting balance for this account"
        )

        advanceUntilIdle()

        val insertedAccount = accountRepositoryFake.accounts.value.first()
        assertThat(accountRepositoryFake.accounts.value).hasSize(1)
        assertThat(insertedAccount.name).isEqualTo("Bargeld")
        assertThat(insertedAccount.balance).isEqualTo(Money(0.00))
        assertThat(insertedAccount.type).isEqualTo(AccountType.Cash)
        assertThat(insertedAccount.listPosition).isEqualTo(0)
        assertThat(insertedAccount.updatedAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)
        assertThat(insertedAccount.createdAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)

        val startingBalanceTransaction = transactionRepositoryFake.transactions.value.first()
        val calculatedStartingBalance = insertedAccount.balance + startingBalanceTransaction.amount
        assertThat(startingBalanceTransaction.transactionPartner).isEqualTo("Starting balance")
        assertThat(calculatedStartingBalance).isEqualTo(Money(0.00))
    }
}