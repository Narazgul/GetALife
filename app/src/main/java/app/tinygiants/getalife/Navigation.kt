package app.tinygiants.getalife

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.tinygiants.getalife.presentation.account.AccountScreen
import app.tinygiants.getalife.presentation.budget.BudgetScreen
import app.tinygiants.getalife.presentation.transaction.TransactionScreen

@Composable
fun GetALifeNavHost(
    navController: NavHostController,
    startDestination: String = NestedNavGraph.BudgetNavGraph.route,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        budgetGraph()
        accountGraph()
        transactionGraph()
    }
}

sealed class NestedNavGraph(val route: String) {
    data object BudgetNavGraph : NestedNavGraph("budgetNavGraph")
    data object AccountNavGraph : NestedNavGraph("accountNavGraph")
    data object TransactionNavGraph : NestedNavGraph("transactionNavGraph")
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

fun NavGraphBuilder.transactionGraph() {
    navigation(
        startDestination = Screens.Transaction.route,
        route = NestedNavGraph.TransactionNavGraph.route
    ) {
        composable(Screens.Transaction.route) { TransactionScreen() }
    }
}

sealed class Screens(@StringRes val label: Int, val iconId: Int, val route: String) {
    data object Budget : Screens(label = R.string.budget, iconId = R.drawable.ic_dashboard, route = "budget")
    data object Account : Screens(label = R.string.account, iconId = R.drawable.ic_account, route = "account")
    data object Transaction : Screens(label = R.string.transaction, iconId = R.drawable.ic_list, route = "transaction")
}