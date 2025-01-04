package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.domain.repository.RemoteConfigRepository
import com.google.android.play.core.install.model.AppUpdateType
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FirebaseRemoteConfigRepository @Inject constructor(private val remoteConfig: FirebaseRemoteConfig) :
    RemoteConfigRepository {

    init {
        remoteConfig.fetchAndActivate()
    }

    override fun getAppUpdateType(): Flow<Int?> = flow {

        val showImmediateUpdate = remoteConfig.getBoolean("immediate_update")
        val showFlexibleUpdate = remoteConfig.getBoolean("flexible_update")

        when {
            showImmediateUpdate && showFlexibleUpdate -> emit(AppUpdateType.IMMEDIATE)
            showImmediateUpdate -> emit(AppUpdateType.IMMEDIATE)
            showFlexibleUpdate -> emit(AppUpdateType.FLEXIBLE)
            else -> emit(null)
        }
    }
}