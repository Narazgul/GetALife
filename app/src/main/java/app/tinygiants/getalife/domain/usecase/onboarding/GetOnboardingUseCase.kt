package app.tinygiants.getalife.domain.usecase.onboarding

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetOnboardingUseCase @Inject constructor() {

    operator fun invoke(): Flow<Result<String>> = flow {
        emit(Result.success("OnboardingScreen"))
    }
}