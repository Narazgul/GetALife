package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.aldiGroceriesJanuary
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.insuranceCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.transactions
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
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
        transactionRepositoryFake = TransactionRepositoryFake()
        accountRepositoryFake = AccountRepositoryFake()
        categoryRepositoryFake = CategoryRepositoryFake()

        exchangeCategory = ExchangeCategoryUseCase(
            transactionRepository = transactionRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )

        transactionRepositoryFake.transactions.value = transactions
        accountRepositoryFake.accountsFlow.value = accounts
        categoryRepositoryFake.categories.value = categories
    }

    @Test
    fun `Change category with previous category available`(): Unit = runTest {
        val account = cashAccount().run {
            Account(
                id = id,
                name = name,
                balance = Money(balance),
                type = type,
                listPosition = listPosition,
                updatedAt = updatedAt,
                createdAt = createdAt,
            )
        }
        val oldCategory = rentCategoryEntity().run {
            Category(
                id = id,
                groupId = groupId,
                emoji = emoji,
                name = name,
                budgetTarget = Money(budgetTarget),
                assignedMoney = Money(assignedMoney),
                availableMoney = Money(availableMoney),
                progress = EmptyProgress(),
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }
        val newCategory = insuranceCategoryEntity().run {
            Category(
                id = id,
                groupId = groupId,
                emoji = emoji,
                name = name,
                budgetTarget = Money(budgetTarget),
                assignedMoney = Money(assignedMoney),
                availableMoney = Money(availableMoney),
                progress = EmptyProgress(),
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = updatedAt,
                createdAt = createdAt,
            )
        }
        val updatedTransaction = aldiGroceriesJanuary().run {
            Transaction(
                id = id,
                amount = Money(amount),
                account = account,
                category = newCategory,
                transactionPartner = transactionPartner,
                transactionDirection = TransactionDirection.Inflow,
                description = description,
                updatedAt = updatedAt,
                createdAt = createdAt,
            )
        }

        exchangeCategory(transaction = updatedTransaction, oldCategory = oldCategory)

        val accountBalance = accountRepositoryFake.accountsFlow.value.find { it.id == cashAccount().id }!!.balance
        val updatedOldCategory = categoryRepositoryFake.categories.value.find { it.id == oldCategory.id }!!
        val updatedNewCategory = categoryRepositoryFake.categories.value.find { it.id == newCategory.id }!!
        val transactionAfterUpdate = transactionRepositoryFake.transactions.value.find { it.id == aldiGroceriesJanuary().id }!!

        assertThat(accountBalance).isEqualTo(500.0)
        assertThat(updatedOldCategory.availableMoney).isEqualTo(1350.0)
        assertThat(updatedNewCategory.availableMoney).isEqualTo(250.0)
        assertThat(transactionAfterUpdate.id).isEqualTo(updatedTransaction.id)
        assertThat(transactionAfterUpdate.id).isNotNull()
        assertThat(transactionAfterUpdate.categoryId).isEqualTo(newCategory.id)
        assertThat(transactionAfterUpdate.amount).isEqualTo(-50.0)
    }
}