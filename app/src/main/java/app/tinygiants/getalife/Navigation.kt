package app.tinygiants.getalife

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.tinygiants.getalife.domain.model.SubscriptionStatus
import app.tinygiants.getalife.presentation.main_app.MainScreen
import app.tinygiants.getalife.presentation.onboarding.OnboardingScreen

@Composable
fun GetALifeNavHost(
    getALifeNavController: NavHostController,
    subscriptionStatus: SubscriptionStatus,
) {
    val startDestination = when (subscriptionStatus) {
        SubscriptionStatus.Active -> NestedNavGraph.BudgetNavGraph.route
        else -> NestedNavGraph.OnboardingNavGraph.route
    }

    val onNavigateToMainAppGraph = {
        getALifeNavController.navigate(NestedNavGraph.BudgetNavGraph.route) {
            popUpTo(getALifeNavController.graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = getALifeNavController,
        startDestination = startDestination
    ) {
        onboardingGraph(onNavigateToMainAppGraph)
        mainAppGraph()
    }
}

fun NavGraphBuilder.onboardingGraph(onNavigateToMainAppGraph: () -> Unit) {
    composable(route = NestedNavGraph.OnboardingNavGraph.route) {
        OnboardingScreen(onNavigateToMainApp = onNavigateToMainAppGraph)
    }
}

fun NavGraphBuilder.mainAppGraph() {
    composable(route = NestedNavGraph.BudgetNavGraph.route) {
        MainScreen()
    }
}

sealed class NestedNavGraph(@StringRes val label: Int, val iconId: Int, val route: String) {

    data object OnboardingNavGraph :
        NestedNavGraph(label = R.string.onboarding, iconId = R.drawable.ic_navigation, route = "onboardingNavGraph")

    data object BudgetNavGraph :
        NestedNavGraph(label = R.string.budget, iconId = R.drawable.ic_dashboard, route = "budgetNavGraph")

    data object AddTransactionGraph :
        NestedNavGraph(label = R.string.transaction, iconId = R.drawable.ic_add, route = "addTransactionGraph")

    data object AccountNavGraph :
        NestedNavGraph(label = R.string.account, iconId = R.drawable.ic_account, route = "accountNavGraph")
}


sealed class Screens(val route: String) {

    sealed class Onboarding {
        data object Step1 : Screens(route = "onboarding")
        data object Paywall : Screens(route = "paywall")
    }
    sealed class Main {
        data object Budget : Screens(route = "budget")
        data object AddTransaction : Screens(route = "add_transaction")
        data object Transactions : Screens(route = "transaction/{accountId}")
        data object Account : Screens(route = "account")
    }
}