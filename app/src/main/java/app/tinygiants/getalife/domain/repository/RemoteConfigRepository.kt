package app.tinygiants.getalife.domain.repository

import kotlinx.coroutines.flow.Flow

interface RemoteConfigRepository {

    fun getAppUpdateType(): Flow<Int?>
}