package app.tinygiants.getalife.presentation.main_app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.tinygiants.getalife.NestedNavGraph
import app.tinygiants.getalife.Screens.Main
import app.tinygiants.getalife.presentation.main_app.account.AccountScreen
import app.tinygiants.getalife.presentation.main_app.budget.BudgetScreen
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransaction
import app.tinygiants.getalife.presentation.main_app.transaction.transactions.TransactionScreen

@Composable
fun MainNavHost(
    bottomBarNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = bottomBarNavController,
        startDestination = NestedNavGraph.BudgetNavGraph.route,
        modifier = modifier
    ) {
        budgetGraph()
        addTransactionGraph()
        accountGraph()
    }
}

fun NavGraphBuilder.budgetGraph() {
    composable(route = NestedNavGraph.BudgetNavGraph.route) {

        val budgetNavController = rememberNavController()
        NavHost(
            navController = budgetNavController,
            startDestination = Main.Budget.route
        ) {
            composable(Main.Budget.route) { BudgetScreen() }
        }
    }
}

fun NavGraphBuilder.addTransactionGraph() {
    composable(route = NestedNavGraph.AddTransactionGraph.route) {

        val addTransactionNavController = rememberNavController()
        NavHost(
            navController = addTransactionNavController,
            startDestination = Main.AddTransaction.route
        ) {
            composable(Main.AddTransaction.route) { AddTransaction() }
        }
    }
}

fun NavGraphBuilder.accountGraph() {
    composable(route = NestedNavGraph.AccountNavGraph.route) {

        val accountNavController = rememberNavController()
        NavHost(
            navController = accountNavController,
            startDestination = Main.Account.route
        ) {
            composable(Main.Account.route) {
                AccountScreen(onNavigateToTransactionScreen = { accountId: Long ->
                    accountNavController.navigate("transaction/$accountId")
                })
            }
            composable(
                route = Main.Transactions.route,
                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
            ) {
                TransactionScreen(onNavigateUp = { accountNavController.navigateUp() })
            }
        }
    }
}