package app.tinygiants.getalife

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.tinygiants.getalife.presentation.account.AccountScreen
import app.tinygiants.getalife.presentation.account.AccountViewModel
import app.tinygiants.getalife.presentation.budget.BudgetScreen
import app.tinygiants.getalife.presentation.budget.BudgetViewModel

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
        composable(Screens.Budget.route) {
            val viewModel: BudgetViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            BudgetScreen(uiState)
        }
    }
}

fun NavGraphBuilder.accountGraph() {
    navigation(
        startDestination = Screens.Account.route,
        route = NestedNavGraph.AccountNavGraph.route
    ) {
        composable(Screens.Account.route) {
            val viewModel: AccountViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            AccountScreen(uiState)
        }
    }
}

sealed class Screens(val label: String, val iconId: Int, val route: String) {
    data object Budget : Screens(label = "Budget", iconId = R.drawable.ic_dashboard, route = "budget")
    data object Account : Screens(label = "Account", iconId = R.drawable.ic_account, route = "account")
}