package app.tinygiants.getalife.presentation.account

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(AccountState())
    val uiState = _uiState.asStateFlow()
}