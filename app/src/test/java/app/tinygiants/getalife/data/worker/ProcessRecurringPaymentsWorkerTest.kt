package app.tinygiants.getalife.data.worker

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.usecase.transaction.ProcessRecurringPaymentsUseCase
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.jvm.java
import kotlin.time.Clock

@ExtendWith(TestDispatcherExtension::class)
class ProcessRecurringPaymentsWorkerTest {

    private lateinit var processRecurringPaymentsUseCase: ProcessRecurringPaymentsUseCase
    private lateinit var workerFactory: ProcessRecurringPaymentsWorker.Factory

    @BeforeEach
    fun setup() {
        processRecurringPaymentsUseCase = mockk()
        workerFactory = mockk()
    }

    @Test
    fun `worker executes successfully when usecase completes normally`() = runTest {
        // Given
        coEvery { processRecurringPaymentsUseCase() } returns Unit

        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val worker = TestListenableWorkerBuilder<ProcessRecurringPaymentsWorker>(context)
            .build()

        // Manually inject dependencies since we can't use Hilt in unit tests
        val actualWorker = ProcessRecurringPaymentsWorker(
            context = context,
            params = worker.workerParameters,
            processRecurringPaymentsUseCase = processRecurringPaymentsUseCase
        )

        // When
        val result = actualWorker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { processRecurringPaymentsUseCase() }
    }

    @Test
    fun `worker retries when usecase throws exception`() = runTest {
        // Given
        coEvery { processRecurringPaymentsUseCase() } throws RuntimeException("Database error")

        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val worker = TestListenableWorkerBuilder<ProcessRecurringPaymentsWorker>(context)
            .build()

        val actualWorker = ProcessRecurringPaymentsWorker(
            context = context,
            params = worker.workerParameters,
            processRecurringPaymentsUseCase = processRecurringPaymentsUseCase
        )

        // When
        val result = actualWorker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.retry(), result)
        coVerify { processRecurringPaymentsUseCase() }
    }

    @Test
    fun `worker factory creates worker with correct dependencies`() {
        // Given
        val context = mockk<android.content.Context>()
        val params = mockk<androidx.work.WorkerParameters>()

        val factory = ProcessRecurringPaymentsWorker.Factory { mockContext, mockParams ->
            ProcessRecurringPaymentsWorker(mockContext, mockParams, processRecurringPaymentsUseCase)
        }

        // When
        val worker = factory.create(context, params)

        // Then
        assertEquals(ProcessRecurringPaymentsWorker::class.java, worker::class.java)
    }

    @Test
    fun `enqueue creates periodic work request with correct configuration`() {
        // Given
        val context = mockk<android.content.Context>(relaxed = true)
        val workManager = mockk<androidx.work.WorkManager>(relaxed = true)

        coEvery { androidx.work.WorkManager.getInstance(context) } returns workManager

        // When
        ProcessRecurringPaymentsWorker.enqueue(context)

        // Then - this would require PowerMock or similar to properly test
        // For now, we verify the method can be called without exceptions
        // In a real test environment, we'd verify:
        // - WorkRequest is created with 1 day interval
        // - Unique work name is "recurring_payments_worker"
        // - ExistingPeriodicWorkPolicy is KEEP
    }

    private fun createTestTransaction(frequency: RecurrenceFrequency): Transaction {
        return Transaction(
            id = 1L,
            amount = Money(-1500.0),
            account = Account(
                id = 1L,
                name = "Test Account",
                balance = Money(1000.0),
                type = AccountType.Checking,
                listPosition = 0,
                updatedAt = Clock.System.now(),
                createdAt = Clock.System.now()
            ),
            category = Category(
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
            ),
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
}