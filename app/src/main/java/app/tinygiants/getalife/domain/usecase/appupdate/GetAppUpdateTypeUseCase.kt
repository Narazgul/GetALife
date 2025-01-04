package app.tinygiants.getalife.domain.usecase.appupdate

import app.tinygiants.getalife.domain.repository.RemoteConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAppUpdateTypeUseCase @Inject constructor(private val remoteConfig: RemoteConfigRepository) {

    operator fun invoke(): Flow<Result<Int>> = flow {
        remoteConfig.getAppUpdateType()
            .catch { throwable ->
                emit(Result.failure(throwable))
            }.collect { appUpdateType ->
                if (appUpdateType == null) emit(Result.failure(Throwable()))
                else emit(Result.success(appUpdateType))
            }
    }
}