package app.tinygiants.getalife.presentation.onboarding.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.tinygiants.getalife.presentation.onboarding.auth.SignUpViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
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
class SignUpViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val authResult: AuthResult = mockk(relaxed = true)
    private val firebaseUser: FirebaseUser = mockk(relaxed = true)

    private lateinit var viewModel: SignUpViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SignUpViewModel(firebaseAuth, crashlytics)
    }

    @Test
    fun `initial state is correct`() = runTest {
        val uiState = viewModel.uiState.first()

        assertEquals("", uiState.name)
        assertEquals("", uiState.email)
        assertEquals("", uiState.password)
        assertEquals("", uiState.confirmPassword)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.signUpSuccess)
        assertNull(uiState.nameError)
        assertNull(uiState.emailError)
        assertNull(uiState.passwordError)
        assertNull(uiState.confirmPasswordError)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `onNameChanged updates name and clears error`() = runTest {
        // Given
        val name = "John Doe"

        // When
        viewModel.onNameChanged(name)

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals(name, uiState.name)
        assertNull(uiState.nameError)
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
    fun `onPasswordChanged updates password and validates confirm password`() = runTest {
        // Given
        val password = "password123"
        val confirmPassword = "differentPassword"

        viewModel.onConfirmPasswordChanged(confirmPassword)

        // When
        viewModel.onPasswordChanged(password)

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals(password, uiState.password)
        assertNull(uiState.passwordError)
        assertEquals("Passwörter stimmen nicht überein", uiState.confirmPasswordError)
    }

    @Test
    fun `onConfirmPasswordChanged validates password match`() = runTest {
        // Given
        val password = "password123"
        val confirmPassword = "password123"

        viewModel.onPasswordChanged(password)

        // When
        viewModel.onConfirmPasswordChanged(confirmPassword)

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals(confirmPassword, uiState.confirmPassword)
        assertNull(uiState.confirmPasswordError)
    }

    @Test
    fun `signUpWithEmail with empty name shows error`() = runTest {
        // Given
        viewModel.onNameChanged("")
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")

        // When
        viewModel.signUpWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("Name ist erforderlich", uiState.nameError)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.signUpSuccess)
    }

    @Test
    fun `signUpWithEmail with short name shows error`() = runTest {
        // Given
        viewModel.onNameChanged("A") // too short
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")

        // When
        viewModel.signUpWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("Name muss mindestens 2 Zeichen haben", uiState.nameError)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.signUpSuccess)
    }

    @Test
    fun `signUpWithEmail with invalid email shows error`() = runTest {
        // Given
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")

        // When
        viewModel.signUpWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("Ungültige E-Mail-Adresse", uiState.emailError)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.signUpSuccess)
    }

    @Test
    fun `signUpWithEmail with password without digit shows error`() = runTest {
        // Given
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("nodigitpassword") // no digit
        viewModel.onConfirmPasswordChanged("nodigitpassword")

        // When
        viewModel.signUpWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("Passwort muss mindestens eine Zahl enthalten", uiState.passwordError)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.signUpSuccess)
    }

    @Test
    fun `signUpWithEmail with short password shows error`() = runTest {
        // Given
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("abc12") // too short
        viewModel.onConfirmPasswordChanged("abc12")

        // When
        viewModel.signUpWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("Passwort muss mindestens 6 Zeichen haben", uiState.passwordError)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.signUpSuccess)
    }

    @Test
    fun `signUpWithEmail with mismatched passwords shows error`() = runTest {
        // Given
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("differentPassword")

        // When
        viewModel.signUpWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("Passwörter stimmen nicht überein", uiState.confirmPasswordError)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.signUpSuccess)
    }

    @Test
    fun `signUpWithEmail with empty confirm password shows error`() = runTest {
        // Given
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("")

        // When
        viewModel.signUpWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("Passwort bestätigen ist erforderlich", uiState.confirmPasswordError)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.signUpSuccess)
    }

    @Test
    fun `signUpWithEmail success flow`() = runTest {
        // Given
        val name = "John Doe"
        val email = "test@example.com"
        val password = "password123"
        val successTask: Task<AuthResult> = Tasks.forResult(authResult)
        val updateProfileTask: Task<Void> = Tasks.forResult(null)

        every { authResult.user } returns firebaseUser
        every {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
        } returns successTask
        every {
            firebaseUser.updateProfile(any<UserProfileChangeRequest>())
        } returns updateProfileTask

        viewModel.onNameChanged(name)
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)

        // When
        viewModel.signUpWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.signUpSuccess)
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)

        verify { firebaseAuth.createUserWithEmailAndPassword(email, password) }
        verify { firebaseUser.updateProfile(any<UserProfileChangeRequest>()) }
    }

    @Test
    fun `signUpWithEmail with existing user shows error`() = runTest {
        // Given
        val name = "John Doe"
        val email = "existing@example.com"
        val password = "password123"
        val exception = FirebaseAuthUserCollisionException("email-already-in-use", "Email already in use")
        val failureTask: Task<AuthResult> = Tasks.forException(exception)

        every {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
        } returns failureTask

        viewModel.onNameChanged(name)
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)

        // When
        viewModel.signUpWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.signUpSuccess)
        assertFalse(uiState.isLoading)
        assertEquals("Ein Benutzer mit dieser E-Mail existiert bereits", uiState.errorMessage)

        verify { crashlytics.recordException(exception) }
    }

    @Test
    fun `signUpWithEmail with weak password exception shows error`() = runTest {
        // Given
        val name = "John Doe"
        val email = "test@example.com"
        val password = "weak1"
        val exception = FirebaseAuthWeakPasswordException("weak-password", "Password is too weak", "PASSWORD_TOO_WEAK")
        val failureTask: Task<AuthResult> = Tasks.forException(exception)

        every {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
        } returns failureTask

        viewModel.onNameChanged(name)
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)

        // When
        viewModel.signUpWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.signUpSuccess)
        assertFalse(uiState.isLoading)
        assertEquals("Passwort ist zu schwach", uiState.errorMessage)

        verify { crashlytics.recordException(exception) }
    }

    @Test
    fun `signUpWithEmail with invalid credentials exception shows error`() = runTest {
        // Given
        val name = "John Doe"
        val email = "test@example.com"
        val password = "password123"
        val exception = FirebaseAuthInvalidCredentialsException("invalid-email", "Invalid email format")
        val failureTask: Task<AuthResult> = Tasks.forException(exception)

        every {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
        } returns failureTask

        viewModel.onNameChanged(name)
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)

        // When
        viewModel.signUpWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.signUpSuccess)
        assertFalse(uiState.isLoading)
        assertEquals("Ungültige E-Mail-Adresse", uiState.errorMessage)

        verify { crashlytics.recordException(exception) }
    }

    @Test
    fun `signUpWithGoogle success flow`() = runTest {
        // Given
        val idToken = "google-id-token"
        val successTask: Task<AuthResult> = Tasks.forResult(authResult)

        every {
            firebaseAuth.signInWithCredential(any())
        } returns successTask

        // When
        viewModel.signUpWithGoogle(idToken)
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.signUpSuccess)
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)

        verify { firebaseAuth.signInWithCredential(any()) }
    }

    @Test
    fun `signUpWithFacebook success flow`() = runTest {
        // Given
        val accessToken = "facebook-access-token"
        val successTask: Task<AuthResult> = Tasks.forResult(authResult)

        every {
            firebaseAuth.signInWithCredential(any())
        } returns successTask

        // When
        viewModel.signUpWithFacebook(accessToken)
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.signUpSuccess)
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)

        verify { firebaseAuth.signInWithCredential(any()) }
    }

    @Test
    fun `signUpWithTwitter success flow`() = runTest {
        // Given
        val token = "twitter-token"
        val secret = "twitter-secret"
        val successTask: Task<AuthResult> = Tasks.forResult(authResult)

        every {
            firebaseAuth.signInWithCredential(any())
        } returns successTask

        // When
        viewModel.signUpWithTwitter(token, secret)
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState.signUpSuccess)
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)

        verify { firebaseAuth.signInWithCredential(any()) }
    }

    @Test
    fun `clearError clears error message`() = runTest {
        // Given - force an error state first
        viewModel.onNameChanged("")
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        viewModel.signUpWithEmail()

        // When
        viewModel.clearError()

        // Then
        val uiState = viewModel.uiState.first()
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `valid names pass validation`() = runTest {
        val validNames = listOf(
            "John Doe",
            "María José",
            "Al", // exactly 2 characters
            "A B", // minimum valid
            "Jean-Pierre"
        )

        validNames.forEach { name ->
            viewModel.onNameChanged(name)
            viewModel.onEmailChanged("test@example.com")
            viewModel.onPasswordChanged("password123")
            viewModel.onConfirmPasswordChanged("password123")

            // Set up success mock for each call
            every {
                firebaseAuth.createUserWithEmailAndPassword("test@example.com", "password123")
            } returns Tasks.forResult(authResult)
            every { authResult.user } returns firebaseUser
            every { firebaseUser.updateProfile(any<UserProfileChangeRequest>()) } returns Tasks.forResult(null)

            viewModel.signUpWithEmail()

            val uiState = viewModel.uiState.first()
            assertNull("Name '$name' should be valid", uiState.nameError)
        }
    }

    @Test
    fun `invalid names fail validation`() = runTest {
        val invalidNames = listOf(
            "",
            "A" // too short
        )

        invalidNames.forEach { name ->
            viewModel.onNameChanged(name)
            viewModel.onEmailChanged("test@example.com")
            viewModel.onPasswordChanged("password123")
            viewModel.onConfirmPasswordChanged("password123")
            viewModel.signUpWithEmail()

            val uiState = viewModel.uiState.first()
            when {
                name.isEmpty() -> assertEquals("Name ist erforderlich", uiState.nameError)
                else -> assertEquals("Name muss mindestens 2 Zeichen haben", uiState.nameError)
            }
        }
    }

    @Test
    fun `password must contain digit validation`() = runTest {
        val passwordsWithDigit = listOf(
            "password1",
            "123456",
            "strongPassword2",
            "abc123"
        )

        val passwordsWithoutDigit = listOf(
            "password",
            "onlyletters",
            "NoDigitHere",
            "abcdef"
        )

        // Test passwords with digits (should pass)
        passwordsWithDigit.forEach { password ->
            viewModel.onNameChanged("John Doe")
            viewModel.onEmailChanged("test@example.com")
            viewModel.onPasswordChanged(password)
            viewModel.onConfirmPasswordChanged(password)

            // Set up success mock for each call
            every {
                firebaseAuth.createUserWithEmailAndPassword("test@example.com", password)
            } returns Tasks.forResult(authResult)
            every { authResult.user } returns firebaseUser
            every { firebaseUser.updateProfile(any<UserProfileChangeRequest>()) } returns Tasks.forResult(null)

            viewModel.signUpWithEmail()

            val uiState = viewModel.uiState.first()
            assertNull("Password '$password' should be valid (contains digit)", uiState.passwordError)
        }

        // Test passwords without digits (should fail)
        passwordsWithoutDigit.forEach { password ->
            viewModel.onNameChanged("John Doe")
            viewModel.onEmailChanged("test@example.com")
            viewModel.onPasswordChanged(password)
            viewModel.onConfirmPasswordChanged(password)
            viewModel.signUpWithEmail()

            val uiState = viewModel.uiState.first()
            assertEquals("Passwort muss mindestens eine Zahl enthalten", uiState.passwordError)
        }
    }

    @Test
    fun `password confirmation validation works correctly`() = runTest {
        // Test matching passwords
        val password = "password123"
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)

        val uiState1 = viewModel.uiState.first()
        assertNull(uiState1.confirmPasswordError)

        // Test mismatched passwords
        viewModel.onConfirmPasswordChanged("differentPassword")

        val uiState2 = viewModel.uiState.first()
        assertEquals("Passwörter stimmen nicht überein", uiState2.confirmPasswordError)

        // Test empty confirmation
        viewModel.onConfirmPasswordChanged("")

        val uiState3 = viewModel.uiState.first()
        assertEquals("Passwort bestätigen ist erforderlich", uiState3.confirmPasswordError)
    }
}