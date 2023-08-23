package app.tinygiants.getalife

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import app.tinygiants.getalife.presentation.account.accountGraph
import app.tinygiants.getalife.presentation.budget.budgetGraph

@Composable
fun GetALifeNavHost(
    navController: NavHostController,
    startDestination: String = NestedNavigation.BudgetNavigation.route,
    modifier: Modifier
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        budgetGraph()
        accountGraph()
    }
}

sealed class NestedNavigation(val route: String) {
    data object BudgetNavigation : NestedNavigation("budgetNavigation")
    data object AccountNavigation : NestedNavigation("accountNavigation")
}

sealed class Screens(val label: String, val iconId: Int, val route: String) {
    data object Budget : Screens(label = "Budget", iconId = R.drawable.ic_dashboard, route = "budget")
    data object Account : Screens(label = "Account", iconId = R.drawable.ic_account, route = "account")
}