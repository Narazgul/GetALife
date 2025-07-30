package app.tinygiants.getalife.data.remote.firebase

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import im.crisp.client.external.notification.CrispNotificationClient
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var remoteFirebaseUsers: RemoteFirebaseUsers

    override fun handleIntent(intent: Intent?) {
        intent?.let {
            if (!CrispNotificationClient.isCrispIntent(intent)
                || CrispNotificationClient.isSessionExist(this, intent)
            ) {
                super.handleIntent(intent)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        CrispNotificationClient.sendTokenToCrisp(this, token)

        if (::remoteFirebaseUsers.isInitialized) {
            remoteFirebaseUsers.updateNotificationToken(firebaseMessagingToken = token)
        } else {
            Log.w("FirebaseMessagingService", "RemoteFirebaseUsers not initialized, skipping token update")
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (CrispNotificationClient.isCrispNotification(message)) CrispNotificationClient.handleNotification(this, message)
        else Log.d("FirebaseMessagingService", "Message received: ${message.data}")
    }
}