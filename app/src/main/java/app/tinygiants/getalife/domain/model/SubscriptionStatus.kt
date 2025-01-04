package app.tinygiants.getalife.domain.model

sealed class SubscriptionStatus {
    data object Unknown : SubscriptionStatus()
    data object Active : SubscriptionStatus()
    data object Inactive : SubscriptionStatus()
    data class Error(val message: String) : SubscriptionStatus()
}