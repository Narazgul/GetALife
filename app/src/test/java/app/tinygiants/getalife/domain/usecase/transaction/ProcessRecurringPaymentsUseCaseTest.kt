package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Clock
import kotlin.time.Instant

@ExtendWith(TestDispatcherExtension::class)
class ProcessRecurringPaymentsUseCaseTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var useCase: ProcessRecurringPaymentsUseCase

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
        useCase = ProcessRecurringPaymentsUseCase(transactionRepository)
    }

    @Test
    fun `invoke processes due recurring payments and creates new transactions`() = runTest {
        // Given
        val recurringTransaction = createRecurringTransaction(RecurrenceFrequency.MONTHLY)
        coEvery { transactionRepository.getDueRecurringTransactions(any()) } returns listOf(recurringTransaction)
        coEvery { transactionRepository.addTransaction(any()) } returns Unit
        coEvery { transactionRepository.updateNextPaymentDate(any(), any()) } returns Unit

        // When
        useCase()

        // Then
        coVerify { transactionRepository.getDueRecurringTransactions(any()) }
        coVerify { transactionRepository.addTransaction(any()) }
        coVerify { transactionRepository.updateNextPaymentDate(recurringTransaction.id, any()) }
    }

    @Test
    fun `calculateNextPaymentDate handles NEVER frequency correctly`() {
        // Given
        val baseDate = LocalDate(2024, 1, 15).atTime(10, 0).toInstant(TimeZone.UTC)

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.NEVER)

        // Then - should return same date as fallback (though this should not be called in practice)
        val expectedDate = LocalDate(2024, 1, 15).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    @Test
    fun `calculateNextPaymentDate handles DAILY frequency correctly`() {
        // Given
        val baseDate = LocalDate(2024, 1, 15).atTime(10, 0).toInstant(TimeZone.UTC)

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.DAILY)

        // Then
        val expectedDate = LocalDate(2024, 1, 16).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    @Test
    fun `calculateNextPaymentDate handles WEEKLY frequency correctly`() {
        // Given
        val baseDate = LocalDate(2024, 1, 15).atTime(10, 0).toInstant(TimeZone.UTC)

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.WEEKLY)

        // Then
        val expectedDate = LocalDate(2024, 1, 22).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    @Test
    fun `calculateNextPaymentDate handles EVERY_OTHER_WEEK frequency correctly`() {
        // Given
        val baseDate = LocalDate(2024, 1, 15).atTime(10, 0).toInstant(TimeZone.UTC)

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.EVERY_OTHER_WEEK)

        // Then
        val expectedDate = LocalDate(2024, 1, 29).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    @Test
    fun `calculateNextPaymentDate handles EVERY_4_WEEKS frequency correctly`() {
        // Given
        val baseDate = LocalDate(2024, 1, 15).atTime(10, 0).toInstant(TimeZone.UTC)

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.EVERY_4_WEEKS)

        // Then
        val expectedDate = LocalDate(2024, 2, 12).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    @Test
    fun `calculateNextPaymentDate handles MONTHLY frequency correctly`() {
        // Given
        val baseDate = LocalDate(2024, 1, 31).atTime(10, 0).toInstant(TimeZone.UTC)

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.MONTHLY)

        // Then - February 29th in leap year 2024
        val expectedDate = LocalDate(2024, 2, 29).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    @Test
    fun `calculateNextPaymentDate handles MONTHLY frequency in non-leap year`() {
        // Given
        val baseDate = LocalDate(2023, 1, 31).atTime(10, 0).toInstant(TimeZone.UTC)

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.MONTHLY)

        // Then - February 28th in non-leap year 2023
        val expectedDate = LocalDate(2023, 2, 28).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    @Test
    fun `calculateNextPaymentDate handles TWICE_A_MONTH frequency - first half of month`() {
        // Given
        val baseDate = LocalDate(2024, 1, 5).atTime(10, 0).toInstant(TimeZone.UTC)

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.TWICE_A_MONTH)

        // Then - should go to 15th of same month
        val expectedDate = LocalDate(2024, 1, 15).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    @Test
    fun `calculateNextPaymentDate handles TWICE_A_MONTH frequency - second half of month`() {
        // Given
        val baseDate = LocalDate(2024, 1, 20).atTime(10, 0).toInstant(TimeZone.UTC)

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.TWICE_A_MONTH)

        // Then - should go to 1st of next month
        val expectedDate = LocalDate(2024, 2, 1).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    @Test
    fun `calculateNextPaymentDate handles EVERY_OTHER_MONTH frequency correctly`() {
        // Given
        val baseDate = LocalDate(2024, 1, 15).atTime(10, 0).toInstant(TimeZone.UTC)

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.EVERY_OTHER_MONTH)

        // Then
        val expectedDate = LocalDate(2024, 3, 15).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    @Test
    fun `calculateNextPaymentDate handles EVERY_3_MONTHS frequency correctly`() {
        // Given
        val baseDate = LocalDate(2024, 1, 15).atTime(10, 0).toInstant(TimeZone.UTC)

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.EVERY_3_MONTHS)

        // Then
        val expectedDate = LocalDate(2024, 4, 15).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    @Test
    fun `calculateNextPaymentDate handles EVERY_4_MONTHS frequency correctly`() {
        // Given
        val baseDate = LocalDate(2024, 1, 15).atTime(10, 0).toInstant(TimeZone.UTC)

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.EVERY_4_MONTHS)

        // Then
        val expectedDate = LocalDate(2024, 5, 15).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    @Test
    fun `calculateNextPaymentDate handles TWICE_A_YEAR frequency correctly`() {
        // Given
        val baseDate = LocalDate(2024, 1, 15).atTime(10, 0).toInstant(TimeZone.UTC)

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.TWICE_A_YEAR)

        // Then
        val expectedDate = LocalDate(2024, 7, 15).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    @Test
    fun `calculateNextPaymentDate handles YEARLY frequency correctly`() {
        // Given
        val baseDate = LocalDate(2024, 2, 29).atTime(10, 0).toInstant(TimeZone.UTC) // Leap year

        // When
        val nextDate = useCase.calculateNextPaymentDate(baseDate, RecurrenceFrequency.YEARLY)

        // Then - should go to Feb 28 in non-leap year 2025
        val expectedDate = LocalDate(2025, 2, 28).atTime(10, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedDate, nextDate)
    }

    private fun createRecurringTransaction(frequency: RecurrenceFrequency): Transaction {
        return Transaction(
            id = 1L,
            amount = Money(-1500.0),
            account = testAccount,
            category = testCategory,
            transactionPartner = "Landlord",
            transactionDirection = TransactionDirection.Outflow,
            description = "Monthly rent",
            dateOfTransaction = Clock.System.now(),
            updatedAt = Clock.System.now(),
            createdAt = Clock.System.now(),
            isRecurring = true,
            recurrenceFrequency = frequency,
            nextPaymentDate = Clock.System.now(),
            recurrenceEndDate = null,
            isRecurrenceActive = true,
            parentRecurringTransactionId = null
        )
    }

    // Make calculateNextPaymentDate accessible for testing
    fun ProcessRecurringPaymentsUseCase.calculateNextPaymentDate(
        currentDate: Instant,
        frequency: RecurrenceFrequency
    ): Instant {
        // Use reflection to access private method
        val method = this::class.java.getDeclaredMethod(
            "calculateNextPaymentDate",
            Instant::class.java,
            RecurrenceFrequency::class.java
        )
        method.isAccessible = true
        return method.invoke(this, currentDate, frequency) as Instant
    }
}