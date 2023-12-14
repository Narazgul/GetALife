package app.tinygiants.getalife

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.tinygiants.getalife.presentation.account.AccountScreen
import app.tinygiants.getalife.presentation.budget.BudgetScreen

@Composable
fun GetALifeNavHost(
    navController: NavHostController,
    startDestination: String = NestedNavGraph.BudgetNavGraph.route,
    modifier: Modifier
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        budgetGraph()
        accountGraph()
    }
}

sealed class NestedNavGraph(val route: String) {
    data object BudgetNavGraph : NestedNavGraph("budgetNavGraph")
    data object AccountNavGraph : NestedNavGraph("accountNavGraph")
}

fun NavGraphBuilder.budgetGraph() {
    navigation(
        startDestination = Screens.Budget.route,
        route = NestedNavGraph.BudgetNavGraph.route
    ) {
        composable(Screens.Budget.route) { BudgetScreen() }
    }
}

fun NavGraphBuilder.accountGraph() {
    navigation(
        startDestination = Screens.Account.route,
        route = NestedNavGraph.AccountNavGraph.route
    ) {
        composable(Screens.Account.route) { AccountScreen() }
    }
}

sealed class Screens(val label: String, val iconId: Int, val route: String) {
    data object Budget : Screens(label = "Budget", iconId = R.drawable.ic_dashboard, route = "budget")
    data object Account : Screens(label = "Account", iconId = R.drawable.ic_account, route = "account")
}