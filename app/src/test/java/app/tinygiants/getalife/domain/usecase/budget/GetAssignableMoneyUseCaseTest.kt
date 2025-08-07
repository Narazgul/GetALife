package app.tinygiants.getalife.domain.usecase.budget

import app.cash.turbine.test
import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.checkingAccount
import app.tinygiants.getalife.data.local.datagenerator.creditCardAccount
import app.tinygiants.getalife.data.local.datagenerator.depotAccount
import app.tinygiants.getalife.data.local.datagenerator.groceriesCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.loanAccount
import app.tinygiants.getalife.data.local.datagenerator.mortgageAccount
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.savingsAccount
import app.tinygiants.getalife.data.local.datagenerator.secondCheckingAccount
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.includeInBudget
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepositoryFake
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.TimeZone
import kotlinx.datetime.toLocalDateTime

class GetAssignableMoneyUseCaseTest {

    private lateinit var getAssignableMoney: GetAssignableMoneyUseCase
    private lateinit var accountRepositoryFake: AccountRepositoryFake
    private lateinit var statusRepositoryFake: CategoryMonthlyStatusRepositoryFake

    private val testClock = object : Clock {
        override fun now(): Instant = Instant.parse("2024-07-15T10:00:00Z")
    }
    private val currentYearMonth = testClock.now().toLocalDateTime(TimeZone.UTC).run {
        "$year-${monthNumber.toString().padStart(2, '0')}"
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        accountRepositoryFake = AccountRepositoryFake()
        statusRepositoryFake = CategoryMonthlyStatusRepositoryFake()

        getAssignableMoney = GetAssignableMoneyUseCase(
            accountRepository = accountRepositoryFake,
            statusRepository = statusRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher,
            clock = testClock
        )
    }

    @Test
    fun `Get assignable Money if no accounts exist should be 0`(): Unit = runTest {
        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(EmptyMoney())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Get assignable Money if only one account exists should be equal to account balance`(): Unit = runTest {
        val account = checkingAccount().toDomain().copy(balance = Money(2000.0))
        accountRepositoryFake.accounts.value = listOf(account)

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = 2000.0))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Get assignable Money for several account types which are supposed to be included`(): Unit = runTest {
        accountRepositoryFake.accounts.value =
            listOf(
                cashAccount().toDomain().copy(balance = Money(100.0)),
                checkingAccount().toDomain().copy(balance = Money(32.0)),
                secondCheckingAccount().toDomain().copy(balance = Money(2034.82)),
                savingsAccount().toDomain().copy(balance = Money(10000.71)),
                creditCardAccount().toDomain().copy(balance = Money(-471.00)),
            )

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = 11696.53))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Get assignable Money for several account types which are supposed to be excluded`(): Unit = runTest {
        accountRepositoryFake.accounts.value =
            listOf(
                mortgageAccount().toDomain(), // not included in budget
                loanAccount().toDomain(),     // not included
                depotAccount().toDomain()       // not included
            )

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(EmptyMoney())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Get assignable Money for several accounts WITH and WITHOUT excluded account types`(): Unit = runTest {
        accountRepositoryFake.accounts.value = accounts.map {
            it.toDomain().copy(balance = Money(it.id.toDouble() * 100))
        }

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            val expected = accounts.filter { it.toDomain().type.includeInBudget }.sumOf { it.id * 100.0 }
            assertThat(assignableMoney).isEqualTo(Money(expected))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Get assignable Money if no accounts exist but single category has money assigned`(): Unit = runTest {
        val status = CategoryMonthlyStatus(
            category = rentCategoryEntity().toDomain(),
            assignedAmount = Money(1200.0),
            yearMonth = currentYearMonth
        )
        statusRepositoryFake.statusData.value = listOf(status)

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = -1200.0))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Get assignable money if no accounts exist and several categories WITH assigned money`(): Unit = runTest {
        val statuses = categories.map {
            CategoryMonthlyStatus(
                category = it.toDomain(),
                assignedAmount = Money(it.id.toDouble() * 10), // arbitrary assigned amount
                yearMonth = currentYearMonth
            )
        }
        statusRepositoryFake.statusData.value = statuses
        val totalAssigned = statuses.sumOf { it.assignedAmount.asDouble() }

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(-totalAssigned))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Get assignable money if one account exists and one category WITHOUT money assigned to`(): Unit = runTest {
        val account = cashAccount().toDomain().copy(balance = Money(1000.0))
        accountRepositoryFake.accounts.value = listOf(account)

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = 1000.0))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Get assignable money if one account exists and one category WITH money assigned to`(): Unit = runTest {
        val account = cashAccount().toDomain().copy(balance = Money(1000.0))
        accountRepositoryFake.accounts.value = listOf(account)

        val status = CategoryMonthlyStatus(
            category = groceriesCategoryEntity().toDomain(),
            assignedAmount = Money(150.0),
            yearMonth = currentYearMonth
        )
        statusRepositoryFake.statusData.value = listOf(status)

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = 850.0))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Get assignable money if one account exists and several categories WITH money assigned to`(): Unit = runTest {
        val account = checkingAccount().toDomain().copy(balance = Money(1200.50))
        accountRepositoryFake.accounts.value = listOf(account)

        val statuses = listOf(
            CategoryMonthlyStatus(category = rentCategoryEntity().toDomain(), assignedAmount = Money(1200.0), yearMonth = currentYearMonth),
            CategoryMonthlyStatus(category = groceriesCategoryEntity().toDomain(), assignedAmount = Money(1600.0), yearMonth = currentYearMonth)
        )
        statusRepositoryFake.statusData.value = statuses
        val totalAssigned = 1200.0 + 1600.0 // = 2800.0

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            val expected = 1200.50 - totalAssigned // = -1599.50
            assertThat(assignableMoney).isEqualTo(Money(expected))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Get assignable money if several accounts exist and one category WITHOUT money assigned to`(): Unit = runTest {
        accountRepositoryFake.accounts.value = accounts.map {
            it.toDomain().copy(balance = Money(it.id * 100.0))
        }

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            val expected = accounts.filter { it.toDomain().type.includeInBudget }.sumOf { it.id * 100.0 }
            assertThat(assignableMoney).isEqualTo(Money(expected))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Get assignable money if several accounts exist and several categories WITH money assigned to`(): Unit = runTest {
        accountRepositoryFake.accounts.value = accounts.map {
            it.toDomain().copy(balance = Money(it.id * 100.0))
        }
        val totalBalance = accounts.filter { it.toDomain().type.includeInBudget }.sumOf { it.id * 100.0 }
        val statuses = listOf(
            CategoryMonthlyStatus(category = rentCategoryEntity().toDomain(), assignedAmount = Money(1200.0), yearMonth = currentYearMonth),
            CategoryMonthlyStatus(category = groceriesCategoryEntity().toDomain(), assignedAmount = Money(1600.0), yearMonth = currentYearMonth)
        )
        statusRepositoryFake.statusData.value = statuses
        val totalAssigned = statuses.sumOf { it.assignedAmount.asDouble() }

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            val expected = totalBalance - totalAssigned
            assertThat(assignableMoney).isEqualTo(Money(expected))
            cancelAndIgnoreRemainingEvents()
        }
    }
}