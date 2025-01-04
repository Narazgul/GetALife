package app.tinygiants.getalife.domain.usecase.subscription

import app.tinygiants.getalife.domain.model.SubscriptionStatus
import app.tinygiants.getalife.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserSubscriptionStatusUseCase @Inject constructor(private val repository: SubscriptionRepository) {
    operator fun invoke(): Flow<SubscriptionStatus> = repository.getSubscriptionFlow()
}