package app.tinygiants.getalife.domain.usecase.subscription

import app.tinygiants.getalife.data.repository.RevenueCatRepository
import app.tinygiants.getalife.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserSubscriptionStatusUseCase @Inject constructor(
    private val repository: RevenueCatRepository
) {
    /**
     * Gets subscription status with live updates and caching fallback
     */
    operator fun invoke(): Flow<SubscriptionStatus> = repository.getSubscriptionFlow()

    /**
     * Gets cached subscription status for instant app startup
     */
    fun getCachedStatus(): Flow<SubscriptionStatus> = repository.getCachedSubscriptionFlow()
}