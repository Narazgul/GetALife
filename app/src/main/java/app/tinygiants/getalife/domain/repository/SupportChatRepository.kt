package app.tinygiants.getalife.domain.repository

import kotlinx.coroutines.flow.Flow

interface SupportChatRepository {

    fun isSupportChatEnabled(): Flow<Boolean>
}