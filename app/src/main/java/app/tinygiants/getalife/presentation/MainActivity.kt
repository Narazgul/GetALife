package app.tinygiants.getalife.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import app.tinygiants.getalife.GetALifeNavHost
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

        tryOpenCrispChatBox(intent)

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { }

        setContent {
            GetALifeTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val mainState by viewModel.mainState.collectAsStateWithLifecycle()

                EnableAppUpdate(
                    appUpdateType = mainState.appUpdateType,
                    activityResultLauncher = activityResultLauncher
                )
                RequestInAppReview(
                    isRequestingInAppReview = mainState.isRequestingInAppReview,
                    onInAppReviewRequestCompleted = viewModel::onInAppReviewRequestCompleted
                )
                RequestNotificationPermission(requestPermissionLauncher = requestPermissionLauncher)
                CrispChat()

                val navController = rememberNavController()
                GetALifeNavHost(
                    getALifeNavController = navController,
                    subscriptionStatus = mainState.subscriptionStatus
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        tryOpenCrispChatBox(intent)
    }

    private fun tryOpenCrispChatBox(intent: Intent): Boolean = CrispNotificationClient.openChatbox(this, intent)
}