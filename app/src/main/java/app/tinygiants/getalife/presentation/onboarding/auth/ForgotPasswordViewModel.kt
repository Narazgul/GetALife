package app.tinygiants.getalife.presentation.onboarding.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val resetEmailSent: Boolean = false,
    val emailError: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun sendPasswordResetEmail() {
        val currentState = _uiState.value

        // Validate email
        val emailError = validateEmail(currentState.email)

        if (emailError != null) {
            _uiState.value = currentState.copy(emailError = emailError)
            return
        }

        _uiState.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                firebaseAuth.sendPasswordResetEmail(currentState.email.trim()).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    resetEmailSent = true
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "E-Mail ist erforderlich"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Ungültige E-Mail-Adresse"
            else -> null
        }
    }

    private fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthInvalidUserException -> "Kein Benutzer mit dieser E-Mail gefunden"
            else -> "Reset-E-Mail konnte nicht gesendet werden: ${exception.localizedMessage}"
        }
    }
}