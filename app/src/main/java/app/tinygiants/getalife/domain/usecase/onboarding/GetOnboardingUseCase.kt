package app.tinygiants.getalife.domain.usecase.onboarding

import app.tinygiants.getalife.domain.usecase.onboarding.OnboardingStep.Quote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

sealed class OnboardingStep() {
    data object Quote : OnboardingStep()
}

class GetOnboardingUseCase @Inject constructor() {

    operator fun invoke(): Flow<Result<OnboardingStep>> = flow {
        emit(Result.success(Quote))
    }
}