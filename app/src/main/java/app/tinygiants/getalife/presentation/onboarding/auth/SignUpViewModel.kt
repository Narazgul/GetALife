package app.tinygiants.getalife.presentation.onboarding.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.TwitterAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val signUpSuccess: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = null
        )
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            confirmPasswordError = if (_uiState.value.confirmPassword.isNotEmpty()) {
                validatePasswordMatch(password, _uiState.value.confirmPassword)
            } else null
        )
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = validatePasswordMatch(_uiState.value.password, confirmPassword)
        )
    }

    fun signUpWithEmail() {
        val currentState = _uiState.value

        // Validate inputs
        val nameError = validateName(currentState.name)
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)
        val confirmPasswordError = validatePasswordMatch(currentState.password, currentState.confirmPassword)

        if (nameError != null || emailError != null || passwordError != null || confirmPasswordError != null) {
            _uiState.value = currentState.copy(
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
            return
        }

        _uiState.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(
                    currentState.email.trim(),
                    currentState.password
                ).await()

                // Update user profile with name
                authResult.user?.updateProfile(
                    userProfileChangeRequest {
                        displayName = currentState.name.trim()
                    }
                )?.await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    signUpSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = getErrorMessage(e)
                )
                crashlytics.recordException(e)
            }
        }
    }

    fun signUpWithGoogle(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(credential).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    signUpSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google Sign-Up fehlgeschlagen: ${e.localizedMessage}"
                )
                crashlytics.recordException(e)
            }
        }
    }

    fun signUpWithFacebook(accessToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val credential = FacebookAuthProvider.getCredential(accessToken)
                firebaseAuth.signInWithCredential(credential).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    signUpSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Facebook Sign-Up fehlgeschlagen: ${e.localizedMessage}"
                )
                crashlytics.recordException(e)
            }
        }
    }

    fun signUpWithTwitter(token: String, secret: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val credential = TwitterAuthProvider.getCredential(token, secret)
                firebaseAuth.signInWithCredential(credential).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    signUpSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Twitter Sign-Up fehlgeschlagen: ${e.localizedMessage}"
                )
                crashlytics.recordException(e)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name ist erforderlich"
            name.length < 2 -> "Name muss mindestens 2 Zeichen haben"
            else -> null
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "E-Mail ist erforderlich"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Ungültige E-Mail-Adresse"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Passwort ist erforderlich"
            password.length < 6 -> "Passwort muss mindestens 6 Zeichen haben"
            !password.any { it.isDigit() } -> "Passwort muss mindestens eine Zahl enthalten"
            else -> null
        }
    }

    private fun validatePasswordMatch(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Passwort bestätigen ist erforderlich"
            password != confirmPassword -> "Passwörter stimmen nicht überein"
            else -> null
        }
    }

    private fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthUserCollisionException -> "Ein Benutzer mit dieser E-Mail existiert bereits"
            is FirebaseAuthInvalidCredentialsException -> "Ungültige E-Mail-Adresse"
            is FirebaseAuthWeakPasswordException -> "Passwort ist zu schwach"
            else -> "Registrierung fehlgeschlagen: ${exception.localizedMessage}"
        }
    }
}