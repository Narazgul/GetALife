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
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
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
class SignUpViewModelTest {

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

    @MockK
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var viewModel: SignUpViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = SignUpViewModel(firebaseAuth, crashlytics)
    }

    @Test
    fun `initial state is correct`() = testScope.runTest {
        val uiState = viewModel.uiState.first()

        assertThat(uiState.name).isEqualTo("")
        assertThat(uiState.email).isEqualTo("")
        assertThat(uiState.password).isEqualTo("")
        assertThat(uiState.confirmPassword).isEqualTo("")
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.signUpSuccess).isFalse()
        assertThat(uiState.nameError).isNull()
        assertThat(uiState.emailError).isNull()
        assertThat(uiState.passwordError).isNull()
        assertThat(uiState.confirmPasswordError).isNull()
        assertThat(uiState.errorMessage).isNull()
    }

    @Test
    fun `onNameChanged updates name and clears error`() = testScope.runTest {
        // Given
        val name = "John Doe"

        // When
        viewModel.onNameChanged(name)

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.name).isEqualTo(name)
        assertThat(uiState.nameError).isNull()
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
    fun `onPasswordChanged updates password and validates confirm password`() = testScope.runTest {
        // Given
        val password = "password123"
        val confirmPassword = "differentPassword"

        viewModel.onConfirmPasswordChanged(confirmPassword)

        // When
        viewModel.onPasswordChanged(password)

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.password).isEqualTo(password)
        assertThat(uiState.passwordError).isNull()
        assertThat(uiState.confirmPasswordError).isEqualTo("Passwörter stimmen nicht überein")
    }

    @Test
    fun `onConfirmPasswordChanged validates password match`() = testScope.runTest {
        // Given
        val password = "password123"
        val confirmPassword = "password123"

        viewModel.onPasswordChanged(password)

        // When
        viewModel.onConfirmPasswordChanged(confirmPassword)

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.confirmPassword).isEqualTo(confirmPassword)
        assertThat(uiState.confirmPasswordError).isNull()
    }

    @Test
    fun `signUpWithEmail with invalid name shows error`() = testScope.runTest {
        // Given
        viewModel.onNameChanged("A") // too short
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")

        // When
        viewModel.signUpWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.nameError).isEqualTo("Name muss mindestens 2 Zeichen haben")
        assertThat(uiState.isLoading).isFalse()
    }

    @Test
    fun `signUpWithEmail with invalid email shows error`() = testScope.runTest {
        // Given
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")

        // When
        viewModel.signUpWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.emailError).isEqualTo("Ungültige E-Mail-Adresse")
        assertThat(uiState.isLoading).isFalse()
    }

    @Test
    fun `signUpWithEmail with weak password shows error`() = testScope.runTest {
        // Given
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("nodigit") // no digit
        viewModel.onConfirmPasswordChanged("nodigit")

        // When
        viewModel.signUpWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.passwordError).isEqualTo("Passwort muss mindestens eine Zahl enthalten")
        assertThat(uiState.isLoading).isFalse()
    }

    @Test
    fun `signUpWithEmail with mismatched passwords shows error`() = testScope.runTest {
        // Given
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("differentPassword")

        // When
        viewModel.signUpWithEmail()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.confirmPasswordError).isEqualTo("Passwörter stimmen nicht überein")
        assertThat(uiState.isLoading).isFalse()
    }

    @Test
    fun `signUpWithEmail success flow`() = testScope.runTest {
        // Given
        val name = "John Doe"
        val email = "test@example.com"
        val password = "password123"

        every { authResult.user } returns firebaseUser
        every { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns Tasks.forResult(authResult)
        every { firebaseUser.updateProfile(any<UserProfileChangeRequest>()) } returns Tasks.forResult(null)

        viewModel.onNameChanged(name)
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)

        // When
        viewModel.signUpWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.signUpSuccess).isTrue()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.errorMessage).isNull()

        verify { firebaseAuth.createUserWithEmailAndPassword(email, password) }
        verify { firebaseUser.updateProfile(any<UserProfileChangeRequest>()) }
    }

    @Test
    fun `signUpWithEmail with existing user shows error`() = testScope.runTest {
        // Given
        val name = "John Doe"
        val email = "existing@example.com"
        val password = "password123"
        val exception = FirebaseAuthUserCollisionException("email-already-in-use", "Email already in use")

        every { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns Tasks.forException(exception)

        viewModel.onNameChanged(name)
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)

        // When
        viewModel.signUpWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.signUpSuccess).isFalse()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.errorMessage).isEqualTo("Ein Benutzer mit dieser E-Mail existiert bereits")

        verify { crashlytics.recordException(exception) }
    }

    @Test
    fun `signUpWithEmail with weak password error shows error`() = testScope.runTest {
        // Given
        val name = "John Doe"
        val email = "test@example.com"
        val password = "weak1"
        val exception = FirebaseAuthWeakPasswordException("weak-password", "Password is too weak", "PASSWORD_TOO_WEAK")

        every { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns Tasks.forException(exception)

        viewModel.onNameChanged(name)
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)

        // When
        viewModel.signUpWithEmail()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.signUpSuccess).isFalse()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.errorMessage).isEqualTo("Passwort ist zu schwach")

        verify { crashlytics.recordException(exception) }
    }

    @Test
    fun `clearError clears error message`() = testScope.runTest {
        // Given - force an error state first
        viewModel.onNameChanged("")
        viewModel.signUpWithEmail()

        // When
        viewModel.clearError()

        // Then
        val uiState = viewModel.uiState.first()
        assertThat(uiState.errorMessage).isNull()
    }

    @Test
    fun `name validation works correctly`() = testScope.runTest {
        val validNames = listOf(
            "John Doe",
            "A B", // minimum 2 characters each
            "María José"
        )

        val invalidNames = listOf(
            "",
            "A" // too short
        )

        validNames.forEach { name ->
            viewModel.onNameChanged(name)
            viewModel.onEmailChanged("test@example.com")
            viewModel.onPasswordChanged("password123")
            viewModel.onConfirmPasswordChanged("password123")
            viewModel.signUpWithEmail()

            val uiState = viewModel.uiState.first()
            assertThat(uiState.nameError).isNull()
        }

        invalidNames.forEach { name ->
            viewModel.onNameChanged(name)
            viewModel.onEmailChanged("test@example.com")
            viewModel.onPasswordChanged("password123")
            viewModel.onConfirmPasswordChanged("password123")
            viewModel.signUpWithEmail()

            val uiState = viewModel.uiState.first()
            assertThat(uiState.nameError).isNotNull()
        }
    }

    @Test
    fun `password validation requires digit`() = testScope.runTest {
        val passwordsWithDigit = listOf(
            "password1",
            "123456",
            "strongPassword2"
        )

        val passwordsWithoutDigit = listOf(
            "password",
            "onlyletters",
            "NoDigitHere"
        )

        passwordsWithDigit.forEach { password ->
            viewModel.onNameChanged("John Doe")
            viewModel.onEmailChanged("test@example.com")
            viewModel.onPasswordChanged(password)
            viewModel.onConfirmPasswordChanged(password)
            viewModel.signUpWithEmail()

            val uiState = viewModel.uiState.first()
            assertThat(uiState.passwordError).isNull()
        }

        passwordsWithoutDigit.forEach { password ->
            viewModel.onNameChanged("John Doe")
            viewModel.onEmailChanged("test@example.com")
            viewModel.onPasswordChanged(password)
            viewModel.onConfirmPasswordChanged(password)
            viewModel.signUpWithEmail()

            val uiState = viewModel.uiState.first()
            assertThat(uiState.passwordError).isEqualTo("Passwort muss mindestens eine Zahl enthalten")
        }
    }
}