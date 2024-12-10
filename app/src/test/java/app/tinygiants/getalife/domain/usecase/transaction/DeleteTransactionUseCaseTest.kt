package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.aldiGroceriesJanuary
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.startingBalanceTransaction
import app.tinygiants.getalife.data.local.datagenerator.transactions
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import assertk.assertThat
import assertk.assertions.containsNone
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class DeleteTransactionUseCaseTest {

    private lateinit var deleteTransaction: DeleteTransactionUseCase
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

        deleteTransaction = DeleteTransactionUseCase(
            transactionRepository = transactionRepositoryFake,
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `Remove Transaction without category involved`(): Unit = runTest {
        transactionRepositoryFake.transactions.value = transactions
        accountRepositoryFake.accounts.value = accounts

        val account = accountRepositoryFake.accounts.value.find { account -> account.id == aldiGroceriesJanuary().accountId }

        assertThat(account).isNotNull()

        val transactionToBeDeleted = aldiGroceriesJanuary().toDomain(account = account!!, category = null)

        deleteTransaction(transaction = transactionToBeDeleted)

        assertThat(transactionRepositoryFake.transactions.value).hasSize(20)
        assertThat(transactionRepositoryFake.transactions.value).containsNone(aldiGroceriesJanuary())

        val accountAfterTransaction = accountRepositoryFake.accounts.value.first()
        assertThat(accountAfterTransaction.name).isEqualTo("Cash Account")
        assertThat(accountAfterTransaction.balance).isEqualTo(Money(550.0))
    }

    @Test
    fun `Remove transaction with category involved`(): Unit = runTest {
        transactionRepositoryFake.transactions.value = transactions
        accountRepositoryFake.accounts.value = accounts
        categoryRepositoryFake.categories.value = categories

        val account = accountRepositoryFake.accounts.value.find { account -> account.id == aldiGroceriesJanuary().accountId }

        assertThat(account).isNotNull()

        val category = categoryRepositoryFake.categories.value.find { category -> category.id == 5L }
        assertThat(category).isNotNull()

        val transactionToBeDeleted = aldiGroceriesJanuary().toDomain(account = account!!, category = category)

        deleteTransaction(transactionToBeDeleted)

        assertThat(transactionRepositoryFake.transactions.value).hasSize(20)
        assertThat(transactionRepositoryFake.transactions.value).containsNone(aldiGroceriesJanuary())

        val accountAfterTransactionDeleted = accountRepositoryFake.accounts.value.first()
        assertThat(accountAfterTransactionDeleted.name).isEqualTo("Cash Account")
        assertThat(accountAfterTransactionDeleted.balance).isEqualTo(Money(550.0))

        val categoryAfterTransactionDeleted = categoryRepositoryFake.categories.value[4]
        assertThat(categoryAfterTransactionDeleted.name).isEqualTo("Groceries")
        assertThat(categoryAfterTransactionDeleted.budgetTarget).isEqualTo(Money(300.0))
        assertThat(categoryAfterTransactionDeleted.assignedMoney).isEqualTo(Money(150.0))
        assertThat(categoryAfterTransactionDeleted.availableMoney).isEqualTo(Money(120.0))
    }

    @Test
    fun `Delete starting budget Transaction of Account`(): Unit = runTest {
        val account = cashAccount().toDomain().copy(balance = Money(1000.0))
        accountRepositoryFake.accounts.value = listOf(account)
        val toBeDeletedTransaction = startingBalanceTransaction().toDomain(account, null)
        transactionRepositoryFake.transactions.value = listOf(toBeDeletedTransaction)

        deleteTransaction(toBeDeletedTransaction)

        val accountAfterDeleteOperation = accountRepositoryFake.accounts.value.first()
        assertThat(accountAfterDeleteOperation.balance).isEqualTo(Money(0.0))
    }
}