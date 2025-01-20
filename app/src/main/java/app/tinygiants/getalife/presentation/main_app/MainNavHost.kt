package app.tinygiants.getalife.presentation.main_app

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.tinygiants.getalife.R
import app.tinygiants.getalife.presentation.main_app.account.AccountScreen
import app.tinygiants.getalife.presentation.main_app.budget.BudgetScreen
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransaction
import app.tinygiants.getalife.presentation.main_app.transaction.transactions.TransactionScreen

sealed class MainNavGraph(@StringRes val label: Int, val iconId: Int, val route: String) {
    data object Budget : MainNavGraph(label = R.string.budget, iconId = R.drawable.ic_dashboard, route = "budgetNavGraph")
    data object AddTransaction : MainNavGraph(label = R.string.transaction, iconId = R.drawable.ic_add, route = "addTransactionGraph")
    data object Account : MainNavGraph(label = R.string.account, iconId = R.drawable.ic_account, route = "accountNavGraph")
}

sealed class MainScreens(val route: String) {
    data object Budget : MainScreens(route = "budget")
    data object AddTransaction : MainScreens(route = "add_transaction")
    data object Transactions : MainScreens(route = "transaction/{accountId}")
    data object Account : MainScreens(route = "account")
}

@Composable
fun MainNavHost(
    bottomBarNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = bottomBarNavController,
        startDestination = MainNavGraph.Budget.route,
        modifier = modifier
    ) {
        budgetGraph()
        addTransactionGraph()
        accountGraph()
    }
}

fun NavGraphBuilder.budgetGraph() {
    composable(route = MainNavGraph.Budget.route) {

        val budgetNavController = rememberNavController()
        NavHost(
            navController = budgetNavController,
            startDestination = MainScreens.Budget.route
        ) {
            composable(MainScreens.Budget.route) { BudgetScreen() }
        }
    }
}

fun NavGraphBuilder.addTransactionGraph() {
    composable(route = MainNavGraph.AddTransaction.route) {

        val addTransactionNavController = rememberNavController()
        NavHost(
            navController = addTransactionNavController,
            startDestination = MainScreens.AddTransaction.route
        ) {
            composable(MainScreens.AddTransaction.route) { AddTransaction() }
        }
    }
}

fun NavGraphBuilder.accountGraph() {
    composable(route = MainNavGraph.Account.route) {

        val accountNavController = rememberNavController()
        NavHost(
            navController = accountNavController,
            startDestination = MainScreens.Account.route
        ) {
            composable(MainScreens.Account.route) {
                AccountScreen(onNavigateToTransactionScreen = { accountId: Long ->
                    accountNavController.navigate("transaction/$accountId")
                })
            }
            composable(
                route = MainScreens.Transactions.route,
                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
            ) {
                TransactionScreen(onNavigateUp = { accountNavController.navigateUp() })
            }
        }
    }
}