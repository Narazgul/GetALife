package app.tinygiants.getalife

import android.app.Application
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.superwall.sdk.Superwall
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GetALifeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        configureSuperwall()
        configureRevenueCat()
    }
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
}