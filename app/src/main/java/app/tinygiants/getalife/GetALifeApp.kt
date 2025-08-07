package app.tinygiants.getalife

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.tinygiants.getalife.domain.usecase.transaction.ProcessRecurringPaymentsUseCase
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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.HiltAndroidApp
import im.crisp.client.external.Crisp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class GetALifeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        configureAppCheck()
        configureSuperwall()
        configureRevenueCat()
        configureCrispChat()
        configureRecurringPayments()
    }
}

private fun Application.configureAppCheck() {
    Firebase.initialize(context = this)

    try {
        if (BuildConfig.DEBUG) {
            val debugProvider = DebugAppCheckProviderFactory.getInstance()
            Firebase.appCheck.installAppCheckProviderFactory(debugProvider)
        } else {
            Firebase.appCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
    } catch (e: Exception) {
        // Log App Check configuration error but don't crash the app
        if (BuildConfig.DEBUG) {
            println("App Check configuration failed: ${e.message}")
            e.printStackTrace()
        }
        // App will continue to work with placeholder tokens
    }
}

private fun Application.configureSuperwall() {
    try {
        Superwall.configure(
            applicationContext = this,
            apiKey = BuildConfig.SUPERWALL_PUBLIC_KEY
        )
    } catch (e: Exception) {
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

/**
 * Configures the recurring payments worker to run daily.
 * This ensures that due recurring payments are processed even when the app is closed.
 */
private fun Application.configureRecurringPayments() {
    val request = PeriodicWorkRequestBuilder<ProcessRecurringPaymentsWorker>(1, TimeUnit.DAYS)
        .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "recurring_payments_worker",
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}

class ProcessRecurringPaymentsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val processRecurringPaymentsUseCase: ProcessRecurringPaymentsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        processRecurringPaymentsUseCase()
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    @AssistedFactory
    interface Factory {
        fun create(context: Context, params: WorkerParameters): ProcessRecurringPaymentsWorker
    }
}