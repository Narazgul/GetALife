package app.tinygiants.getalife.domain.model

sealed class SubscriptionStatus {
    data object Unknown : SubscriptionStatus()
    data object Active : SubscriptionStatus()
    data object PastSubscriber : SubscriptionStatus()
    data object NeverSubscribed : SubscriptionStatus()
}