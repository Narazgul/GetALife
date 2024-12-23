package app.tinygiants.getalife.data.remote

import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import javax.inject.Inject

class AppUpdate @Inject constructor(private val appUpdateManager: AppUpdateManager) {

    private val appUpdateInfo = appUpdateManager.appUpdateInfo
    private val updateType = AppUpdateType.IMMEDIATE

    val canStartUpdateProgress = appUpdateInfo.addOnSuccessListener { appUpdateInfo ->

        val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        val isImmediateUpdateAllowed = appUpdateInfo.isImmediateUpdateAllowed

        isUpdateAvailable && isImmediateUpdateAllowed
    }
}