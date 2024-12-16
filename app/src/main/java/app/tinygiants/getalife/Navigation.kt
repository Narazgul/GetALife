package app.tinygiants.getalife

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.tinygiants.getalife.presentation.account.AccountScreen
import app.tinygiants.getalife.presentation.budget.BudgetScreen
import app.tinygiants.getalife.presentation.onboarding.OnboardingScreen
import app.tinygiants.getalife.presentation.transaction.add_transaction.AddTransaction
import app.tinygiants.getalife.presentation.transaction.transactions.TransactionScreen

@Composable
fun GetALifeNavHost(
    bottomBarNavController: NavHostController,
    startDestination: String = NestedNavGraph.BudgetNavGraph.route,
    modifier: Modifier
) {
    NavHost(
        navController = bottomBarNavController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        onboardingGraph()
        budgetGraph()
        addTransactionGraph()
        accountGraph()
    }
}

sealed class NestedNavGraph(@StringRes val label: Int, val iconId: Int, val route: String) {

    data object OnboardingNavGraph :
    NestedNavGraph(label = R.string.onboarding, iconId = R.drawable.ic_launcher_monochrome, route = "onboardingNavGraph")

    data object BudgetNavGraph :
        NestedNavGraph(label = R.string.budget, iconId = R.drawable.ic_dashboard, route = "budgetNavGraph")

    data object AddTransactionGraph :
        NestedNavGraph(label = R.string.transaction, iconId = R.drawable.ic_add, route = "addTransactionGraph")

    data object AccountNavGraph :
        NestedNavGraph(label = R.string.account, iconId = R.drawable.ic_account, route = "accountNavGraph")
}

fun NavGraphBuilder.onboardingGraph() {
    composable(route = NestedNavGraph.OnboardingNavGraph.route) {

        val onboardingNavController = rememberNavController()
        NavHost(
            navController = onboardingNavController,
            startDestination = Screens.Onboarding.route
        ) {
            composable(Screens.Onboarding.route) { OnboardingScreen() }
        }
    }
}

fun NavGraphBuilder.budgetGraph() {
    composable(route = NestedNavGraph.BudgetNavGraph.route) {

        val budgetNavController = rememberNavController()
        NavHost(
            navController = budgetNavController,
            startDestination = Screens.Budget.route
        ) {
            composable(Screens.Budget.route) { BudgetScreen() }
        }
    }
}

fun NavGraphBuilder.addTransactionGraph() {
    composable(route = NestedNavGraph.AddTransactionGraph.route) {

        val addTransactionNavController = rememberNavController()
        NavHost(
            navController = addTransactionNavController,
            startDestination = Screens.AddTransaction.route
        ) {
            composable(Screens.AddTransaction.route) { AddTransaction() }
        }
    }
}

fun NavGraphBuilder.accountGraph() {
    composable(route = NestedNavGraph.AccountNavGraph.route) {

        val accountNavController = rememberNavController()
        NavHost(
            navController = accountNavController,
            startDestination = Screens.Account.route
        ) {
            composable(Screens.Account.route) {
                AccountScreen(onNavigateToTransactionScreen = { accountId: Long ->
                    accountNavController.navigate("transaction/$accountId")
                })
            }
            composable(
                route = Screens.Transactions.route,
                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
            ) {
                TransactionScreen(onNavigateUp = { accountNavController.navigateUp() })
            }
        }
    }
}

sealed class Screens(val route: String) {
    data object Onboarding : Screens(route = "onboarding")
    data object Budget : Screens(route = "budget")
    data object AddTransaction : Screens(route = "add_transaction")
    data object Transactions : Screens(route = "transaction/{accountId}")
    data object Account : Screens(route = "account")
}

fun navigateToGraph(bottomBarNavController: NavHostController, graph: NestedNavGraph) {
    bottomBarNavController.navigate(graph.route) {
        popUpTo(bottomBarNavController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}