package app.tinygiants.getalife.domain.usecase.transaction

import aldiGroceriesJanuary
import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.groceriesCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
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
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import transactions

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
        transactionRepositoryFake = TransactionRepositoryFake()
        accountRepositoryFake = AccountRepositoryFake()
        categoryRepositoryFake = CategoryRepositoryFake()

        updateTransaction = UpdateTransactionUseCase(
            transactionRepository = transactionRepositoryFake,
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )

        transactionRepositoryFake.transactions.value = transactions
        accountRepositoryFake.accountsFlow.value = accounts
        categoryRepositoryFake.categories.value = categories
    }

    @Test
    fun `Test changing nothing on the update`(): Unit = runTest {
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
        val category = groceriesCategoryEntity().run {
            Category(
                id = id,
                groupId = groupId,
                emoji = emoji,
                name = name,
                budgetTarget = Money(budgetTarget ?: 0.0),
                budgetPurpose = budgetPurpose,
                assignedMoney = Money(assignedMoney),
                availableMoney = Money(availableMoney),
                progress = EmptyProgress(),
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }
        val updatedTransaction = aldiGroceriesJanuary().run {
            Transaction(
                id = id,
                amount = Money(amount),
                account = account,
                category = category,
                transactionPartner = transactionPartner,
                transactionDirection = transactionDirection,
                description = description,
                updatedAt = updatedAt,
                createdAt = createdAt,
            )
        }

        updateTransaction(updatedTransaction)

        assertThat(transactionRepositoryFake.transactions.value).contains(aldiGroceriesJanuary())
        assertThat(transactionRepositoryFake.transactions.value[updatedTransaction.id.toInt()-1].amount).isEqualTo(aldiGroceriesJanuary().amount)
        assertThat(accountRepositoryFake.accountsFlow.value[account.id.toInt()-1].balance).isEqualTo(cashAccount().balance)
        assertThat(categoryRepositoryFake.categories.value[category.id.toInt()-1].availableMoney).isEqualTo(groceriesCategoryEntity().availableMoney)
    }

    @Test
    fun `Test reducing transaction amount`(): Unit = runTest {
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
        val category = rentCategoryEntity().run {
            Category(
                id = id,
                groupId = groupId,
                emoji = emoji,
                name = name,
                budgetTarget = Money(budgetTarget ?: 0.0),
                budgetPurpose = budgetPurpose,
                assignedMoney = Money(assignedMoney),
                availableMoney = Money(availableMoney),
                progress = EmptyProgress(),
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }
        val updatedTransaction = aldiGroceriesJanuary().run {
            Transaction(
                id = id,
                amount = Money(amount) - Money(10.0),
                account = account,
                category = category,
                transactionPartner = transactionPartner,
                transactionDirection = TransactionDirection.Outflow,
                description = description,
                updatedAt = updatedAt,
                createdAt = createdAt,
            )
        }

        updateTransaction(updatedTransaction)

        assertThat(transactionRepositoryFake.transactions.value.first().amount).isEqualTo(aldiGroceriesJanuary().amount - 10.0)
        assertThat(categoryRepositoryFake.categories.value.first().availableMoney).isEqualTo(rentCategoryEntity().availableMoney + 10.0)
        assertThat(accountRepositoryFake.accountsFlow.value.first().balance).isEqualTo(cashAccount().balance + 10.0)
    }

    @Test
    fun `Test raising transaction amount`(): Unit = runTest {
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
        val category = rentCategoryEntity().run {
            Category(
                id = id,
                groupId = groupId,
                emoji = emoji,
                name = name,
                budgetTarget = Money(budgetTarget ?: 0.0),
                budgetPurpose = budgetPurpose,
                assignedMoney = Money(assignedMoney),
                availableMoney = Money(availableMoney),
                progress = EmptyProgress(),
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }
        val updatedTransaction = aldiGroceriesJanuary().run {
            Transaction(
                id = id,
                amount = Money(amount) + Money(10.0),
                account = account,
                category = category,
                transactionPartner = transactionPartner,
                transactionDirection = TransactionDirection.Inflow,
                description = description,
                updatedAt = updatedAt,
                createdAt = createdAt,
            )
        }

        updateTransaction(updatedTransaction)

        assertThat(transactionRepositoryFake.transactions.value.first().amount).isEqualTo(aldiGroceriesJanuary().amount + 10.0)
        assertThat(categoryRepositoryFake.categories.value.first().availableMoney).isEqualTo(rentCategoryEntity().availableMoney - 10.0)
        assertThat(accountRepositoryFake.accountsFlow.value.first().balance).isEqualTo(cashAccount().balance - 10.0)
    }
}