package app.tinygiants.getalife.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.tinygiants.getalife.presentation.account.AccountScreen
import app.tinygiants.getalife.presentation.budget.BudgetScreen

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