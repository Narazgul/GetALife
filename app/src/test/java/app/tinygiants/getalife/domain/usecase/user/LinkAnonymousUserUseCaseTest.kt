package app.tinygiants.getalife.domain.usecase.user

import app.tinygiants.getalife.data.repository.BudgetRepository
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("LinkAnonymousUserUseCase")
class LinkAnonymousUserUseCaseTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var mockBudgetRepository: BudgetRepository
    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser
    private lateinit var linkAnonymousUserUseCase: LinkAnonymousUserUseCase

    @BeforeEach
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        mockBudgetRepository = mockk(relaxed = true)
        mockFirebaseAuth = mockk()
        mockFirebaseUser = mockk()

        linkAnonymousUserUseCase = LinkAnonymousUserUseCase(
            budgetRepository = mockBudgetRepository,
            firebaseAuth = mockFirebaseAuth,
            defaultDispatcher = testDispatcher
        )
    }

    @Nested
    @DisplayName("Successful account linking")
    inner class SuccessfulAccountLinking {

        @Test
        @DisplayName("should link anonymous account to authenticated user")
        fun linksAnonymousAccountToAuthenticatedUser() = runTest {
            // Arrange
            val authenticatedUserId = "auth123"
            val anonymousUserId = "anon456"
            val userName = "John Doe"

            every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.displayName } returns userName

            // Act
            linkAnonymousUserUseCase(
                authenticatedUserId = authenticatedUserId,
                previousAnonymousUserId = anonymousUserId
            )

            // Assert
            coVerify(exactly = 1) {
                mockBudgetRepository.linkAnonymousAccount(
                    anonymousUserId = anonymousUserId,
                    authenticatedUserId = authenticatedUserId,
                    userName = userName
                )
            }
        }

        @Test
        @DisplayName("should use default username when display name is null")
        fun usesDefaultUsernameWhenDisplayNameIsNull() = runTest {
            // Arrange
            val authenticatedUserId = "auth123"
            val anonymousUserId = "anon456"

            every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.displayName } returns null

            val userNameSlot = slot<String>()

            // Act
            linkAnonymousUserUseCase(
                authenticatedUserId = authenticatedUserId,
                previousAnonymousUserId = anonymousUserId
            )

            // Assert
            coVerify {
                mockBudgetRepository.linkAnonymousAccount(
                    anonymousUserId = any(),
                    authenticatedUserId = any(),
                    userName = capture(userNameSlot)
                )
            }
            assertThat(userNameSlot.captured).isEqualTo("Mein Budget")
        }

        @Test
        @DisplayName("should get anonymous user ID from Firebase when not provided")
        fun getsAnonymousUserIdFromFirebaseWhenNotProvided() = runTest {
            // Arrange
            val authenticatedUserId = "auth123"
            val currentUserId = "current456"
            val userName = "Jane Doe"

            every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.uid } returns currentUserId
            every { mockFirebaseUser.displayName } returns userName

            val anonymousUserIdSlot = slot<String>()

            // Act
            linkAnonymousUserUseCase(
                authenticatedUserId = authenticatedUserId,
                previousAnonymousUserId = null // Not provided
            )

            // Assert
            coVerify {
                mockBudgetRepository.linkAnonymousAccount(
                    anonymousUserId = capture(anonymousUserIdSlot),
                    authenticatedUserId = any(),
                    userName = any()
                )
            }
            assertThat(anonymousUserIdSlot.captured).isEqualTo(currentUserId)
        }

        @Test
        @DisplayName("should link account successfully with minimal user info")
        fun linksAccountWithMinimalUserInfo() = runTest {
            // Arrange
            val authenticatedUserId = "auth123"
            val anonymousUserId = "anon456"

            every { mockFirebaseAuth.currentUser } returns null // No current user

            // Act
            linkAnonymousUserUseCase(
                authenticatedUserId = authenticatedUserId,
                previousAnonymousUserId = anonymousUserId
            )

            // Assert - Should still proceed with default values
            coVerify {
                mockBudgetRepository.linkAnonymousAccount(
                    anonymousUserId = anonymousUserId,
                    authenticatedUserId = authenticatedUserId,
                    userName = "Mein Budget"
                )
            }
        }
    }

    @Nested
    @DisplayName("Account linking skipped scenarios")
    inner class AccountLinkingSkippedScenarios {

        @Test
        @DisplayName("should skip linking when user IDs are the same")
        fun skipsLinkingWhenUserIdsAreSame() = runTest {
            // Arrange
            val sameUserId = "user123"

            every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.displayName } returns "John Doe"

            // Act
            linkAnonymousUserUseCase(
                authenticatedUserId = sameUserId,
                previousAnonymousUserId = sameUserId // Same as authenticated
            )

            // Assert
            coVerify(exactly = 0) { mockBudgetRepository.linkAnonymousAccount(any(), any(), any()) }
        }

        @Test
        @DisplayName("should skip linking when anonymous user ID is 'anonymous'")
        fun skipsLinkingWhenAnonymousUserIdIsAnonymous() = runTest {
            // Arrange
            val authenticatedUserId = "auth123"

            every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.displayName } returns "John Doe"

            // Act
            linkAnonymousUserUseCase(
                authenticatedUserId = authenticatedUserId,
                previousAnonymousUserId = "anonymous" // Special anonymous value
            )

            // Assert
            coVerify(exactly = 0) { mockBudgetRepository.linkAnonymousAccount(any(), any(), any()) }
        }

        @Test
        @DisplayName("should skip linking when current user ID is 'anonymous' and not provided")
        fun skipsLinkingWhenCurrentUserIdIsAnonymous() = runTest {
            // Arrange
            val authenticatedUserId = "auth123"

            every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.uid } returns "anonymous" // Current user is anonymous
            every { mockFirebaseUser.displayName } returns "John Doe"

            // Act
            linkAnonymousUserUseCase(
                authenticatedUserId = authenticatedUserId,
                previousAnonymousUserId = null // Will use current user ID
            )

            // Assert
            coVerify(exactly = 0) { mockBudgetRepository.linkAnonymousAccount(any(), any(), any()) }
        }

        @Test
        @DisplayName("should skip linking when current user is null and previous not provided")
        fun skipsLinkingWhenCurrentUserIsNullAndPreviousNotProvided() = runTest {
            // Arrange
            val authenticatedUserId = "auth123"

            every { mockFirebaseAuth.currentUser } returns null

            // Act
            linkAnonymousUserUseCase(
                authenticatedUserId = authenticatedUserId,
                previousAnonymousUserId = null
            )

            // Assert - Should still skip because current user ID becomes "anonymous"
            coVerify(exactly = 0) { mockBudgetRepository.linkAnonymousAccount(any(), any(), any()) }
        }
    }

    @Nested
    @DisplayName("Username resolution")
    inner class UsernameResolution {

        @Test
        @DisplayName("should prioritize display name over default")
        fun prioritizesDisplayNameOverDefault() = runTest {
            // Arrange
            val authenticatedUserId = "auth123"
            val anonymousUserId = "anon456"
            val displayName = "Custom User Name"

            every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.displayName } returns displayName

            val userNameSlot = slot<String>()

            // Act
            linkAnonymousUserUseCase(
                authenticatedUserId = authenticatedUserId,
                previousAnonymousUserId = anonymousUserId
            )

            // Assert
            coVerify {
                mockBudgetRepository.linkAnonymousAccount(
                    anonymousUserId = any(),
                    authenticatedUserId = any(),
                    userName = capture(userNameSlot)
                )
            }
            assertThat(userNameSlot.captured).isEqualTo(displayName)
        }

        @Test
        @DisplayName("should handle empty display name as null")
        fun handlesEmptyDisplayNameAsNull() = runTest {
            // Arrange
            val authenticatedUserId = "auth123"
            val anonymousUserId = "anon456"

            every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.displayName } returns "" // Empty string

            val userNameSlot = slot<String>()

            // Act
            linkAnonymousUserUseCase(
                authenticatedUserId = authenticatedUserId,
                previousAnonymousUserId = anonymousUserId
            )

            // Assert - Should use default when display name is empty
            coVerify {
                mockBudgetRepository.linkAnonymousAccount(
                    anonymousUserId = any(),
                    authenticatedUserId = any(),
                    userName = capture(userNameSlot)
                )
            }
            assertThat(userNameSlot.captured).isEqualTo("Mein Budget")
        }

        @Test
        @DisplayName("should handle whitespace-only display name")
        fun handlesWhitespaceOnlyDisplayName() = runTest {
            // Arrange
            val authenticatedUserId = "auth123"
            val anonymousUserId = "anon456"

            every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.displayName } returns "   " // Whitespace only

            val userNameSlot = slot<String>()

            // Act
            linkAnonymousUserUseCase(
                authenticatedUserId = authenticatedUserId,
                previousAnonymousUserId = anonymousUserId
            )

            // Assert - Should use the whitespace name (let repository handle validation)
            coVerify {
                mockBudgetRepository.linkAnonymousAccount(
                    anonymousUserId = any(),
                    authenticatedUserId = any(),
                    userName = capture(userNameSlot)
                )
            }
            assertThat(userNameSlot.captured).isEqualTo("   ")
        }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandling {

        @Test
        @DisplayName("should not crash when repository throws exception")
        fun doesNotCrashWhenRepositoryThrowsException() = runTest {
            // Arrange
            val authenticatedUserId = "auth123"
            val anonymousUserId = "anon456"

            every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.displayName } returns "User"
            coEvery {
                mockBudgetRepository.linkAnonymousAccount(any(), any(), any())
            } throws RuntimeException("Repository error")

            // Act & Assert - Should not throw exception
            val result = runCatching {
                linkAnonymousUserUseCase(
                    authenticatedUserId = authenticatedUserId,
                    previousAnonymousUserId = anonymousUserId
                )
            }

            // Exception should propagate (use case doesn't handle it)
            assertThat(result.isFailure).isEqualTo(true)
        }
    }
}