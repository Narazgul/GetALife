package app.tinygiants.getalife.domain.repository

import kotlinx.coroutines.flow.Flow

interface RemoteConfigRepository {

    fun getFeatureFlagValue(flagName: String): Flow<Boolean>

}

