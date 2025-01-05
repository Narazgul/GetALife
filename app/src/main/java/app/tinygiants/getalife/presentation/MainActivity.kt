package app.tinygiants.getalife.presentation

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
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.superwall.sdk.delegate.SuperwallDelegate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(), SuperwallDelegate {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Even tho we don't use the result, we need to register this for the AppUpdate mechanism
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode != RESULT_OK) Firebase.crashlytics.log(activityResult.toString())
        }

        setContent {
            GetALifeTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val mainState by viewModel.mainState.collectAsStateWithLifecycle()

                EnableAppUpdate(
                    appUpdateType = mainState.appUpdateType,
                    activityResultLauncher = activityResultLauncher
                )

                RequestInAppReview(
                    activity = this,
                    isRequestingInAppReview = mainState.isRequestingInAppReview,
                    onInAppReviewRequestCompleted = viewModel::onInAppReviewRequestCompleted
                )

                val navController = rememberNavController()
                GetALifeNavHost(
                    getALifeNavController = navController,
                    subscriptionStatus = mainState.subscriptionStatus
                )
            }
        }
    }
}