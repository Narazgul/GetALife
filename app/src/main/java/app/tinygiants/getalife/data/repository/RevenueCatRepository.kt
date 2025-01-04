package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.domain.model.SubscriptionStatus.Active
import app.tinygiants.getalife.domain.model.SubscriptionStatus.Error
import app.tinygiants.getalife.domain.model.SubscriptionStatus.Inactive
import app.tinygiants.getalife.domain.repository.SubscriptionRepository
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

private const val entitlementId = "premium_access"

class RevenueCatRepository @Inject constructor() : SubscriptionRepository {

    override fun getSubscriptionFlow() = callbackFlow {
        Purchases.sharedInstance.getCustomerInfoWith(
            onError = { error -> trySend(Error(error.message)) },
            onSuccess = { customerInfo ->
                val hasActiveSubscription = customerInfo.entitlements.active.isNotEmpty()

                if (hasActiveSubscription) trySend(Active)
                else trySend(Inactive)
            }
        )

        val listener = UpdatedCustomerInfoListener { customerInfo ->
            val isActive = customerInfo.entitlements.active.containsKey(entitlementId)
            val newSubscriptionStatus = if (isActive) Active else Inactive
            trySend(newSubscriptionStatus)
        }
        Purchases.sharedInstance.updatedCustomerInfoListener = listener

        awaitClose {
            Purchases.sharedInstance.removeUpdatedCustomerInfoListener()
        }
    }
}