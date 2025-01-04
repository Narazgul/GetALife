package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {

    fun getSubscriptionFlow(): Flow<SubscriptionStatus>
}