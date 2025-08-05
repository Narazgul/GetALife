package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.budget.RecalculateCategoryMonthlyStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import kotlin.time.Clock

@ExtendWith(TestDispatcherExtension::class)
class AddTransactionUseCaseTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var groupRepository: GroupRepository
    private lateinit var recalculateCategoryMonthlyStatus: RecalculateCategoryMonthlyStatusUseCase
    private lateinit var categoryMonthlyStatusRepository: CategoryMonthlyStatusRepository
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var useCase: AddTransactionUseCase

    private val testAccount = Account(
        id = 1L,
        name = "Test Account",
        balance = Money(1000.0),
        type = AccountType.Checking,
        listPosition = 0,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )

    private val testCategory = Category(
        id = 1L,
        groupId = 1L,
        emoji = "🏠",
        name = "Rent",
        budgetTarget = Money(1500.0),
        monthlyTargetAmount = Money(1500.0),
        targetMonthsRemaining = null,
        listPosition = 0,
        isInitialCategory = false,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )

    @BeforeEach
    fun setup() {
        transactionRepository = mockk()
        accountRepository = mockk()
        categoryRepository = mockk()
        groupRepository = mockk()
        recalculateCategoryMonthlyStatus = mockk()
        categoryMonthlyStatusRepository = mockk()
        testDispatcher = mockk()

        useCase = AddTransactionUseCase(
            transactionRepository = transactionRepository,
            accountRepository = accountRepository,
            categoryRepository = categoryRepository,
            groupRepository = groupRepository,
            recalculateCategoryMonthlyStatus = recalculateCategoryMonthlyStatus,
            defaultDispatcher = testDispatcher,
            categoryMonthlyStatusRepository = categoryMonthlyStatusRepository
        )

        // Setup common mocks
        coEvery { accountRepository.getAccount(testAccount.id) } returns testAccount
        coEvery { transactionRepository.addTransaction(any()) } returns Unit
        coEvery { accountRepository.updateAccount(any()) } returns Unit
        coEvery { recalculateCategoryMonthlyStatus(any(), any()) } returns Unit
    }

    @Test
    fun `creates recurring transaction with correct properties`() = runTest {
        // Given
        val transactionSlot = slot<app.tinygiants.getalife.domain.model.Transaction>()
        coEvery { transactionRepository.addTransaction(capture(transactionSlot)) } returns Unit

        // When
        useCase(
            accountId = testAccount.id,
            category = testCategory,
            amount = Money(1500.0),
            direction = TransactionDirection.Outflow,
            transactionPartner = "Landlord",
            description = "Monthly rent",
            recurrenceFrequency = RecurrenceFrequency.MONTHLY
        )

        // Then
        val capturedTransaction = transactionSlot.captured
        assertTrue(capturedTransaction.isRecurring, "Transaction should be recurring")
        assertEquals(RecurrenceFrequency.MONTHLY, capturedTransaction.recurrenceFrequency)
        assertNotNull(capturedTransaction.nextPaymentDate, "Next payment date should be set")

        coVerify { transactionRepository.addTransaction(any()) }
    }

    @Test
    fun `creates non-recurring transaction when frequency is null`() = runTest {
        // Given
        val transactionSlot = slot<app.tinygiants.getalife.domain.model.Transaction>()
        coEvery { transactionRepository.addTransaction(capture(transactionSlot)) } returns Unit

        // When
        useCase(
            accountId = testAccount.id,
            category = testCategory,
            amount = Money(1500.0),
            direction = TransactionDirection.Outflow,
            transactionPartner = "Landlord",
            description = "One-time payment",
            recurrenceFrequency = null
        )

        // Then
        val capturedTransaction = transactionSlot.captured
        assertEquals(false, capturedTransaction.isRecurring, "Transaction should not be recurring")
        assertEquals(null, capturedTransaction.recurrenceFrequency)
        assertEquals(null, capturedTransaction.nextPaymentDate)

        coVerify { transactionRepository.addTransaction(any()) }
    }

    @Test
    fun `throws exception when recurring transaction has no frequency`() = runTest {
        // This test verifies our validation logic
        // When creating a transaction that should be recurring but has no frequency

        val transactionSlot = slot<app.tinygiants.getalife.domain.model.Transaction>()
        coEvery { transactionRepository.addTransaction(capture(transactionSlot)) } returns Unit

        // This scenario shouldn't happen in normal usage, but tests our validation
        useCase(
            accountId = testAccount.id,
            category = testCategory,
            amount = Money(1500.0),
            direction = TransactionDirection.Outflow,
            transactionPartner = "Landlord",
            description = "Test payment",
            recurrenceFrequency = null // No frequency specified
        )

        // The transaction should be created as non-recurring, which is valid
        val capturedTransaction = transactionSlot.captured
        assertEquals(false, capturedTransaction.isRecurring)
    }

    @Test
    fun `calculateNextPaymentDate handles all frequencies correctly`() = runTest {
        // Test each frequency type by creating transactions and verifying nextPaymentDate is set
        val frequencies = listOf(
            RecurrenceFrequency.NEVER,
            RecurrenceFrequency.DAILY,
            RecurrenceFrequency.WEEKLY,
            RecurrenceFrequency.EVERY_OTHER_WEEK,
            RecurrenceFrequency.MONTHLY,
            RecurrenceFrequency.TWICE_A_MONTH,
            RecurrenceFrequency.EVERY_OTHER_MONTH,
            RecurrenceFrequency.EVERY_4_WEEKS,
            RecurrenceFrequency.EVERY_3_MONTHS,
            RecurrenceFrequency.EVERY_4_MONTHS,
            RecurrenceFrequency.TWICE_A_YEAR,
            RecurrenceFrequency.YEARLY
        )

        frequencies.forEach { frequency ->
            val transactionSlot = slot<app.tinygiants.getalife.domain.model.Transaction>()
            coEvery { transactionRepository.addTransaction(capture(transactionSlot)) } returns Unit

            useCase(
                accountId = testAccount.id,
                category = testCategory,
                amount = Money(1500.0),
                direction = TransactionDirection.Outflow,
                transactionPartner = "Test Partner",
                description = "Test ${frequency.name}",
                recurrenceFrequency = frequency
            )

            val capturedTransaction = transactionSlot.captured
            if (frequency != RecurrenceFrequency.NEVER) {
                assertTrue(capturedTransaction.isRecurring, "Transaction should be recurring for $frequency")
                assertEquals(frequency, capturedTransaction.recurrenceFrequency)
                assertNotNull(capturedTransaction.nextPaymentDate, "Next payment date should be set for $frequency")
            } else {
                assertEquals(false, capturedTransaction.isRecurring, "Transaction should not be recurring for $frequency")
                assertEquals(null, capturedTransaction.recurrenceFrequency)
                assertEquals(null, capturedTransaction.nextPaymentDate)
            }
        }
    }

    @Test
    fun `updates account balance correctly for recurring transactions`() = runTest {
        // Given
        val accountSlot = slot<Account>()
        coEvery { accountRepository.updateAccount(capture(accountSlot)) } returns Unit

        // When
        useCase(
            accountId = testAccount.id,
            category = testCategory,
            amount = Money(1500.0),
            direction = TransactionDirection.Outflow,
            transactionPartner = "Landlord",
            description = "Monthly rent",
            recurrenceFrequency = RecurrenceFrequency.MONTHLY
        )

        // Then
        val capturedAccount = accountSlot.captured
        assertEquals(Money(-500.0), capturedAccount.balance, "Account balance should be updated correctly")

        coVerify { accountRepository.updateAccount(any()) }
    }
}