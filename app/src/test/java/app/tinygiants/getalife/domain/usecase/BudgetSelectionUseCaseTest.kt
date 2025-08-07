package app.tinygiants.getalife.domain.usecase

import android.content.Context
import app.tinygiants.getalife.data.local.entities.BudgetEntity
import app.tinygiants.getalife.data.repository.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class BudgetSelectionUseCaseTest {

    private lateinit var context: Context
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var useCase: BudgetSelectionUseCase

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        budgetRepository = mockk()
        firebaseAuth = mockk()
        firebaseUser = mockk()

        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "test-user-id"
        every { firebaseUser.displayName } returns "Test User"
        every { firebaseUser.isAnonymous } returns false

        useCase = BudgetSelectionUseCase(context, budgetRepository, firebaseAuth)
    }

    @Test
    fun `getBudgetsFlow returns budgets for current user`() = runTest {
        // Given
        val budgets = listOf(
            BudgetEntity(
                id = "budget-1",
                name = "Test Budget",
                firebaseUserId = "test-user-id",
                createdAt = Instant.fromEpochMilliseconds(0),
                lastModifiedAt = Instant.fromEpochMilliseconds(0),
                isSynced = true
            )
        )
        every { budgetRepository.getBudgetsFlow("test-user-id") } returns flowOf(budgets)

        // When
        val result = useCase.getBudgetsFlow().first()

        // Then
        assertEquals(budgets, result)
    }

    @Test
    fun `createBudget creates budget with current user ID`() = runTest {
        // Given
        val budgetName = "New Budget"
        val expectedBudget = BudgetEntity(
            id = "new-budget-id",
            name = budgetName,
            firebaseUserId = "test-user-id",
            createdAt = Instant.fromEpochMilliseconds(0),
            lastModifiedAt = Instant.fromEpochMilliseconds(0),
            isSynced = false
        )
        coEvery { budgetRepository.createBudget(budgetName, "test-user-id") } returns expectedBudget

        // When
        val result = useCase.createBudget(budgetName)

        // Then
        assertEquals(expectedBudget, result)
        coVerify { budgetRepository.createBudget(budgetName, "test-user-id") }
    }

    @Test
    fun `initializeDefaultBudget creates budget when none exist`() = runTest {
        // Given
        val userName = "Test User"
        coEvery { budgetRepository.getBudgets("test-user-id") } returns emptyList()
        val expectedBudget = BudgetEntity(
            id = "default-budget",
            name = userName,
            firebaseUserId = "test-user-id",
            createdAt = Instant.fromEpochMilliseconds(0),
            lastModifiedAt = Instant.fromEpochMilliseconds(0),
            isSynced = false
        )
        coEvery { budgetRepository.createBudget(userName, "test-user-id") } returns expectedBudget

        // When
        val result = useCase.initializeDefaultBudget()

        // Then
        assertEquals(expectedBudget, result)
        coVerify { budgetRepository.createBudget(userName, "test-user-id") }
    }

    @Test
    fun `initializeDefaultBudget uses existing budget when available`() = runTest {
        // Given
        val existingBudget = BudgetEntity(
            id = "existing-budget",
            name = "Existing Budget",
            firebaseUserId = "test-user-id",
            createdAt = Instant.fromEpochMilliseconds(0),
            lastModifiedAt = Instant.fromEpochMilliseconds(0),
            isSynced = true
        )
        coEvery { budgetRepository.getBudgets("test-user-id") } returns listOf(existingBudget)

        // When
        val result = useCase.initializeDefaultBudget()

        // Then
        assertEquals(existingBudget, result)
        coVerify(exactly = 0) { budgetRepository.createBudget(any(), any()) }
    }

    @Test
    fun `handleAccountLinking calls repository with correct parameters`() = runTest {
        // Given
        val authenticatedUserId = "auth-user-id"
        val anonymousUserId = "test-user-id"
        val userName = "Test User"

        coEvery {
            budgetRepository.linkAnonymousAccount(anonymousUserId, authenticatedUserId, userName)
        } returns Unit

        val existingBudget = BudgetEntity(
            id = "linked-budget",
            name = "Linked Budget",
            firebaseUserId = authenticatedUserId,
            createdAt = Instant.fromEpochMilliseconds(0),
            lastModifiedAt = Instant.fromEpochMilliseconds(0),
            isSynced = false
        )

        // Mock for initializeDefaultBudget after linking
        every { firebaseAuth.currentUser?.uid } returns authenticatedUserId
        coEvery { budgetRepository.getBudgets(authenticatedUserId) } returns listOf(existingBudget)

        // When
        useCase.handleAccountLinking(authenticatedUserId)

        // Then
        coVerify { budgetRepository.linkAnonymousAccount(anonymousUserId, authenticatedUserId, userName) }
    }

    @Test
    fun `switchToBudget switches when budget exists`() = runTest {
        // Given
        val budgetId = "target-budget"
        val targetBudget = BudgetEntity(
            id = budgetId,
            name = "Target Budget",
            firebaseUserId = "test-user-id",
            createdAt = Instant.fromEpochMilliseconds(0),
            lastModifiedAt = Instant.fromEpochMilliseconds(0),
            isSynced = true
        )
        coEvery { budgetRepository.getBudget(budgetId) } returns targetBudget

        // When
        useCase.switchToBudget(budgetId)

        // Then
        coVerify { budgetRepository.getBudget(budgetId) }
    }

    @Test
    fun `isAnonymousUser returns correct status`() {
        // Given
        every { firebaseUser.isAnonymous } returns true

        // When & Then
        assertTrue(useCase.isAnonymousUser())

        // Given
        every { firebaseUser.isAnonymous } returns false

        // When & Then
        assertFalse(useCase.isAnonymousUser())
    }

    @Test
    fun `getCurrentUserDisplayName returns display name when available`() {
        // When & Then
        assertEquals("Test User", useCase.getCurrentUserDisplayName())
    }

    @Test
    fun `getCurrentUserDisplayName returns email when display name unavailable`() {
        // Given
        every { firebaseUser.displayName } returns null
        every { firebaseUser.email } returns "test@example.com"

        // When & Then
        assertEquals("test@example.com", useCase.getCurrentUserDisplayName())
    }

    @Test
    fun `getCurrentUserDisplayName returns fallback when both unavailable`() {
        // Given
        every { firebaseUser.displayName } returns null
        every { firebaseUser.email } returns null

        // When & Then
        assertEquals("Unbekannter Nutzer", useCase.getCurrentUserDisplayName())
    }
}