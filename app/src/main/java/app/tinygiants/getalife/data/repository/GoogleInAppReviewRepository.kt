package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.domain.repository.InAppReviewRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleInAppReviewRepository @Inject constructor() : InAppReviewRepository {

    // Use replay and extra buffer to avoid missing a one-shot trigger
    private val _inAppReviewRequests = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)

    override fun observeInAppReviewRequests(): SharedFlow<Unit> = _inAppReviewRequests.asSharedFlow()
    override suspend fun showInAppReview() = _inAppReviewRequests.emit(Unit)

}