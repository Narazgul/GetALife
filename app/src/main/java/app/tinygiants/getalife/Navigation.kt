package app.tinygiants.getalife

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import app.tinygiants.getalife.presentation.account.AccountScreen
import app.tinygiants.getalife.presentation.budget.BudgetScreen
import app.tinygiants.getalife.presentation.transaction.TransactionScreen
import app.tinygiants.getalife.presentation.transaction.add_transaction.AddTransaction

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
        addTransactionGraph()
        accountGraph(navController)
    }
}

sealed class NestedNavGraph(val route: String) {
    data object BudgetNavGraph : NestedNavGraph("budgetNavGraph")
    data object AddTransactionGraph : NestedNavGraph("addTransactionGraph")
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

fun NavGraphBuilder.addTransactionGraph() {
    navigation(
        startDestination = Screens.AddTransaction.route,
        route = NestedNavGraph.AddTransactionGraph.route
    ) {
        composable(Screens.AddTransaction.route) { AddTransaction() }
    }
}

fun NavGraphBuilder.accountGraph(navController: NavHostController) {
    navigation(
        startDestination = Screens.Account.route,
        route = NestedNavGraph.AccountNavGraph.route
    ) {
        composable(Screens.Account.route) {
            AccountScreen(onNavigateToTransactionScreen = { accountId: Long ->
                navController.navigate("transaction/$accountId")
            })
        }
        composable(
            route = Screens.Transactions.route,
            arguments = listOf(navArgument("accountId") { type = NavType.LongType })
        ) {
            TransactionScreen(onNavigateUp = {
                navController.navigateUp()
            })
        }
    }
}

sealed class Screens(@StringRes val label: Int, val iconId: Int, val route: String) {
    data object Budget : Screens(label = R.string.budget, iconId = R.drawable.ic_dashboard, route = "budget")
    data object AddTransaction : Screens(label = R.string.transaction, iconId = R.drawable.ic_add, route = "add_transaction")
    data object Transactions : Screens(label = R.string.transaction, iconId = R.drawable.ic_add, route = "transaction/{accountId}")
    data object Account : Screens(label = R.string.account, iconId = R.drawable.ic_account, route = "account")
}