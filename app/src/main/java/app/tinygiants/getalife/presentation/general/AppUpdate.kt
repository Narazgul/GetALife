package app.tinygiants.getalife.presentation.general

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Composable
fun AppUpdate() {

    val viewModel: AppUpdateViewModel = hiltViewModel()
    val updateType by viewModel.updateType.collectAsStateWithLifecycle()

    if (updateType == null) return

    val appUpdateManager = AppUpdateManagerFactory.create(LocalContext.current)
    val updateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result -> }

    LaunchedEffect(Unit) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = appUpdateInfo.isUpdateTypeAllowed(updateType!!)
            val appUpdateOptions = AppUpdateOptions.newBuilder(updateType!!).build()

            if (isUpdateAvailable && isUpdateAllowed) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateLauncher,
                    appUpdateOptions
                )
            }
        }
    }

    // In-app updates lifecycle handling
    DisposableEffect(Unit) {
        val listener: (com.google.android.play.core.install.InstallState) -> Unit = { state ->
            when (state.installStatus()) {
                com.google.android.play.core.install.model.InstallStatus.DOWNLOADED -> { appUpdateManager.completeUpdate() }
                com.google.android.play.core.install.model.InstallStatus.INSTALLED -> { }
                com.google.android.play.core.install.model.InstallStatus.FAILED -> { }
                else -> { }
            }
        }

        appUpdateManager.registerListener(listener)

        onDispose {
            appUpdateManager.unregisterListener(listener)
        }
    }
}

@HiltViewModel
class AppUpdateViewModel @Inject constructor() : ViewModel() {

    private val _updateType = MutableStateFlow<Int?>(AppUpdateType.IMMEDIATE)
    val updateType: StateFlow<Int?> = _updateType.asStateFlow()

    init {
        _updateType.update { AppUpdateType.IMMEDIATE }
    }
}