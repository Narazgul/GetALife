package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.MainCoroutineExtension
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import app.tinygiants.getalife.domain.usecase.transaction.AddTransactionUseCase
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class AddAccountUseCaseTest {

    private lateinit var addAccount: AddAccountUseCase
    private lateinit var accountRepositoryFake: AccountRepositoryFake
    private lateinit var transactionRepositoryFake: TransactionRepositoryFake
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val mainCoroutineExtension: MainCoroutineExtension = MainCoroutineExtension()
    }

    @BeforeEach
    fun setUp() {
        accountRepositoryFake = AccountRepositoryFake()
        transactionRepositoryFake = TransactionRepositoryFake()
        categoryRepositoryFake = CategoryRepositoryFake()

        val addTransaction = AddTransactionUseCase(
            transactionRepository = transactionRepositoryFake,
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            mainCoroutineExtension.testDispatcher
        )

        addAccount = AddAccountUseCase(
            repository = accountRepositoryFake,
            addTransaction = addTransaction,
            defaultDispatcher = mainCoroutineExtension.testDispatcher
        )
    }

    @Test
    fun `Add new account with positive balance and AccountType and has positive starting balance`(): Unit = runTest {
        val testBegin = Clock.System.now()
        val oneSecondAfterTestBegin = testBegin + 50.milliseconds
        addAccount(name = "Bargeld", balance = Money(value = 1.00), type = AccountType.Cash, startingBalanceName = "Starting balance")

        advanceUntilIdle()

        val insertedAccount = accountRepositoryFake.accountsFlow.value.first()
        assertThat(accountRepositoryFake.accountsFlow.value).hasSize(1)
        assertThat(insertedAccount.name).isEqualTo("Bargeld")
        assertThat(insertedAccount.balance).isEqualTo(1.00)
        assertThat(insertedAccount.type).isEqualTo(AccountType.Cash)
        assertThat(insertedAccount.listPosition).isEqualTo(0)
        assertThat(insertedAccount.updatedAt).isBetween(start = testBegin, end = oneSecondAfterTestBegin)
        assertThat(insertedAccount.createdAt).isBetween(start = testBegin, end = oneSecondAfterTestBegin)

        val startingBalanceTransaction = transactionRepositoryFake.transactions.value.first()
        assertThat(startingBalanceTransaction.description).isEqualTo("Starting balance")
        assertThat(startingBalanceTransaction.amount).isEqualTo(1.00)
    }

    @Test
    fun `Add new account with negative balance and AccountType and has negative starting balance`(): Unit = runTest {
        val testBegin = Clock.System.now()
        val shortlyAfterTestBegin = testBegin + 50.milliseconds
        addAccount(name = "Bargeld", balance = Money(value = -1.00), type = AccountType.Cash, startingBalanceName = "Starting balance")

        advanceUntilIdle()

        val insertedAccount = accountRepositoryFake.accountsFlow.value.first()
        assertThat(accountRepositoryFake.accountsFlow.value).hasSize(1)
        assertThat(insertedAccount.name).isEqualTo("Bargeld")
        assertThat(insertedAccount.balance).isEqualTo(-1.00)
        assertThat(insertedAccount.type).isEqualTo(AccountType.Cash)
        assertThat(insertedAccount.listPosition).isEqualTo(0)
        assertThat(insertedAccount.updatedAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)
        assertThat(insertedAccount.createdAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)

        val startingBalanceTransaction = transactionRepositoryFake.transactions.value.first()
        assertThat(startingBalanceTransaction.description).isEqualTo("Starting balance")
        assertThat(startingBalanceTransaction.amount).isEqualTo(-1.00)
    }

    @Test
    fun `Test zero available starting balance`(): Unit = runTest {
        val testBegin = Clock.System.now()
        val shortlyAfterTestBegin = testBegin + 50.milliseconds
        addAccount(name = "Bargeld", balance = Money(0.00), type = AccountType.Cash, startingBalanceName = "Starting balance")

        advanceUntilIdle()

        val insertedAccount = accountRepositoryFake.accountsFlow.value.first()
        assertThat(accountRepositoryFake.accountsFlow.value).hasSize(1)
        assertThat(insertedAccount.name).isEqualTo("Bargeld")
        assertThat(insertedAccount.balance).isEqualTo(0.00)
        assertThat(insertedAccount.type).isEqualTo(AccountType.Cash)
        assertThat(insertedAccount.listPosition).isEqualTo(0)
        assertThat(insertedAccount.updatedAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)
        assertThat(insertedAccount.createdAt).isBetween(start = testBegin, end = shortlyAfterTestBegin)

        val startingBalanceTransaction = transactionRepositoryFake.transactions.value.first()
        val calculatedStartingBalance = insertedAccount.balance + startingBalanceTransaction.amount
        assertThat(startingBalanceTransaction.description).isEqualTo("Starting balance")
        assertThat(calculatedStartingBalance).isEqualTo(0.00)
    }
}