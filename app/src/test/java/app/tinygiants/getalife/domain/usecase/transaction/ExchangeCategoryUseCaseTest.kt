package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.aldiGroceriesJanuary
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.insuranceCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.transactions
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
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

class ExchangeCategoryUseCaseTest {

    private lateinit var exchangeCategory: ExchangeCategoryUseCase
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

        exchangeCategory = ExchangeCategoryUseCase(
            transactionRepository = transactionRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )

        transactionRepositoryFake.transactions.value = transactions
        accountRepositoryFake.accounts.value = accounts
        categoryRepositoryFake.categories.value = categories
    }

    @Test
    fun `Change category to another category`(): Unit = runTest {
        val account = cashAccount().toDomain()
        val oldCategory = rentCategoryEntity().toDomain()
        val newCategory = insuranceCategoryEntity().toDomain()
        val updatedTransaction = aldiGroceriesJanuary().toDomain(account, newCategory).copy(transactionDirection = TransactionDirection.Inflow)

        exchangeCategory(transaction = updatedTransaction, oldCategory = oldCategory)

        val accountBalance = accountRepositoryFake.accounts.value.find { it.id == cashAccount().id }!!.balance
        val updatedOldCategory = categoryRepositoryFake.categories.value.find { it.id == oldCategory.id }!!
        val updatedNewCategory = categoryRepositoryFake.categories.value.find { it.id == newCategory.id }!!
        val transactionAfterUpdate = transactionRepositoryFake.transactions.value.find { it.id == aldiGroceriesJanuary().id }!!

        assertThat(accountBalance).isEqualTo(Money(500.0))
        assertThat(updatedOldCategory.availableMoney).isEqualTo(Money(1350.0))
        assertThat(updatedNewCategory.availableMoney).isEqualTo(Money(250.0))
        assertThat(transactionAfterUpdate.id).isEqualTo(updatedTransaction.id)
        assertThat(transactionAfterUpdate.id).isNotNull()
        assertThat(transactionAfterUpdate.category?.id).isEqualTo(newCategory.id)
        assertThat(transactionAfterUpdate.amount).isEqualTo(Money(-50.0))
    }
}