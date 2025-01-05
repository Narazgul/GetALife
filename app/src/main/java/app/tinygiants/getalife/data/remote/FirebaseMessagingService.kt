package app.tinygiants.getalife.data.remote

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import im.crisp.client.external.notification.CrispNotificationClient


class MyFirebaseMessagingService : FirebaseMessagingService() {

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
        Log.d("FirebaseMessagingService", "Token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (CrispNotificationClient.isCrispNotification(message)) CrispNotificationClient.handleNotification(this, message)
        else Log.d("FirebaseMessagingService", "Message received: ${message.data}")
    }

}