package app.tinygiants.getalife

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.tinygiants.getalife.domain.model.SubscriptionStatus
import app.tinygiants.getalife.domain.model.SubscriptionStatus.Active
import app.tinygiants.getalife.presentation.main_app.MainScreen
import app.tinygiants.getalife.presentation.onboarding.OnboardingNavHost

sealed class Path(val route: String) {
    data object Onboarding : Path(route = "onboardingNavGraph")
    data object Main : Path(route = "budgetNavGraph")
}

@Composable
fun GetALifeNavHost(
    getALifeNavController: NavHostController,
    subscriptionStatus: SubscriptionStatus
) {
    val startDestination = when (subscriptionStatus) {
        Active -> Path.Main.route
        else -> Path.Onboarding.route
    }

    val onNavigateToMainAppGraph = {
        getALifeNavController.navigate(Path.Main.route) {
            popUpTo(getALifeNavController.graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = getALifeNavController,
        startDestination = startDestination
    ) {

        composable(route = Path.Onboarding.route) {
            OnboardingNavHost(
                subscriptionStatus = subscriptionStatus,
                onNavigateToMainApp = onNavigateToMainAppGraph
            )
        }

        composable(route = Path.Main.route) {
            MainScreen()
        }
    }
}