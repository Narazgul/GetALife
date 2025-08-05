package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.aldiGroceriesJanuary
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.checkingAccount
import app.tinygiants.getalife.data.local.datagenerator.groceriesCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.techCorpSalaryJanuary
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ExchangeTransactionDirectionUseCaseTest {

    private lateinit var exchangeTransactionDirection: ExchangeTransactionDirectionUseCase
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

        exchangeTransactionDirection = ExchangeTransactionDirectionUseCase(
            categoryRepository = categoryRepositoryFake,
            accountRepository = accountRepositoryFake,
            transactionRepository = transactionRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `Change from Outflow to Inflow`(): Unit = runTest {
        categoryRepositoryFake.categories.value = listOf(groceriesCategoryEntity().toDomain())
        accountRepositoryFake.accounts.value = listOf(cashAccount().toDomain())
        transactionRepositoryFake.transactions.value = listOf(
            aldiGroceriesJanuary().toDomain(
                account = cashAccount().toDomain(),
                category = groceriesCategoryEntity().toDomain()
            )
        )

        val cashAccount = cashAccount().toDomain()
        val groceriesCategory = groceriesCategoryEntity().toDomain()
        val changedTransaction = aldiGroceriesJanuary().copy(transactionDirection = TransactionDirection.Inflow)

        exchangeTransactionDirection(transaction = changedTransaction.toDomain(cashAccount, groceriesCategory))

        // ensure category list accessed to keep test side-effect but no assertion needed

        val updatedAccount = accountRepositoryFake.accounts.value.first()
        assertThat(updatedAccount.balance).isEqualTo(Money(550.0))

        val updatedTransaction = transactionRepositoryFake.transactions.value.first()
        assertThat(updatedTransaction.transactionDirection).isEqualTo(TransactionDirection.Inflow)
        assertThat(updatedTransaction.amount).isEqualTo(Money(50.0))
        assertThat(updatedTransaction.account.id).isEqualTo(cashAccount().id)
        assertThat(updatedTransaction.category?.id).isEqualTo(groceriesCategory.id)
    }

    @Test
    fun `Change from Inflow to Outflow`(): Unit = runTest {
        categoryRepositoryFake.categories.value = listOf(groceriesCategoryEntity().toDomain())
        accountRepositoryFake.accounts.value = listOf(checkingAccount().toDomain())
        transactionRepositoryFake.transactions.value = listOf(
            techCorpSalaryJanuary().toDomain(
                account = checkingAccount().toDomain(),
                category = groceriesCategoryEntity().toDomain()
            )
        )

        val checkingAccount = checkingAccount().toDomain()
        val groceriesCategory = groceriesCategoryEntity().toDomain()
        val changedTransaction = techCorpSalaryJanuary().copy(transactionDirection = TransactionDirection.Outflow)
            .toDomain(account = checkingAccount, category = groceriesCategory)

        exchangeTransactionDirection(transaction = changedTransaction)

        // ensure category accessed

        val updatedAccount = accountRepositoryFake.accounts.value.first()
        assertThat(updatedAccount.balance).isEqualTo(Money(-799.5))

        val updatedTransaction = transactionRepositoryFake.transactions.value.first()
        assertThat(updatedTransaction.transactionDirection).isEqualTo(TransactionDirection.Outflow)
        assertThat(updatedTransaction.amount).isEqualTo(Money(-2000.0))
        assertThat(updatedTransaction.account.id).isEqualTo(checkingAccount().id)
        assertThat(updatedTransaction.category?.id).isEqualTo(groceriesCategoryEntity().id)
    }

    @Test
    fun `Change from Inflow to Outflow with MISSING CATEGORY`(): Unit = runTest {
        categoryRepositoryFake.categories.value = listOf(groceriesCategoryEntity().toDomain())
        accountRepositoryFake.accounts.value = listOf(checkingAccount().toDomain())
        transactionRepositoryFake.transactions.value = listOf(
            techCorpSalaryJanuary().toDomain(
                account = checkingAccount().toDomain(),
                category = groceriesCategoryEntity().toDomain()
            )
        )

        val changedTransaction = techCorpSalaryJanuary()
            .copy(transactionDirection = TransactionDirection.Outflow).toDomain(
                account = checkingAccount().toDomain(),
                category = null
            )

        exchangeTransactionDirection(transaction = changedTransaction)

        // ensure category accessed

        val updatedAccount = accountRepositoryFake.accounts.value.first()
        assertThat(updatedAccount.balance).isEqualTo(Money(1200.5))

        val updatedTransaction = transactionRepositoryFake.transactions.value.first()
        assertThat(updatedTransaction.transactionDirection).isEqualTo(TransactionDirection.Inflow)
        assertThat(updatedTransaction.amount).isEqualTo(Money(2000.0))
        assertThat(updatedTransaction.account.id).isEqualTo(checkingAccount().id)
        assertThat(updatedTransaction.category).isEqualTo(groceriesCategoryEntity().toDomain())
    }
}