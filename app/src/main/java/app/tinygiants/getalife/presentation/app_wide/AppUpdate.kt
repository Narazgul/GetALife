package app.tinygiants.getalife.presentation.app_wide

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.usecase.appupdate.GetAppUpdateTypeUseCase
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun AppUpdate(activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>) {

    val viewModel: AppUpdateViewModel = hiltViewModel()
    val appUpdateType by viewModel.appUpdateType.collectAsStateWithLifecycle()

    if (appUpdateType == null) return

    val appUpdateManager = AppUpdateManagerFactory.create(LocalContext.current)

    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = appUpdateInfo.isUpdateTypeAllowed(appUpdateType!!)
            val appUpdateOptions = AppUpdateOptions.newBuilder(appUpdateType!!).build()

            if (isUpdateAvailable && isUpdateAllowed) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    appUpdateOptions,
                )
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->

                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activityResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )

                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    // TODO Show Snackbar to restart App
                }
            }
    }
}

@HiltViewModel
class AppUpdateViewModel @Inject constructor(private val getUpdateType: GetAppUpdateTypeUseCase) : ViewModel() {

    private val _appUpdateType = MutableStateFlow<Int?>(null)
    val appUpdateType = _appUpdateType.asStateFlow()

    init {
        viewModelScope.launch {
            getUpdateType()
                .catch { _appUpdateType.update { null } }
                .collect { appUpdateType -> _appUpdateType.update { appUpdateType } }
        }
    }
}