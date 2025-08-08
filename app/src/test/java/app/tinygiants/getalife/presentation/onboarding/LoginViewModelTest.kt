package app.tinygiants.getalife.presentation.onboarding

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @MockK
    private lateinit var firebaseAuth: FirebaseAuth

    @MockK
    private lateinit var crashlytics: FirebaseCrashlytics

    @MockK
    private lateinit var authResult: AuthResult

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = LoginViewModel(firebaseAuth, crashlytics)
    }

    @Test
    fun `initial state is correct`() = testScope.runTest {
        val uiState = viewModel.uiState.first()

        assertThat(uiState.email).isEqualTo("")
        assertThat(uiState.password).isEqualTo("")
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.loginSuccess).isFalse()
        assertThat(uiState.emailError).isNull()
        assertThat(uiState.passwordError).isNull()
        assertThat(uiState.errorMessage).isNull()
    }

    @Test
    fun `onEmailChanged updates email and clears error`() = testScope.runTest {
        // Given
        val email = "test@example.com"

        // When
        viewModel.onEmailChanged(email)

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.email).isEqualTo(email)
        assertThat(uiState.emailError).isNull()
    }

    @Test
    fun `onPasswordChanged updates password and clears error`() = testScope.runTest {
        // Given
        val password = "password123"

        // When
        viewModel.onPasswordChanged(password)

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.password).isEqualTo(password)
        assertThat(uiState.passwordError).isNull()
    }

    @Test
    fun `signInWithEmail with invalid email shows error`() = testScope.runTest {
        // Given
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("password123")

        // When
        viewModel.signInWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.emailError).isEqualTo("Ungültige E-Mail-Adresse")
        assertThat(uiState.isLoading).isFalse()
    }

    @Test
    fun `signInWithEmail with empty password shows error`() = testScope.runTest {
        // Given
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("")

        // When
        viewModel.signInWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.passwordError).isEqualTo("Passwort ist erforderlich")
        assertThat(uiState.isLoading).isFalse()
    }

    @Test
    fun `signInWithEmail success flow`() = testScope.runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"

        every { firebaseAuth.signInWithEmailAndPassword(email, password) } returns Tasks.forResult(authResult)

        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)

        // When
        viewModel.signInWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.loginSuccess).isTrue()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.errorMessage).isNull()

        verify { firebaseAuth.signInWithEmailAndPassword(email, password) }
    }

    @Test
    fun `signInWithEmail with invalid credentials shows error`() = testScope.runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val exception = FirebaseAuthInvalidCredentialsException("invalid-credential", "Invalid credentials")

        every { firebaseAuth.signInWithEmailAndPassword(email, password) } returns Tasks.forException(exception)

        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)

        // When
        viewModel.signInWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.loginSuccess).isFalse()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.errorMessage).isEqualTo("Ungültige E-Mail oder Passwort")

        verify { crashlytics.recordException(exception) }
    }

    @Test
    fun `signInWithEmail with invalid user shows error`() = testScope.runTest {
        // Given
        val email = "nonexistent@example.com"
        val password = "password123"
        val exception = FirebaseAuthInvalidUserException("user-not-found", "User not found")

        every { firebaseAuth.signInWithEmailAndPassword(email, password) } returns Tasks.forException(exception)

        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)

        // When
        viewModel.signInWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.loginSuccess).isFalse()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.errorMessage).isEqualTo("Kein Benutzer mit dieser E-Mail gefunden")

        verify { crashlytics.recordException(exception) }
    }

    @Test
    fun `clearError clears error message`() = testScope.runTest {
        // Given - force an error state first
        viewModel.onEmailChanged("invalid-email")
        viewModel.signInWithEmail()

        // When
        viewModel.clearError()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.errorMessage).isNull()
    }

    @Test
    fun `email validation works correctly`() = testScope.runTest {
        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org"
        )

        val invalidEmails = listOf(
            "",
            "invalid-email",
            "@example.com",
            "user@",
            "user@@example.com"
        )

        validEmails.forEach { email ->
            viewModel.onEmailChanged(email)
            viewModel.onPasswordChanged("password123")
            viewModel.signInWithEmail()

            val uiState = viewModel.uiState.first()
            assertThat(uiState.emailError).isNull()
        }

        invalidEmails.forEach { email ->
            viewModel.onEmailChanged(email)
            viewModel.onPasswordChanged("password123")
            viewModel.signInWithEmail()

            val uiState = viewModel.uiState.first()
            assertThat(uiState.emailError).isNotNull()
        }
    }

    @Test
    fun `password validation works correctly`() = testScope.runTest {
        val validPasswords = listOf(
            "password123",
            "123456",
            "strongPassword!"
        )

        val invalidPasswords = listOf(
            "",
            "12345" // too short
        )

        validPasswords.forEach { password ->
            viewModel.onEmailChanged("test@example.com")
            viewModel.onPasswordChanged(password)
            viewModel.signInWithEmail()

            val uiState = viewModel.uiState.first()
            assertThat(uiState.passwordError).isNull()
        }

        invalidPasswords.forEach { password ->
            viewModel.onEmailChanged("test@example.com")
            viewModel.onPasswordChanged(password)
            viewModel.signInWithEmail()

            val uiState = viewModel.uiState.first()
            assertThat(uiState.passwordError).isNotNull()
        }
    }
}