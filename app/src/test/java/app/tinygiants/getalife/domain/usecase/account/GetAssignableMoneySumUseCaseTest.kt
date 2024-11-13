package app.tinygiants.getalife.domain.usecase.account

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
import app.tinygiants.getalife.data.local.datagenerator.transportCategoryEntity
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class GetAssignableMoneySumUseCaseTest {

    private lateinit var getAssignableMoney: GetAssignableMoneySumUseCase
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

        getAssignableMoney = GetAssignableMoneySumUseCase(
            accountsRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `Get assignable Money if no accounts exist should be 0`(): Unit = runTest {
        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow()
            assertThat(assignableMoney).isNotNull()
            assertThat(assignableMoney).isEqualTo(Money(value = 0.0))
        }
    }

    @Test
    fun `Get assignable Money if only one account exists should be equal to account balance`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value = listOf(cashAccount())

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow()
            assertThat(assignableMoney).isEqualTo(Money(value = 500.0))
        }
    }

    @Test
    fun `Get assignable Money for several account types which are supposed to be included`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value =
            listOf(cashAccount(), checkingAccount(), secondCheckingAccount(), savingsAccount(), creditCardAccount())

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow()
            assertThat(assignableMoney).isEqualTo(Money(value = 4484.92))
        }
    }

    @Test
    fun `Get assignable Money for several account types which are supposed to be excluded`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value =
            listOf(mortgageAccount(), loanAccount(), depotAccount())

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow()
            assertThat(assignableMoney).isEqualTo(Money(value = 0.0))
        }
    }

    @Test
    fun `Get assignable Money for several accounts with and without excluded account types`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value = accounts

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow()
            assertThat(assignableMoney).isEqualTo(Money(value = 4484.92))
        }
    }

    @Test
    fun `Get assignable Money if no accounts exist but single category has money assigned`(): Unit = runTest {
        categoryRepositoryFake.categories.value = listOf(rentCategoryEntity())

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow()
            assertThat(assignableMoney).isNotNull()
            assertThat(assignableMoney).isEqualTo(Money(value = -1200.0))
        }
    }

    @Test
    fun `Get assignable money if no accounts exist and several categories with assigned money`(): Unit = runTest {
        categoryRepositoryFake.categories.value = categories

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow()
            assertThat(assignableMoney).isEqualTo(Money(value = -2800.0))
        }
    }

    @Test
    fun `Get assignable money if one account exists and one category with no money assigned to`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value = listOf(cashAccount())
        categoryRepositoryFake.categories.value = listOf(transportCategoryEntity())

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow()
            assertThat(assignableMoney).isEqualTo(Money(value = 500.0))
        }
    }

    @Test
    fun `Get assignable money if one account exists and one category with money assigned to`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value = listOf(cashAccount())
        categoryRepositoryFake.categories.value = listOf(groceriesCategoryEntity())

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow()
            assertThat(assignableMoney).isEqualTo(Money(value = 350.0))
        }
    }

    @Test
    fun `Get assignable money if one account exists and several categories with money assigned to`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value = listOf(cashAccount())
        categoryRepositoryFake.categories.value = categories

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow()
            assertThat(assignableMoney).isEqualTo(Money(value = -2300.0))
        }
    }

    @Test
    fun `Get assignable money if several accounts exist and one category without money assigned to`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value = accounts
        categoryRepositoryFake.categories.value = listOf(transportCategoryEntity())

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow()
            assertThat(assignableMoney).isEqualTo(Money(value = 4484.92))
        }
    }

    @Test
    fun `Get assignable money if several accounts exist and several categories with money assigned to`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value = accounts
        categoryRepositoryFake.categories.value = categories

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow()
            assertThat(assignableMoney).isEqualTo(Money(value = 1684.92))
        }
    }
}