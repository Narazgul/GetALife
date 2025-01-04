package app.tinygiants.getalife.domain.repository

import kotlinx.coroutines.flow.SharedFlow

interface InAppReviewRepository {

    fun observeInAppReviewRequests(): SharedFlow<Unit>
    suspend fun showInAppReview()
}