package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.remote.firebase.RemoteFirebaseUsers
import app.tinygiants.getalife.domain.repository.SupportChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CrispChatRepository @Inject constructor(private val remoteFirebaseUsers: RemoteFirebaseUsers) : SupportChatRepository {

    override fun isSupportChatEnabled(): Flow<Boolean> = flow {
        remoteFirebaseUsers.isChatEnabled()
            .collect { isChatEnabled ->
                emit(isChatEnabled)
            }
    }

    override fun updateNotificationToken(token: String) = remoteFirebaseUsers.updateNotificationToken(token)
}