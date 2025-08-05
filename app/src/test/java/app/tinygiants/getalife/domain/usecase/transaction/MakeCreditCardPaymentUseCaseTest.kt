package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.checkingAccount
import app.tinygiants.getalife.data.local.datagenerator.creditCardAccount
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.GroupRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import app.tinygiants.getalife.domain.usecase.budget.RecalculateCategoryMonthlyStatusUseCase
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class MakeCreditCardPaymentUseCaseTest {

    private lateinit var makeCreditCardPayment: MakeCreditCardPaymentUseCase
    private lateinit var accountRepositoryFake: AccountRepositoryFake
    private lateinit var transactionRepositoryFake: TransactionRepositoryFake
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
        val groupRepositoryFake = GroupRepositoryFake()
        val statusRepositoryFake = CategoryMonthlyStatusRepositoryFake()

        val recalculateCategoryMonthlyStatusUseCase = RecalculateCategoryMonthlyStatusUseCase(
            statusRepository = statusRepositoryFake,
            transactionRepository = transactionRepositoryFake,
            categoryRepository = categoryRepositoryFake
        )

        val addTransactionUseCase = AddTransactionUseCase(
            transactionRepository = transactionRepositoryFake,
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            groupRepository = groupRepositoryFake,
            recalculateCategoryMonthlyStatus = recalculateCategoryMonthlyStatusUseCase,
            defaultDispatcher = testDispatcherExtension.testDispatcher,
            categoryMonthlyStatusRepository = statusRepositoryFake
        )
        makeCreditCardPayment = MakeCreditCardPaymentUseCase(
            addTransaction = addTransactionUseCase,
            accountRepository = accountRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `Make credit card payment reduces debt and updates source account`(): Unit = runTest {
        val creditCardAccount = creditCardAccount().toDomain().copy(balance = Money(-200.0))
        val checkingAccount = checkingAccount().toDomain().copy(balance = Money(500.0))
        accountRepositoryFake.accounts.value = listOf(creditCardAccount, checkingAccount)

        val paymentCategory = createPaymentCategory(creditCardAccount.id)
        categoryRepositoryFake.categories.value = listOf(paymentCategory)

        makeCreditCardPayment(
            creditCardAccount = creditCardAccount,
            paymentCategory = paymentCategory,
            paymentAmount = Money(100.0),
            fromAccountId = checkingAccount.id
        )

        val updatedCreditCard = accountRepositoryFake.accounts.value.find { it.id == creditCardAccount.id }!!
        val updatedChecking = accountRepositoryFake.accounts.value.find { it.id == checkingAccount.id }!!

        assertThat(updatedCreditCard.balance).isEqualTo(Money(-100.0))
        assertThat(updatedChecking.balance).isEqualTo(Money(400.0))

        val paymentTransaction = transactionRepositoryFake.transactions.value.find {
            it.transactionDirection == TransactionDirection.CreditCardPayment &&
                    it.account.id == creditCardAccount.id
        }
        assertThat(paymentTransaction).isNotNull()
        assertThat(paymentTransaction!!.amount).isEqualTo(Money(100.0))
        assertThat(paymentTransaction.category?.id).isEqualTo(paymentCategory.id)
        assertThat(paymentTransaction.transactionPartner).isEqualTo("Credit Card Payment")
    }

    @Test
    fun `Make full payment clears credit card debt completely`(): Unit = runTest {
        val creditCardAccount = creditCardAccount().toDomain().copy(balance = Money(-150.0))
        val checkingAccount = checkingAccount().toDomain().copy(balance = Money(300.0))
        accountRepositoryFake.accounts.value = listOf(creditCardAccount, checkingAccount)

        val paymentCategory = createPaymentCategory(creditCardAccount.id)
        categoryRepositoryFake.categories.value = listOf(paymentCategory)

        makeCreditCardPayment(
            creditCardAccount = creditCardAccount,
            paymentCategory = paymentCategory,
            paymentAmount = Money(150.0),
            fromAccountId = checkingAccount.id
        )

        val updatedCreditCardAccount = accountRepositoryFake.accounts.value.find { it.id == creditCardAccount.id }!!
        assertThat(updatedCreditCardAccount.balance).isEqualTo(Money(0.0))

        val updatedCheckingAccount = accountRepositoryFake.accounts.value.find { it.id == checkingAccount.id }!!
        assertThat(updatedCheckingAccount.balance).isEqualTo(Money(150.0))
    }

    @Test
    fun `Make overpayment creates positive credit card balance`(): Unit = runTest {
        val creditCardAccount = creditCardAccount().toDomain().copy(balance = Money(-50.0))
        val cashAccount = cashAccount().toDomain().copy(balance = Money(200.0))
        accountRepositoryFake.accounts.value = listOf(creditCardAccount, cashAccount)

        val paymentCategory = createPaymentCategory(creditCardAccount.id)
        categoryRepositoryFake.categories.value = listOf(paymentCategory)

        makeCreditCardPayment(
            creditCardAccount = creditCardAccount,
            paymentCategory = paymentCategory,
            paymentAmount = Money(100.0),
            fromAccountId = cashAccount.id
        )

        val updatedCreditCardAccount = accountRepositoryFake.accounts.value.find { it.id == creditCardAccount.id }!!
        assertThat(updatedCreditCardAccount.balance).isEqualTo(Money(50.0))

        val updatedCashAccount = accountRepositoryFake.accounts.value.find { it.id == cashAccount.id }!!
        assertThat(updatedCashAccount.balance).isEqualTo(Money(100.0))
    }

    @Test
    fun `Make minimum payment on large debt`(): Unit = runTest {
        val creditCardAccount = creditCardAccount().toDomain().copy(balance = Money(-2000.0))
        val checkingAccount = checkingAccount().toDomain().copy(balance = Money(500.0))
        accountRepositoryFake.accounts.value = listOf(creditCardAccount, checkingAccount)

        val paymentCategory = createPaymentCategory(creditCardAccount.id)
        categoryRepositoryFake.categories.value = listOf(paymentCategory)

        makeCreditCardPayment(
            creditCardAccount = creditCardAccount,
            paymentCategory = paymentCategory,
            paymentAmount = Money(50.0),
            fromAccountId = checkingAccount.id
        )

        val updatedCreditCardAccount = accountRepositoryFake.accounts.value.find { it.id == creditCardAccount.id }!!
        assertThat(updatedCreditCardAccount.balance).isEqualTo(Money(-1950.0))

        val paymentTransaction = transactionRepositoryFake.transactions.value.find {
            it.transactionDirection == TransactionDirection.CreditCardPayment
        }
        assertThat(paymentTransaction).isNotNull()
        assertThat(paymentTransaction!!.amount).isEqualTo(Money(50.0))
    }

    @Test
    fun `Payment from insufficient funds account still processes`(): Unit = runTest {
        val creditCardAccount = creditCardAccount().toDomain().copy(balance = Money(-100.0))
        val cashAccount = cashAccount().toDomain().copy(balance = Money(30.0))
        accountRepositoryFake.accounts.value = listOf(creditCardAccount, cashAccount)

        val paymentCategory = createPaymentCategory(creditCardAccount.id)
        categoryRepositoryFake.categories.value = listOf(paymentCategory)

        makeCreditCardPayment(
            creditCardAccount = creditCardAccount,
            paymentCategory = paymentCategory,
            paymentAmount = Money(50.0),
            fromAccountId = cashAccount.id
        )

        val updatedCreditCardAccount = accountRepositoryFake.accounts.value.find { it.id == creditCardAccount.id }!!
        assertThat(updatedCreditCardAccount.balance).isEqualTo(Money(-50.0))

        val updatedCashAccount = accountRepositoryFake.accounts.value.find { it.id == cashAccount.id }!!
        assertThat(updatedCashAccount.balance).isEqualTo(Money(-20.0))
    }

    @Test
    fun `Multiple payments on same credit card accumulate correctly`(): Unit = runTest {
        val creditCardAccount = creditCardAccount().toDomain().copy(balance = Money(-300.0))
        val checkingAccount = checkingAccount().toDomain().copy(balance = Money(500.0))
        accountRepositoryFake.accounts.value = listOf(creditCardAccount, checkingAccount)

        val paymentCategory = createPaymentCategory(creditCardAccount.id)
        categoryRepositoryFake.categories.value = listOf(paymentCategory)

        makeCreditCardPayment(
            creditCardAccount = creditCardAccount,
            paymentCategory = paymentCategory,
            paymentAmount = Money(100.0),
            fromAccountId = checkingAccount.id
        )

        makeCreditCardPayment(
            creditCardAccount = accountRepositoryFake.accounts.value.find { it.id == creditCardAccount.id }!!,
            paymentCategory = paymentCategory,
            paymentAmount = Money(150.0),
            fromAccountId = checkingAccount.id
        )

        val finalCreditCard = accountRepositoryFake.accounts.value.find { it.id == creditCardAccount.id }!!
        assertThat(finalCreditCard.balance).isEqualTo(Money(-50.0))

        val finalChecking = accountRepositoryFake.accounts.value.find { it.id == checkingAccount.id }!!
        assertThat(finalChecking.balance).isEqualTo(Money(250.0))

        val paymentTransactions = transactionRepositoryFake.transactions.value.filter {
            it.transactionDirection == TransactionDirection.CreditCardPayment
        }
        assertThat(paymentTransactions.size).isEqualTo(2)
    }

    @Test
    fun `Payment with zero amount creates no transaction`(): Unit = runTest {
        val creditCardAccount = creditCardAccount().toDomain().copy(balance = Money(-100.0))
        val cashAccount = cashAccount().toDomain().copy(balance = Money(200.0))
        accountRepositoryFake.accounts.value = listOf(creditCardAccount, cashAccount)

        val paymentCategory = createPaymentCategory(creditCardAccount.id)
        categoryRepositoryFake.categories.value = listOf(paymentCategory)

        makeCreditCardPayment(
            creditCardAccount = creditCardAccount,
            paymentCategory = paymentCategory,
            paymentAmount = Money(0.0),
            fromAccountId = cashAccount.id
        )

        val updatedCreditCard = accountRepositoryFake.accounts.value.find { it.id == creditCardAccount.id }!!
        val updatedCash = accountRepositoryFake.accounts.value.find { it.id == cashAccount.id }!!
        assertThat(updatedCreditCard.balance).isEqualTo(Money(-100.0))
        assertThat(updatedCash.balance).isEqualTo(Money(200.0))

        val paymentTransaction = transactionRepositoryFake.transactions.value.find {
            it.transactionDirection == TransactionDirection.CreditCardPayment
        }
        assertThat(paymentTransaction).isNotNull()
        assertThat(paymentTransaction!!.amount).isEqualTo(Money(0.0))
    }

    @Test
    fun `Payment creates transaction with correct metadata`(): Unit = runTest {
        val creditCardAccount = creditCardAccount().toDomain().copy(balance = Money(-200.0))
        val cashAccount = cashAccount().toDomain().copy(balance = Money(300.0))
        accountRepositoryFake.accounts.value = listOf(creditCardAccount, cashAccount)

        val paymentCategory = createPaymentCategory(creditCardAccount.id)
        categoryRepositoryFake.categories.value = listOf(paymentCategory)

        makeCreditCardPayment(
            creditCardAccount = creditCardAccount,
            paymentCategory = paymentCategory,
            paymentAmount = Money(75.0),
            fromAccountId = cashAccount.id
        )

        val transaction = transactionRepositoryFake.transactions.value.find {
            it.transactionDirection == TransactionDirection.CreditCardPayment
        }
        assertThat(transaction).isNotNull()
        assertThat(transaction!!.transactionPartner).isEqualTo("Credit Card Payment")
        assertThat(transaction.description).isEqualTo("Credit card payment")
        assertThat(transaction.account.type).isEqualTo(AccountType.CreditCard)
        assertThat(transaction.category?.linkedAccountId).isEqualTo(creditCardAccount.id)
    }

    private fun createPaymentCategory(creditCardAccountId: Long): Category {
        return Category(
            id = 10L,
            groupId = 1L,
            emoji = "💳",
            name = "Credit Card Payment",
            budgetTarget = Money(0.0),
            monthlyTargetAmount = null,
            targetMonthsRemaining = null,
            listPosition = 0,
            isInitialCategory = false,
            linkedAccountId = creditCardAccountId,
            updatedAt = Clock.System.now(),
            createdAt = Clock.System.now()
        )
    }
}