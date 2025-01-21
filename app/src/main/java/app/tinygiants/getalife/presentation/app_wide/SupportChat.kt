package app.tinygiants.getalife.presentation.app_wide

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.usecase.chat.DisplaySupportChatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun ComponentActivity.SupportChat() {
    val viewModel: SupportChatViewModel = hiltViewModel()
    val isSupportChatEnabled by viewModel.isSupportChatEnabled.collectAsStateWithLifecycle()

    if (!isSupportChatEnabled) return

    val crispIntent = Intent(this, im.crisp.client.external.ChatActivity::class.java)
    startActivity(crispIntent)
}

@HiltViewModel
class SupportChatViewModel @Inject constructor(private val displaySupportChat: DisplaySupportChatUseCase) : ViewModel() {

    private val _isSupportChatEnabled = MutableStateFlow(false)
    val isSupportChatEnabled = _isSupportChatEnabled.asStateFlow()

    init {
        shouldShowSupportChat()
    }

    private fun shouldShowSupportChat() {
        viewModelScope.launch {
            displaySupportChat().collect { isChatEnabled ->
                _isSupportChatEnabled.update { isChatEnabled }
            }
        }
    }
}