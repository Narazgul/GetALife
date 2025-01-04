package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.domain.repository.InAppReviewRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppReviewRepositoryImpl @Inject constructor() : InAppReviewRepository {

    private val _inAppReviewRequests = MutableSharedFlow<Unit>()

    override fun observeInAppReviewRequests(): SharedFlow<Unit> = _inAppReviewRequests.asSharedFlow()
    override suspend fun showInAppReview() = _inAppReviewRequests.emit(Unit)

}