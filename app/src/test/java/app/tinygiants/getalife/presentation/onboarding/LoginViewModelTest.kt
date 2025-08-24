package app.tinygiants.getalife.presentation.onboarding

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.tinygiants.getalife.presentation.onboarding.auth.LoginViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val authResult: AuthResult = mockk(relaxed = true)

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(firebaseAuth, crashlytics)
    }

    @Test
    fun `initial state is correct`() = runTest {
        val uiState = viewModel.uiState.first()

        assertEquals("", uiState.email)
        assertEquals("", uiState.password)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.loginSuccess)
        assertNull(uiState.emailError)
        assertNull(uiState.passwordError)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `onEmailChanged updates email and clears error`() = runTest {
        // Given
        val email = "test@example.com"

        // When
        viewModel.onEmailChanged(email)

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals(email, uiState.email)
        assertNull(uiState.emailError)
    }

    @Test
    fun `onPasswordChanged updates password and clears error`() = runTest {
        // Given
        val password = "password123"

        // When
        viewModel.onPasswordChanged(password)

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals(password, uiState.password)
        assertNull(uiState.passwordError)
    }

    @Test
    fun `signInWithEmail with empty email shows error`() = runTest {
        // Given
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("password123")

        // When
        viewModel.signInWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("E-Mail ist erforderlich", uiState.emailError)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.loginSuccess)
    }

    @Test
    fun `signInWithEmail with invalid email shows error`() = runTest {
        // Given
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("password123")

        // When
        viewModel.signInWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("Ungültige E-Mail-Adresse", uiState.emailError)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.loginSuccess)
    }

    @Test
    fun `signInWithEmail with empty password shows error`() = runTest {
        // Given
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("")

        // When
        viewModel.signInWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("Passwort ist erforderlich", uiState.passwordError)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.loginSuccess)
    }

    @Test
    fun `signInWithEmail with short password shows error`() = runTest {
        // Given
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("12345")

        // When
        viewModel.signInWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("Passwort muss mindestens 6 Zeichen haben", uiState.passwordError)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.loginSuccess)
    }

    @Test
    fun `signInWithEmail success flow`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val successTask: Task<AuthResult> = Tasks.forResult(authResult)

        every {
            firebaseAuth.signInWithEmailAndPassword(email, password)
        } returns successTask

        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)

        // When
        viewModel.signInWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.loginSuccess)
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)

        verify { firebaseAuth.signInWithEmailAndPassword(email, password) }
    }

    @Test
    fun `signInWithEmail with invalid credentials shows error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val exception = FirebaseAuthInvalidCredentialsException("invalid-credential", "Invalid credentials")
        val failureTask: Task<AuthResult> = Tasks.forException(exception)

        every {
            firebaseAuth.signInWithEmailAndPassword(email, password)
        } returns failureTask

        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)

        // When
        viewModel.signInWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.loginSuccess)
        assertFalse(uiState.isLoading)
        assertEquals("Ungültige E-Mail oder Passwort", uiState.errorMessage)

        verify { crashlytics.recordException(exception) }
    }

    @Test
    fun `signInWithEmail with invalid user shows error`() = runTest {
        // Given
        val email = "nonexistent@example.com"
        val password = "password123"
        val exception = FirebaseAuthInvalidUserException("user-not-found", "User not found")
        val failureTask: Task<AuthResult> = Tasks.forException(exception)

        every {
            firebaseAuth.signInWithEmailAndPassword(email, password)
        } returns failureTask

        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)

        // When
        viewModel.signInWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.loginSuccess)
        assertFalse(uiState.isLoading)
        assertEquals("Kein Benutzer mit dieser E-Mail gefunden", uiState.errorMessage)

        verify { crashlytics.recordException(exception) }
    }

    @Test
    fun `signInWithEmail with weak password shows error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "weak"
        val exception = FirebaseAuthWeakPasswordException("weak-password", "Password too weak", "WEAK_PASSWORD")
        val failureTask: Task<AuthResult> = Tasks.forException(exception)

        every {
            firebaseAuth.signInWithEmailAndPassword(email, password)
        } returns failureTask

        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)

        // When
        viewModel.signInWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.loginSuccess)
        assertFalse(uiState.isLoading)
        assertEquals("Passwort ist zu schwach", uiState.errorMessage)

        verify { crashlytics.recordException(exception) }
    }

    @Test
    fun `signInWithGoogle success flow`() = runTest {
        // Given
        val idToken = "google-id-token"
        val successTask: Task<AuthResult> = Tasks.forResult(authResult)

        every {
            firebaseAuth.signInWithCredential(any())
        } returns successTask

        // When
        viewModel.signInWithGoogle(idToken)
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.loginSuccess)
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)

        verify { firebaseAuth.signInWithCredential(any()) }
    }

    @Test
    fun `signInWithFacebook success flow`() = runTest {
        // Given
        val accessToken = "facebook-access-token"
        val successTask: Task<AuthResult> = Tasks.forResult(authResult)

        every {
            firebaseAuth.signInWithCredential(any())
        } returns successTask

        // When
        viewModel.signInWithFacebook(accessToken)
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.loginSuccess)
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)

        verify { firebaseAuth.signInWithCredential(any()) }
    }

    @Test
    fun `signInWithTwitter success flow`() = runTest {
        // Given
        val token = "twitter-token"
        val secret = "twitter-secret"
        val successTask: Task<AuthResult> = Tasks.forResult(authResult)

        every {
            firebaseAuth.signInWithCredential(any())
        } returns successTask

        // When
        viewModel.signInWithTwitter(token, secret)
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.loginSuccess)
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)

        verify { firebaseAuth.signInWithCredential(any()) }
    }

    @Test
    fun `clearError clears error message`() = runTest {
        // Given - force an error state first
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("password123")
        viewModel.signInWithEmail()

        // When
        viewModel.clearError()

        // Then
        val uiState = viewModel.uiState.first()
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `valid email addresses pass validation`() = runTest {
        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org",
            "123@numbers.com"
        )

        validEmails.forEach { email ->
            viewModel.onEmailChanged(email)
            viewModel.onPasswordChanged("password123")

            // Set up success mock for each call
            every {
                firebaseAuth.signInWithEmailAndPassword(email, "password123")
            } returns Tasks.forResult(authResult)

            viewModel.signInWithEmail()

            val uiState = viewModel.uiState.first()
            assertNull("Email $email should be valid", uiState.emailError)
        }
    }

    @Test
    fun `invalid email addresses fail validation`() = runTest {
        val invalidEmails = listOf(
            "invalid-email",
            "@example.com",
            "user@",
            "user@@example.com",
            "user..name@example.com"
        )

        invalidEmails.forEach { email ->
            viewModel.onEmailChanged(email)
            viewModel.onPasswordChanged("password123")
            viewModel.signInWithEmail()

            val uiState = viewModel.uiState.first()
            assertEquals("Ungültige E-Mail-Adresse", uiState.emailError)
        }
    }

    @Test
    fun `valid passwords pass validation`() = runTest {
        val validPasswords = listOf(
            "password123",
            "123456",
            "strongPassword!",
            "abcdef" // exactly 6 characters
        )

        validPasswords.forEach { password ->
            viewModel.onEmailChanged("test@example.com")
            viewModel.onPasswordChanged(password)

            // Set up success mock for each call
            every {
                firebaseAuth.signInWithEmailAndPassword("test@example.com", password)
            } returns Tasks.forResult(authResult)

            viewModel.signInWithEmail()

            val uiState = viewModel.uiState.first()
            assertNull("Password $password should be valid", uiState.passwordError)
        }
    }

    @Test
    fun `short passwords fail validation`() = runTest {
        val shortPasswords = listOf(
            "12345", // 5 characters
            "abc",   // 3 characters
            ""       // empty
        )

        shortPasswords.forEach { password ->
            viewModel.onEmailChanged("test@example.com")
            viewModel.onPasswordChanged(password)
            viewModel.signInWithEmail()

            val uiState = viewModel.uiState.first()
            when {
                password.isEmpty() -> assertEquals("Passwort ist erforderlich", uiState.passwordError)
                else -> assertEquals("Passwort muss mindestens 6 Zeichen haben", uiState.passwordError)
            }
        }
    }
}