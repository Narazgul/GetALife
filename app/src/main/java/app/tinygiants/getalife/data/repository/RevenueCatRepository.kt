package app.tinygiants.getalife.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.tinygiants.getalife.domain.model.SubscriptionStatus
import app.tinygiants.getalife.domain.model.SubscriptionStatus.Active
import app.tinygiants.getalife.domain.model.SubscriptionStatus.NeverSubscribed
import app.tinygiants.getalife.domain.model.SubscriptionStatus.PastSubscriber
import app.tinygiants.getalife.domain.model.SubscriptionStatus.Unknown
import app.tinygiants.getalife.domain.repository.SubscriptionRepository
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.subscriptionCache: DataStore<Preferences> by preferencesDataStore(name = "subscription_cache")

@Singleton
class RevenueCatRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SubscriptionRepository {

    private val dataStore = context.subscriptionCache
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private val CACHED_SUBSCRIPTION_STATUS = stringPreferencesKey("cached_subscription_status")
    }

    // Get cached subscription status
    private val cachedStatusFlow: Flow<SubscriptionStatus> = dataStore.data.map { prefs ->
        val statusString = prefs[CACHED_SUBSCRIPTION_STATUS]
        when (statusString) {
            "Active" -> Active
            "PastSubscriber" -> PastSubscriber
            "NeverSubscribed" -> NeverSubscribed
            else -> Unknown
        }
    }

    // Get live subscription status from RevenueCat
    private val liveStatusFlow: Flow<SubscriptionStatus> = callbackFlow {
        val listener = UpdatedCustomerInfoListener { customerInfo ->
            val hasActiveSubscription = customerInfo.entitlements.active.isNotEmpty()
            val hasPurchasedInThePast = customerInfo.allPurchasedProductIds.isNotEmpty()

            val subscriptionStatus = when {
                hasActiveSubscription -> Active
                hasPurchasedInThePast -> PastSubscriber
                else -> NeverSubscribed
            }

            // Cache the new status
            scope.launch {
                cacheSubscriptionStatus(subscriptionStatus)
            }

            trySend(subscriptionStatus)
        }

        Purchases.sharedInstance.updatedCustomerInfoListener = listener

        awaitClose {
            Purchases.sharedInstance.removeUpdatedCustomerInfoListener()
        }
    }

    override fun getSubscriptionFlow(): Flow<SubscriptionStatus> = combine(
        cachedStatusFlow,
        liveStatusFlow
    ) { cached, live ->
        // Prefer live status if available, fallback to cached
        if (live != Unknown) live else cached
    }

    /**
     * Get immediate cached subscription status for instant app startup
     */
    fun getCachedSubscriptionFlow(): Flow<SubscriptionStatus> = cachedStatusFlow

    private suspend fun cacheSubscriptionStatus(status: SubscriptionStatus) {
        dataStore.edit { prefs ->
            prefs[CACHED_SUBSCRIPTION_STATUS] = when (status) {
                Active -> "Active"
                PastSubscriber -> "PastSubscriber"
                NeverSubscribed -> "NeverSubscribed"
                Unknown -> return@edit // Don't cache Unknown status
            }
        }
    }
}