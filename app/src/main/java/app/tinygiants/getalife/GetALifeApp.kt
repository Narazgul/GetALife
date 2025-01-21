package app.tinygiants.getalife

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.superwall.sdk.Superwall
import dagger.hilt.android.HiltAndroidApp
import im.crisp.client.external.Crisp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class GetALifeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        configureAppCheck()
        configureSuperwall()
        configureRevenueCat()
        configureCrispChat()
    }
}

private fun Application.configureAppCheck() {
    Firebase.initialize(context = this)
    Firebase.appCheck.installAppCheckProviderFactory(
        if (BuildConfig.DEBUG) DebugAppCheckProviderFactory.getInstance()
        else PlayIntegrityAppCheckProviderFactory.getInstance(),
    )
}

private fun Application.configureSuperwall() =
    Superwall.configure(
        applicationContext = this,
        apiKey = BuildConfig.SUPERWALL_PUBLIC_KEY
    )

private fun Application.configureRevenueCat() {
    Purchases.logLevel = LogLevel.DEBUG
    val configuration = PurchasesConfiguration.Builder(context = this, apiKey = BuildConfig.REVENUECAT_API_KEY).build()
    Purchases.configure(configuration)

    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
        val appInstanceId = Firebase.analytics.firebaseInstanceId
        Purchases.sharedInstance.setFirebaseAppInstanceID(firebaseAppInstanceID = appInstanceId)
    }
}

private fun Application.configureCrispChat() = Crisp.configure(this, BuildConfig.CRISP_CHAT)