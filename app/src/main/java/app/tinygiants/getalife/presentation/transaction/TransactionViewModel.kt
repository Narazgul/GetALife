package app.tinygiants.getalife.presentation.transaction

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class TransactionViewModel @Inject constructor(): ViewModel() {

    private val _uiState = MutableStateFlow(
        TransactionUiState(
            title = "TransactionScreen"
        )
    )
    val uiState = _uiState.asStateFlow()

}