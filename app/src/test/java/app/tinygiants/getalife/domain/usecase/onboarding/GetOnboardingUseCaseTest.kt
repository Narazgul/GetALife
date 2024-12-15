package app.tinygiants.getalife.domain.usecase.onboarding

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetOnboardingUseCaseTest {

    private lateinit var getOnboarding: GetOnboardingUseCase

    @BeforeEach
    fun setUp() {
        getOnboarding = GetOnboardingUseCase()
    }

    @Test
    fun `get Title`(): Unit = runTest {
        getOnboarding().test {
            val firstEmission = awaitItem().getOrNull()
            awaitComplete()

            assertThat(firstEmission).isEqualTo("OnboardingScreen")
        }
    }
}