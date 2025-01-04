package app.tinygiants.getalife.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.tinygiants.getalife.Screens.Onboarding
import app.tinygiants.getalife.presentation.onboarding.step1.Step1Screen

@Composable
fun OnboardingScreen(
    onNavigateToMainApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Onboarding.Paywall.route,
        modifier = modifier
    ) {
        composable(Onboarding.Step1.route) {
            Step1Screen(onNavigateToPaywall = { navController.navigate(Onboarding.Paywall.route) })
        }
        composable(Onboarding.Paywall.route) {
            Paywall(onNavigateToMainApp = onNavigateToMainApp)
        }
    }
}