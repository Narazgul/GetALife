package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.aldiGroceriesJanuary
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.toAccount
import app.tinygiants.getalife.data.local.datagenerator.toTransaction
import app.tinygiants.getalife.data.local.datagenerator.transactions
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.BudgetRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import app.tinygiants.getalife.domain.usecase.budget.UpdateAssignableMoneyUseCase
import assertk.assertThat
import assertk.assertions.containsNone
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class DeleteTransactionUseCaseTest {

    private lateinit var deleteTransaction: DeleteTransactionUseCase
    private lateinit var updateAssignableMoney: UpdateAssignableMoneyUseCase
    private lateinit var budgetRepositoryFake: BudgetRepositoryFake
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
        budgetRepositoryFake = BudgetRepositoryFake()
        transactionRepositoryFake = TransactionRepositoryFake()
        accountRepositoryFake = AccountRepositoryFake()
        categoryRepositoryFake = CategoryRepositoryFake()
        updateAssignableMoney = UpdateAssignableMoneyUseCase(
            repository = budgetRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )

        deleteTransaction = DeleteTransactionUseCase(
            updateAssignableMoney = updateAssignableMoney,
            transactionRepository = transactionRepositoryFake,
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `Remove Transaction without category involved`(): Unit = runTest {
        transactionRepositoryFake.transactions.value = transactions
        accountRepositoryFake.accountsFlow.value = accounts

        val accountEntity =
            accountRepositoryFake.accountsFlow.value.find { account -> account.id == aldiGroceriesJanuary().accountId }
        val account = accountEntity?.run {
            Account(
                id = id,
                name = name,
                balance = Money(balance),
                type = type,
                listPosition = listPosition,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }
        assertThat(account).isNotNull()

        val transactionToBeDeleted = aldiGroceriesJanuary().run {
            Transaction(
                id = id,
                amount = Money(amount),
                account = account,
                category = null,
                transactionPartner = transactionPartner,
                transactionDirection = transactionDirection,
                description = description,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }

        deleteTransaction(transaction = transactionToBeDeleted)

        assertThat(transactionRepositoryFake.transactions.value).hasSize(20)
        assertThat(transactionRepositoryFake.transactions.value).containsNone(aldiGroceriesJanuary())

        val accountAfterTransaction = accountRepositoryFake.accountsFlow.value.first()
        assertThat(accountAfterTransaction.name).isEqualTo("Cash Account")
        assertThat(accountAfterTransaction.balance).isEqualTo(550.0)
    }

    @Test
    fun `Remove transaction with category involved`(): Unit = runTest {
        transactionRepositoryFake.transactions.value = transactions
        accountRepositoryFake.accountsFlow.value = accounts
        categoryRepositoryFake.categories.value = categories

        val accountEntity =
            accountRepositoryFake.accountsFlow.value.find { account -> account.id == aldiGroceriesJanuary().accountId }
        val account = accountEntity?.run {
            Account(
                id = id,
                name = name,
                balance = Money(balance),
                type = type,
                listPosition = listPosition,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }
        assertThat(account).isNotNull()

        val categoryEntity = categoryRepositoryFake.categories.value.find { category -> category.id == 5L }
        val category = categoryEntity?.run {
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
        assertThat(category).isNotNull()

        val transactionToBeDeleted = aldiGroceriesJanuary().run {
            Transaction(
                id = id,
                amount = Money(amount),
                account = account,
                category = category,
                transactionPartner = transactionPartner,
                transactionDirection = transactionDirection,
                description = description,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }

        deleteTransaction(transactionToBeDeleted)

        assertThat(transactionRepositoryFake.transactions.value).hasSize(20)
        assertThat(transactionRepositoryFake.transactions.value).containsNone(aldiGroceriesJanuary())

        val accountAfterTransactionDeleted = accountRepositoryFake.accountsFlow.value.first()
        assertThat(accountAfterTransactionDeleted.name).isEqualTo("Cash Account")
        assertThat(accountAfterTransactionDeleted.balance).isEqualTo(550.0)

        val categoryAfterTransactionDeleted = categoryRepositoryFake.categories.value[4]
        assertThat(categoryAfterTransactionDeleted.name).isEqualTo("Groceries")
        assertThat(categoryAfterTransactionDeleted.budgetTarget).isEqualTo(300.0)
        assertThat(categoryAfterTransactionDeleted.assignedMoney).isEqualTo(150.0)
        assertThat(categoryAfterTransactionDeleted.availableMoney).isEqualTo(120.0)
    }

    @Test
    fun `Delete starting budget Transaction of Account`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value = listOf(cashAccount())
        val startingBalanceTransactionEntity = TransactionEntity(
            id = 0L,
            accountId = cashAccount().id,
            categoryId = null,
            amount = 100.0,
            transactionPartner = "",
            transactionDirection = TransactionDirection.Inflow,
            description = "",
            updatedAt = Clock.System.now(),
            createdAt = Clock.System.now(),
        )
        transactionRepositoryFake.transactions.value = listOf(startingBalanceTransactionEntity)
        budgetRepositoryFake.budgetsFlow.value =
            listOf(budgetRepositoryFake.initialBudget.copy(readyToAssign = 100.0))

        assertThat(budgetRepositoryFake.budgetsFlow.value.first().readyToAssign).isEqualTo(100.0)

        val transactionToBeDeleted = startingBalanceTransactionEntity.toTransaction(account = cashAccount().toAccount())

        deleteTransaction(transactionToBeDeleted)

        val account = accountRepositoryFake.accountsFlow.value.first()
        assertThat(account.balance).isEqualTo(400.0)

        val readyToAssignMoney = budgetRepositoryFake.budgetsFlow.value.first().readyToAssign
        assertThat(readyToAssignMoney).isEqualTo(0.0)
    }
}