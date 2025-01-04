package app.tinygiants.getalife.domain.usecase.inappreview

import app.tinygiants.getalife.domain.repository.InAppReviewRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class ObserveInAppReviewRequestsUseCase @Inject constructor(private val repository: InAppReviewRepository) {

    operator fun invoke(): SharedFlow<Unit> = repository.observeInAppReviewRequests()
}