package app.tinygiants.getalife.domain.usecase.budget

import app.cash.turbine.test
import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.BANK_ACCOUNT_ONE
import app.tinygiants.getalife.data.local.datagenerator.BANK_ACCOUNT_TWO
import app.tinygiants.getalife.data.local.datagenerator.CASH_ACCOUNT
import app.tinygiants.getalife.data.local.datagenerator.CREDIT_CARD_ACCOUNT
import app.tinygiants.getalife.data.local.datagenerator.DEPOT_ACCOUNT
import app.tinygiants.getalife.data.local.datagenerator.LOAN_ACCOUNT
import app.tinygiants.getalife.data.local.datagenerator.MORTGAGE_ACCOUNT
import app.tinygiants.getalife.data.local.datagenerator.SAVINGS_ACCOUNT
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.aldiGroceriesJanuary
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
import app.tinygiants.getalife.data.local.datagenerator.startingBalanceTransaction
import app.tinygiants.getalife.data.local.datagenerator.techCorpSalaryJanuary
import app.tinygiants.getalife.data.local.datagenerator.transportCategoryEntity
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class GetAssignableMoneyUseCaseTest {

    private lateinit var getAssignableMoney: GetAssignableMoneyUseCase
    private lateinit var transactionRepositoryFake: TransactionRepositoryFake
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake
    private lateinit var accountRepositoryFake: AccountRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        categoryRepositoryFake = CategoryRepositoryFake()
        accountRepositoryFake = AccountRepositoryFake()
        transactionRepositoryFake = TransactionRepositoryFake(accountRepositoryFake, categoryRepositoryFake)

        getAssignableMoney = GetAssignableMoneyUseCase(
            transactionRepository = transactionRepositoryFake,
            categoriesRepository = categoryRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `Get assignable Money if no accounts exist should be 0`(): Unit = runTest {
        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isNotNull()
            assertThat(assignableMoney).isEqualTo(EmptyMoney())
        }
    }

    @Test
    fun `Get assignable Money if only one account exists should be equal to account balance`(): Unit = runTest {
        accountRepositoryFake.accounts.value = listOf(checkingAccount().toDomain())
        transactionRepositoryFake.transactions.value = listOf(techCorpSalaryJanuary().toDomain(checkingAccount().toDomain(), null))

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = 2000.0))
        }
    }

    @Test
    fun `Get assignable Money for several account types which are supposed to be included`(): Unit = runTest {
        accountRepositoryFake.accounts.value =
            listOf(
                cashAccount().toDomain(),
                checkingAccount().toDomain(),
                secondCheckingAccount().toDomain(),
                savingsAccount().toDomain(),
                creditCardAccount().toDomain())
        transactionRepositoryFake.transactions.value = listOf(
            techCorpSalaryJanuary().copy(accountId = CASH_ACCOUNT, amount = 100.0).toDomain(cashAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = BANK_ACCOUNT_ONE, amount = 32.0).toDomain(checkingAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = BANK_ACCOUNT_TWO, amount = 2034.82).toDomain(secondCheckingAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = SAVINGS_ACCOUNT, amount = 10000.71).toDomain(savingsAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = CREDIT_CARD_ACCOUNT, amount = -471.00).toDomain(creditCardAccount().toDomain(), null),
        )

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = 11696.53))
        }
    }

    @Test
    fun `Get assignable Money for several account types which are supposed to be excluded`(): Unit = runTest {
        accountRepositoryFake.accounts.value =
            listOf(mortgageAccount().toDomain(), loanAccount().toDomain(), depotAccount().toDomain())
        transactionRepositoryFake.transactions.value = listOf(
            techCorpSalaryJanuary().copy(accountId = MORTGAGE_ACCOUNT, amount = 100.0).toDomain(mortgageAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = LOAN_ACCOUNT, amount = 32.0).toDomain(loanAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = DEPOT_ACCOUNT, amount = 2034.82).toDomain(depotAccount().toDomain(), null)
        )

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(EmptyMoney())
        }
    }

    @Test
    fun `Get assignable Money for several accounts WITH and WITHOUT excluded account types`(): Unit = runTest {
        accountRepositoryFake.accounts.value = accounts
        transactionRepositoryFake.transactions.value = listOf(
            techCorpSalaryJanuary().copy(accountId = CASH_ACCOUNT, amount = 100.0).toDomain(cashAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = BANK_ACCOUNT_ONE, amount = 32.0).toDomain(checkingAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = BANK_ACCOUNT_TWO, amount = 2034.82).toDomain(secondCheckingAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = SAVINGS_ACCOUNT, amount = 10000.71).toDomain(savingsAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = CREDIT_CARD_ACCOUNT, amount = -471.00).toDomain(creditCardAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = MORTGAGE_ACCOUNT, amount = 100.0).toDomain(mortgageAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = LOAN_ACCOUNT, amount = 32.0).toDomain(loanAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = DEPOT_ACCOUNT, amount = 2034.82).toDomain(depotAccount().toDomain(), null)
        )

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = 11696.53))
        }
    }

    @Test
    fun `Get assignable Money if no accounts exist but single category has money assigned`(): Unit = runTest {
        categoryRepositoryFake.categories.value = listOf(rentCategoryEntity().toDomain())

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isNotNull()
            assertThat(assignableMoney).isEqualTo(Money(value = -1200.0))
        }
    }

    @Test
    fun `Get assignable money if no accounts exist and several categories WITH assigned money`(): Unit = runTest {
        categoryRepositoryFake.categories.value = categories

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = -2800.0))
        }
    }

    @Test
    fun `Get assignable money if one account exists and one category WITHOUT money assigned to`(): Unit = runTest {
        val account = cashAccount().toDomain()
        val category = transportCategoryEntity().toDomain()
        accountRepositoryFake.accounts.value = listOf(account)
        categoryRepositoryFake.categories.value = listOf(category)
        transactionRepositoryFake.transactions.value = listOf(startingBalanceTransaction().toDomain(account, category))

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = 1000.0))
        }
    }

    @Test
    fun `Get assignable money if one account exists and one category WITH money assigned to`(): Unit = runTest {
        val account = cashAccount().toDomain()
        val category = groceriesCategoryEntity().toDomain()
        accountRepositoryFake.accounts.value = listOf(account)
        categoryRepositoryFake.categories.value = listOf(category)
        transactionRepositoryFake.transactions.value = listOf(startingBalanceTransaction().toDomain(account = account, category = category))

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = 850.0))
        }
    }

    @Test
    fun `Get assignable money if one account exists and several categories WITH money assigned to`(): Unit = runTest {
        accountRepositoryFake.accounts.value = listOf(checkingAccount().toDomain())
        categoryRepositoryFake.categories.value = categories
        transactionRepositoryFake.transactions.value = listOf(techCorpSalaryJanuary()
            .toDomain(checkingAccount().toDomain(), null).copy(amount = Money(1200.50)))

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = -1599.50))
        }
    }

    @Test
    fun `Get assignable money if several accounts exist and one category WITHOUT money assigned to`(): Unit = runTest {
        accountRepositoryFake.accounts.value = accounts
        transactionRepositoryFake.transactions.value = listOf(
            techCorpSalaryJanuary().copy(accountId = CASH_ACCOUNT, amount = 100.0).toDomain(cashAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = BANK_ACCOUNT_ONE, amount = 32.0).toDomain(checkingAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = BANK_ACCOUNT_TWO, amount = 2034.82).toDomain(secondCheckingAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = SAVINGS_ACCOUNT, amount = 10000.71).toDomain(savingsAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = CREDIT_CARD_ACCOUNT, amount = -471.00).toDomain(creditCardAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = MORTGAGE_ACCOUNT, amount = 100.0).toDomain(mortgageAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = LOAN_ACCOUNT, amount = 32.0).toDomain(loanAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = DEPOT_ACCOUNT, amount = 2034.82).toDomain(depotAccount().toDomain(), null)
        )

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = 11696.53))
        }
    }

    @Test
    fun `Get assignable money if several accounts exist and several categories WITH money assigned to`(): Unit = runTest {
        accountRepositoryFake.accounts.value = accounts
        categoryRepositoryFake.categories.value = categories
        transactionRepositoryFake.transactions.value = listOf(
            techCorpSalaryJanuary().copy(accountId = CASH_ACCOUNT, amount = 100.0).toDomain(cashAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = BANK_ACCOUNT_ONE, amount = 32.0).toDomain(checkingAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = BANK_ACCOUNT_TWO, amount = 2034.82).toDomain(secondCheckingAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = SAVINGS_ACCOUNT, amount = 10000.71).toDomain(savingsAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = CREDIT_CARD_ACCOUNT, amount = -471.00).toDomain(creditCardAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = MORTGAGE_ACCOUNT, amount = 100.0).toDomain(mortgageAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = LOAN_ACCOUNT, amount = 32.0).toDomain(loanAccount().toDomain(), null),
            techCorpSalaryJanuary().copy(accountId = DEPOT_ACCOUNT, amount = 2034.82).toDomain(depotAccount().toDomain(), null)
        )

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = 8896.53))
        }
    }

    @Test
    fun `Raise Inflow Transaction Amount`(): Unit = runTest {
        val account = checkingAccount().toDomain()
        accountRepositoryFake.accounts.value = listOf(account)
        val updatedTransaction = techCorpSalaryJanuary().toDomain(account = account, null).copy(amount = Money(2500.0))
        transactionRepositoryFake.transactions.value = listOf(updatedTransaction)

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = 2500.0))
        }
    }

    @Test
    fun `Lower Inflow transaction amount`(): Unit = runTest {
        val account = checkingAccount().toDomain()
        accountRepositoryFake.accounts.value = listOf(account)
        val updatedTransaction = techCorpSalaryJanuary().toDomain(account = account, category = null).copy(amount = Money(1500.0))
        transactionRepositoryFake.transactions.value = listOf(updatedTransaction)

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(value = 1500.0))
        }
    }

    @Test
    fun `Available Money with overspent Category`(): Unit = runTest {
        val account = cashAccount().toDomain()
        accountRepositoryFake.accounts.value = listOf(account)
        val category = groceriesCategoryEntity().toDomain()
        categoryRepositoryFake.categories.value = listOf(category)
        val startingBalanceTransaction = startingBalanceTransaction().copy(amount = 100.0).toDomain(
            account = account,
            category = category
        )
        val shopping = aldiGroceriesJanuary().toDomain(
            account = account,
            category = category
        ).copy(amount = Money(-5.0))
        transactionRepositoryFake.transactions.value = listOf(startingBalanceTransaction, shopping)

        getAssignableMoney().test {
            val assignableMoney = awaitItem().getOrThrow().first
            assertThat(assignableMoney).isEqualTo(Money(95.0))
        }
    }
}