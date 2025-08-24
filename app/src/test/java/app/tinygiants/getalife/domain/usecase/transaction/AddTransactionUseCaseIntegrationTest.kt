package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.budget.RecalculateCategoryMonthlyStatusUseCase
import app.tinygiants.getalife.domain.usecase.transaction.credit_card.EnsureCreditCardPaymentCategoryUseCase
import app.tinygiants.getalife.domain.usecase.transaction.recurrence.CalculateRecurrenceDatesUseCase
import app.tinygiants.getalife.domain.usecase.transaction.validation.ValidateTransactionDataUseCase
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.YearMonth
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock

@DisplayName("AddTransactionUseCase Integration Tests")
class AddTransactionUseCaseIntegrationTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockAccountRepository: AccountRepository
    private lateinit var mockCategoryRepository: CategoryRepository
    private lateinit var mockGroupRepository: GroupRepository
    private lateinit var mockCategoryMonthlyStatusRepository: CategoryMonthlyStatusRepository
    private lateinit var mockValidateTransactionData: ValidateTransactionDataUseCase
    private lateinit var mockEnsureCreditCardPaymentCategory: EnsureCreditCardPaymentCategoryUseCase
    private lateinit var mockCalculateRecurrenceDates: CalculateRecurrenceDatesUseCase
    private lateinit var mockRecalculateCategoryMonthlyStatus: RecalculateCategoryMonthlyStatusUseCase
    private lateinit var addTransactionUseCase: AddTransactionUseCase

    @BeforeEach
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        mockTransactionRepository = mockk(relaxed = true)
        mockAccountRepository = mockk(relaxed = true)
        mockCategoryRepository = mockk(relaxed = true)
        mockGroupRepository = mockk(relaxed = true)
        mockCategoryMonthlyStatusRepository = mockk(relaxed = true)
        mockValidateTransactionData = mockk(relaxed = true)
        mockEnsureCreditCardPaymentCategory = mockk(relaxed = true)
        mockCalculateRecurrenceDates = mockk(relaxed = true)
        mockRecalculateCategoryMonthlyStatus = mockk(relaxed = true)

        addTransactionUseCase = AddTransactionUseCase(
            transactionRepository = mockTransactionRepository,
            accountRepository = mockAccountRepository,
            categoryRepository = mockCategoryRepository,
            groupRepository = mockGroupRepository,
            validateTransactionData = mockValidateTransactionData,
            ensureCreditCardPaymentCategory = mockEnsureCreditCardPaymentCategory,
            calculateRecurrenceDates = mockCalculateRecurrenceDates,
            recalculateCategoryMonthlyStatus = mockRecalculateCategoryMonthlyStatus,
            defaultDispatcher = testDispatcher,
            categoryMonthlyStatusRepository = mockCategoryMonthlyStatusRepository
        )
    }

    private fun createMockAccount(id: Long, type: AccountType = AccountType.Checking, balance: Money = Money(1000.0)): Account {
        return mockk<Account>().also {
            coEvery { it.id } returns id
            coEvery { it.type } returns type
            coEvery { it.balance } returns balance
            coEvery { it.copy(any(), any()) } returns it
        }
    }

    private fun createMockCategory(id: Long): Category {
        return mockk<Category>().also {
            coEvery { it.id } returns id
        }
    }

    @Nested
    @DisplayName("Normal transaction flow")
    inner class NormalTransactionFlow {

        @Test
        @DisplayName("should complete full transaction flow for normal transaction")
        fun completesFullTransactionFlow() = runTest {
            // Arrange
            val accountId = 1L
            val categoryId = 2L
            val amount = Money(100.0)
            val direction = TransactionDirection.Outflow
            val transactionPartner = "Test Store"
            val description = "Test Purchase"

            val mockAccount = createMockAccount(accountId)
            val mockCategory = createMockCategory(categoryId)
            val transactionSlot = slot<Transaction>()

            coEvery { mockAccountRepository.getAccount(accountId) } returns mockAccount
            coEvery { mockCategoryRepository.getCategory(categoryId) } returns mockCategory
            coEvery { mockTransactionRepository.addTransaction(capture(transactionSlot)) } returns Unit

            // Act
            addTransactionUseCase(
                accountId = accountId,
                categoryId = categoryId,
                amount = amount,
                direction = direction,
                transactionPartner = transactionPartner,
                description = description
            )

            // Assert - Verify all components were called in correct order
            coVerify { mockValidateTransactionData(any()) }
            coVerify { mockAccountRepository.getAccount(accountId) }
            coVerify { mockCategoryRepository.getCategory(categoryId) }
            coVerify { mockTransactionRepository.addTransaction(any()) }
            coVerify { mockAccountRepository.updateAccount(any()) }
            coVerify { mockRecalculateCategoryMonthlyStatus(categoryId, any<YearMonth>()) }

            // Verify transaction was created correctly
            val capturedTransaction = transactionSlot.captured
            assertThat(capturedTransaction.account).isEqualTo(mockAccount)
            assertThat(capturedTransaction.category).isEqualTo(mockCategory)
            assertThat(capturedTransaction.amount).isEqualTo(Money(-100.0)) // Negative for outflow
            assertThat(capturedTransaction.transactionDirection).isEqualTo(direction)
            assertThat(capturedTransaction.transactionPartner).isEqualTo(transactionPartner)
            assertThat(capturedTransaction.description).isEqualTo(description)
        }

        @Test
        @DisplayName("should handle recurring transactions correctly")
        fun handlesRecurringTransactions() = runTest {
            // Arrange
            val accountId = 1L
            val categoryId = 2L
            val amount = Money(50.0)
            val direction = TransactionDirection.Inflow
            val recurrenceFrequency = RecurrenceFrequency.MONTHLY
            val currentDate = Clock.System.now()
            val nextPaymentDate = Clock.System.now() // Mocked return value

            val mockAccount = createMockAccount(accountId)
            val mockCategory = createMockCategory(categoryId)
            val transactionSlot = slot<Transaction>()

            coEvery { mockAccountRepository.getAccount(accountId) } returns mockAccount
            coEvery { mockCategoryRepository.getCategory(categoryId) } returns mockCategory
            coEvery { mockCalculateRecurrenceDates(any(), recurrenceFrequency) } returns nextPaymentDate
            coEvery { mockTransactionRepository.addTransaction(capture(transactionSlot)) } returns Unit

            // Act
            addTransactionUseCase(
                accountId = accountId,
                categoryId = categoryId,
                amount = amount,
                direction = direction,
                transactionPartner = "Salary",
                description = "Monthly Salary",
                recurrenceFrequency = recurrenceFrequency
            )

            // Assert
            coVerify { mockCalculateRecurrenceDates(any(), recurrenceFrequency) }

            val capturedTransaction = transactionSlot.captured
            assertThat(capturedTransaction.isRecurring).isEqualTo(true)
            assertThat(capturedTransaction.recurrenceFrequency).isEqualTo(recurrenceFrequency)
            assertThat(capturedTransaction.nextPaymentDate).isEqualTo(nextPaymentDate)
        }
    }

    @Nested
    @DisplayName("Credit card transaction flow")
    inner class CreditCardTransactionFlow {

        @Test
        @DisplayName("should handle credit card outflow transactions")
        fun handlesCreditCardOutflowTransactions() = runTest {
            // Arrange
            val accountId = 1L
            val categoryId = 2L
            val amount = Money(75.0)
            val direction = TransactionDirection.Outflow

            val mockCreditCardAccount = createMockAccount(accountId, AccountType.CreditCard)
            val mockCategory = createMockCategory(categoryId)
            val mockPaymentCategory = createMockCategory(3L)

            coEvery { mockAccountRepository.getAccount(accountId) } returns mockCreditCardAccount
            coEvery { mockCategoryRepository.getCategory(categoryId) } returns mockCategory
            coEvery { mockEnsureCreditCardPaymentCategory(mockCreditCardAccount) } returns mockPaymentCategory

            // Act
            addTransactionUseCase(
                accountId = accountId,
                categoryId = categoryId,
                amount = amount,
                direction = direction,
                transactionPartner = "Store",
                description = "Credit card purchase"
            )

            // Assert
            coVerify { mockEnsureCreditCardPaymentCategory(mockCreditCardAccount) }
            coVerify { mockTransactionRepository.addTransaction(any()) }
            coVerify { mockAccountRepository.updateAccount(any()) }
        }

        @Test
        @DisplayName("should not call credit card use case for non-credit card accounts")
        fun doesNotCallCreditCardUseCaseForNormalAccounts() = runTest {
            // Arrange
            val accountId = 1L
            val categoryId = 2L
            val amount = Money(75.0)
            val direction = TransactionDirection.Outflow

            val mockCheckingAccount = createMockAccount(accountId, AccountType.Checking)
            val mockCategory = createMockCategory(categoryId)

            coEvery { mockAccountRepository.getAccount(accountId) } returns mockCheckingAccount
            coEvery { mockCategoryRepository.getCategory(categoryId) } returns mockCategory

            // Act
            addTransactionUseCase(
                accountId = accountId,
                categoryId = categoryId,
                amount = amount,
                direction = direction,
                transactionPartner = "Store",
                description = "Regular purchase"
            )

            // Assert
            coVerify(exactly = 0) { mockEnsureCreditCardPaymentCategory(any()) }
        }

        @Test
        @DisplayName("should not call credit card use case for inflow transactions")
        fun doesNotCallCreditCardUseCaseForInflowTransactions() = runTest {
            // Arrange
            val accountId = 1L
            val categoryId = 2L
            val amount = Money(100.0)
            val direction = TransactionDirection.Inflow

            val mockCreditCardAccount = createMockAccount(accountId, AccountType.CreditCard)
            val mockCategory = createMockCategory(categoryId)

            coEvery { mockAccountRepository.getAccount(accountId) } returns mockCreditCardAccount
            coEvery { mockCategoryRepository.getCategory(categoryId) } returns mockCategory

            // Act
            addTransactionUseCase(
                accountId = accountId,
                categoryId = categoryId,
                amount = amount,
                direction = direction,
                transactionPartner = "Credit",
                description = "Credit card refund"
            )

            // Assert
            coVerify(exactly = 0) { mockEnsureCreditCardPaymentCategory(any()) }
        }
    }

    @Nested
    @DisplayName("Error scenarios")
    inner class ErrorScenarios {

        @Test
        @DisplayName("should handle validation errors gracefully")
        fun handlesValidationErrors() = runTest {
            // Arrange
            val exception = IllegalArgumentException("Invalid transaction data")
            coEvery { mockValidateTransactionData(any()) } throws exception

            // Act & Assert
            val result = runCatching {
                addTransactionUseCase(
                    accountId = 1L,
                    amount = Money(-100.0), // Invalid negative amount
                    direction = TransactionDirection.Outflow,
                    transactionPartner = "Store",
                    description = "Test"
                )
            }

            assertThat(result.isFailure).isEqualTo(true)
            assertThat(result.exceptionOrNull()).isEqualTo(exception)

            // Verify no further processing happened
            coVerify(exactly = 0) { mockTransactionRepository.addTransaction(any()) }
            coVerify(exactly = 0) { mockAccountRepository.updateAccount(any()) }
        }

        @Test
        @DisplayName("should handle missing account gracefully")
        fun handlesMissingAccount() = runTest {
            // Arrange
            val accountId = 999L
            coEvery { mockAccountRepository.getAccount(accountId) } returns null

            // Act & Assert
            val result = runCatching {
                addTransactionUseCase(
                    accountId = accountId,
                    amount = Money(100.0),
                    direction = TransactionDirection.Outflow,
                    transactionPartner = "Store",
                    description = "Test"
                )
            }

            assertThat(result.isFailure).isEqualTo(true)
            assertThat(result.exceptionOrNull()).isNotNull()

            // Verify transaction was not created
            coVerify(exactly = 0) { mockTransactionRepository.addTransaction(any()) }
        }

        @Test
        @DisplayName("should handle missing category gracefully for transactions without categories")
        fun handlesMissingCategory() = runTest {
            // Arrange
            val accountId = 1L
            val categoryId = 999L
            val mockAccount = createMockAccount(accountId)

            coEvery { mockAccountRepository.getAccount(accountId) } returns mockAccount
            coEvery { mockCategoryRepository.getCategory(categoryId) } returns null

            // Act
            addTransactionUseCase(
                accountId = accountId,
                categoryId = categoryId, // Non-existent category
                amount = Money(100.0),
                direction = TransactionDirection.Outflow,
                transactionPartner = "Store",
                description = "Test"
            )

            // Assert - Should still work but with null category
            coVerify { mockTransactionRepository.addTransaction(any()) }
            coVerify(exactly = 0) { mockRecalculateCategoryMonthlyStatus(any(), any<YearMonth>()) }
        }
    }

    @Nested
    @DisplayName("Transaction amount transformation")
    inner class TransactionAmountTransformation {

        @Test
        @DisplayName("should transform inflow amounts to positive")
        fun transformsInflowToPositive() = runTest {
            // Arrange
            val accountId = 1L
            val amount = Money(100.0)
            val mockAccount = createMockAccount(accountId)
            val transactionSlot = slot<Transaction>()

            coEvery { mockAccountRepository.getAccount(accountId) } returns mockAccount
            coEvery { mockTransactionRepository.addTransaction(capture(transactionSlot)) } returns Unit

            // Act
            addTransactionUseCase(
                accountId = accountId,
                amount = amount,
                direction = TransactionDirection.Inflow,
                transactionPartner = "Income",
                description = "Test"
            )

            // Assert
            val capturedTransaction = transactionSlot.captured
            assertThat(capturedTransaction.amount).isEqualTo(Money(100.0)) // Positive for inflow
        }

        @Test
        @DisplayName("should transform outflow amounts to negative")
        fun transformsOutflowToNegative() = runTest {
            // Arrange
            val accountId = 1L
            val amount = Money(100.0)
            val mockAccount = createMockAccount(accountId)
            val transactionSlot = slot<Transaction>()

            coEvery { mockAccountRepository.getAccount(accountId) } returns mockAccount
            coEvery { mockTransactionRepository.addTransaction(capture(transactionSlot)) } returns Unit

            // Act
            addTransactionUseCase(
                accountId = accountId,
                amount = amount,
                direction = TransactionDirection.Outflow,
                transactionPartner = "Store",
                description = "Test"
            )

            // Assert
            val capturedTransaction = transactionSlot.captured
            assertThat(capturedTransaction.amount).isEqualTo(Money(-100.0)) // Negative for outflow
        }
    }
}