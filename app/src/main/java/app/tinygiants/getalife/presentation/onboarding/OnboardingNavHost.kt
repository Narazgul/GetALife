package app.tinygiants.getalife.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.tinygiants.getalife.domain.model.SubscriptionStatus
import app.tinygiants.getalife.domain.model.SubscriptionStatus.NeverSubscribed
import app.tinygiants.getalife.domain.model.SubscriptionStatus.PastSubscriber
import app.tinygiants.getalife.presentation.onboarding.step1.Step1Screen
import com.superwall.sdk.Superwall
import com.superwall.sdk.paywall.presentation.register

sealed class OnboardingScreens(val route: String) {
    data object Step1 : OnboardingScreens(route = "step1")
}

sealed class Paywall {
    data object NeverSubscribed : OnboardingScreens(route = "never_subscribed")
    data object PastSubscriber : OnboardingScreens(route = "past_subscriber")
}

@Composable
fun OnboardingNavHost(
    subscriptionStatus: SubscriptionStatus,
    onNavigateToMainApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val startDestination = when (subscriptionStatus) {
        NeverSubscribed -> OnboardingScreens.Step1.route
        PastSubscriber -> Paywall.PastSubscriber.route
        else -> OnboardingScreens.Step1.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(OnboardingScreens.Step1.route) {
            Step1Screen(onNavigateToPaywall = { navController.navigate(Paywall.NeverSubscribed.route) })
        }
        composable(Paywall.NeverSubscribed.route) {
            Superwall.instance.register("NeverSubscribed") { onNavigateToMainApp() }
        }
        composable(Paywall.PastSubscriber.route) {
            Superwall.instance.register("PastSubscriber") { onNavigateToMainApp() }
        }
    }
}