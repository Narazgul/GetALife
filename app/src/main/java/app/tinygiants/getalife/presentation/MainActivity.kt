package app.tinygiants.getalife.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.tinygiants.getalife.GetALifeNavHost
import app.tinygiants.getalife.presentation.general.AppUpdate
import app.tinygiants.getalife.presentation.general.Authentication
import app.tinygiants.getalife.presentation.general.InAppReview
import app.tinygiants.getalife.presentation.general.Notification
import app.tinygiants.getalife.presentation.general.SupportChat
import app.tinygiants.getalife.theme.GetALifeTheme
import com.superwall.sdk.delegate.SuperwallDelegate
import dagger.hilt.android.AndroidEntryPoint
import im.crisp.client.external.notification.CrispNotificationClient

@AndroidEntryPoint
class MainActivity : ComponentActivity(), SuperwallDelegate {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val notificationLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { }

        setContent {
            GetALifeTheme {

                Authentication()
                InAppReview()
                SupportChat()
                Notification(notificationLauncher = notificationLauncher)
                AppUpdate()

                GetALifeNavHost()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        CrispNotificationClient.openChatbox(this, intent)
    }
}