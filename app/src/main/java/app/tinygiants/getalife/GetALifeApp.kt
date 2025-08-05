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
        // Schedule recurring payments worker
        app.tinygiants.getalife.data.worker.ProcessRecurringPaymentsWorker.enqueue(this)
    }
}

private fun Application.configureAppCheck() {
    Firebase.initialize(context = this)

    if (BuildConfig.DEBUG) {
        val debugProvider = DebugAppCheckProviderFactory.getInstance()
        Firebase.appCheck.installAppCheckProviderFactory(debugProvider)
    } else {
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }
}

private fun Application.configureSuperwall() {
    try {
        Superwall.configure(
            applicationContext = this,
            apiKey = BuildConfig.SUPERWALL_PUBLIC_KEY
        )
    } catch (e: Exception) {
        // Log configuration error but don't crash the app
        if (BuildConfig.DEBUG) {
            e.printStackTrace()
        }
    }
}

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