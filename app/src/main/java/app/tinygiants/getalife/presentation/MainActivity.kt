package app.tinygiants.getalife.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.tinygiants.getalife.GetALifeNavHost
import app.tinygiants.getalife.presentation.app_wide.AppUpdate
import app.tinygiants.getalife.presentation.app_wide.Authentication
import app.tinygiants.getalife.presentation.app_wide.InAppReview
import app.tinygiants.getalife.presentation.app_wide.Notification
import app.tinygiants.getalife.presentation.app_wide.SupportChat
import app.tinygiants.getalife.theme.GetALifeTheme
import com.superwall.sdk.delegate.SuperwallDelegate
import dagger.hilt.android.AndroidEntryPoint
import im.crisp.client.external.notification.CrispNotificationClient


@AndroidEntryPoint
class MainActivity : ComponentActivity(), SuperwallDelegate {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
        val appUpdateLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { }

        setContent {
            GetALifeTheme {

                Authentication()
                InAppReview()
                SupportChat()
                Notification(requestPermissionLauncher = permissionLauncher)
                AppUpdate(activityResultLauncher = appUpdateLauncher)

                GetALifeNavHost()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        CrispNotificationClient.openChatbox(this, intent)
    }
}