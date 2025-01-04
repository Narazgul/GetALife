package app.tinygiants.getalife.domain.usecase.inappreview

import app.tinygiants.getalife.domain.repository.InAppReviewRepository
import javax.inject.Inject

class RequestInAppReviewUseCase @Inject constructor(private val repository: InAppReviewRepository) {

    suspend operator fun invoke() = repository.showInAppReview()

}