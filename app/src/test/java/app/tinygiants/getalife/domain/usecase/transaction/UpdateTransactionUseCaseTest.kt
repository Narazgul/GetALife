package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.aldiGroceriesJanuary
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.groceriesCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.transactions
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
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

        updateTransaction = UpdateTransactionUseCase(
            transactionRepository = transactionRepositoryFake,
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )

        transactionRepositoryFake.transactions.value = transactions
        accountRepositoryFake.accounts.value = accounts
        categoryRepositoryFake.categories.value = categories
    }

    @Test
    fun `Test changing nothing on the update`(): Unit = runTest {
        val account = cashAccount().toDomain()
        val category = groceriesCategoryEntity().toDomain()
        val updatedTransaction = aldiGroceriesJanuary().toDomain(account = account, category = category)

        updateTransaction(updatedTransaction)

        assertThat(transactionRepositoryFake.transactions.value).contains(aldiGroceriesJanuary().toDomain(account, category))
        assertThat(transactionRepositoryFake.transactions.value[updatedTransaction.id.toInt() - 1].amount).isEqualTo(
            aldiGroceriesJanuary().toDomain(account, category).amount
        )
        assertThat(accountRepositoryFake.accounts.value[account.id.toInt() - 1].balance).isEqualTo(cashAccount().toDomain().balance)
        assertThat(categoryRepositoryFake.categories.value[category.id.toInt() - 1].availableMoney).isEqualTo(
            groceriesCategoryEntity().toDomain().availableMoney
        )
    }

    @Test
    fun `Spent less on Outflow Transaction Amount`(): Unit = runTest {
        val account = cashAccount().toDomain()
        val category = rentCategoryEntity().toDomain()
        val updatedTransaction =
            aldiGroceriesJanuary().toDomain(account = account, category = category).copy(amount = Money(-40.0))

        updateTransaction(updatedTransaction)

        val accountBalance = accountRepositoryFake.accounts.value.find { it.id == cashAccount().id }!!.balance
        val categoryAvailableMoney =
            categoryRepositoryFake.categories.value.find { it.id == rentCategoryEntity().id }!!.availableMoney
        val transactionAmount = transactionRepositoryFake.transactions.value.find { it.id == aldiGroceriesJanuary().id }!!.amount

        assertThat(accountBalance).isEqualTo(Money(510.0))
        assertThat(categoryAvailableMoney).isEqualTo(Money(1310.0))
        assertThat(transactionAmount).isEqualTo(Money(-40.0))
    }

    @Test
    fun `Spent more on Outflow Transaction Amount`(): Unit = runTest {
        val account = cashAccount().toDomain()
        val category = rentCategoryEntity().toDomain()
        val updatedTransaction = aldiGroceriesJanuary().toDomain(account = account, category = category).copy(amount = Money(-60.0))

        updateTransaction(updatedTransaction)

        val accountBalance = accountRepositoryFake.accounts.value.find { it.id == cashAccount().id }!!.balance
        val categoryAvailableMoney =
            categoryRepositoryFake.categories.value.find { it.id == rentCategoryEntity().id }!!.availableMoney
        val transactionAmount = transactionRepositoryFake.transactions.value.find { it.id == aldiGroceriesJanuary().id }!!.amount

        assertThat(accountBalance).isEqualTo(Money(490.0))
        assertThat(categoryAvailableMoney).isEqualTo(Money(1290.0))
        assertThat(transactionAmount).isEqualTo(Money(-60.0))
    }
}