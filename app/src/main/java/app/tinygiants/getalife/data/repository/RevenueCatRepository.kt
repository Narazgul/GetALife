package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.domain.model.SubscriptionStatus.Active
import app.tinygiants.getalife.domain.model.SubscriptionStatus.NeverSubscribed
import app.tinygiants.getalife.domain.model.SubscriptionStatus.PastSubscriber
import app.tinygiants.getalife.domain.repository.SubscriptionRepository
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class RevenueCatRepository @Inject constructor() : SubscriptionRepository {

    override fun getSubscriptionFlow() = callbackFlow {

        val listener = UpdatedCustomerInfoListener { customerInfo ->

            val hasActiveSubscription = customerInfo.entitlements.active.isNotEmpty()
            val hasPurchasedInThePast = customerInfo.allPurchasedProductIds.isNotEmpty()

            val subscriptionStatus = when {
                hasActiveSubscription -> Active
                hasPurchasedInThePast -> PastSubscriber
                else -> NeverSubscribed
            }

            trySend(subscriptionStatus)
        }

        Purchases.sharedInstance.updatedCustomerInfoListener = listener

        awaitClose {
            Purchases.sharedInstance.removeUpdatedCustomerInfoListener()
        }
    }
}