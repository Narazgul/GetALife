package app.tinygiants.getalife.domain.usecase.appupdate

import app.tinygiants.getalife.domain.repository.RemoteConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAppUpdateTypeUseCase @Inject constructor(private val remoteConfig: RemoteConfigRepository) {

    operator fun invoke(): Flow<Int?> = flow {
        remoteConfig.getAppUpdateType()
            .collect { appUpdateType ->
                emit(appUpdateType)
            }
    }
}