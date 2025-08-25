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
import app.tinygiants.getalife.presentation.main_app.bulk_categorization.BulkCategorizationScreen
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransaction
import app.tinygiants.getalife.presentation.main_app.transaction.transactions.TransactionScreen

// Navigation Constants
object NavigationConstants {
    const val ARG_ACCOUNT_ID = "accountId"

    object Routes {
        const val BUDGET_GRAPH = "budgetNavGraph"
        const val ADD_TRANSACTION_GRAPH = "addTransactionGraph"
        const val ACCOUNT_GRAPH = "accountNavGraph"

        const val BUDGET_SCREEN = "budget"
        const val ADD_TRANSACTION_SCREEN = "add_transaction"
        const val ACCOUNT_SCREEN = "account"
        const val BULK_CATEGORIZATION_SCREEN = "bulk_categorization"
        const val TRANSACTIONS_SCREEN = "transaction/{$ARG_ACCOUNT_ID}"
    }

    object Labels {
        val BUDGET = R.string.budget
        val TRANSACTION = R.string.transaction
        val ACCOUNT = R.string.account
    }

    object Icons {
        val BUDGET = R.drawable.ic_dashboard
        val TRANSACTION = R.drawable.ic_add
        val ACCOUNT = R.drawable.ic_account
    }
}

// Navigation Extensions
fun NavHostController.navigateToTransactions(accountId: Long) {
    navigate("transaction/$accountId")
}

sealed class MainNavGraph(@StringRes val label: Int, val iconId: Int, val route: String) {
    data object Budget : MainNavGraph(
        label = NavigationConstants.Labels.BUDGET,
        iconId = NavigationConstants.Icons.BUDGET,
        route = NavigationConstants.Routes.BUDGET_GRAPH
    )

    data object AddTransaction : MainNavGraph(
        label = NavigationConstants.Labels.TRANSACTION,
        iconId = NavigationConstants.Icons.TRANSACTION,
        route = NavigationConstants.Routes.ADD_TRANSACTION_GRAPH
    )

    data object Account : MainNavGraph(
        label = NavigationConstants.Labels.ACCOUNT,
        iconId = NavigationConstants.Icons.ACCOUNT,
        route = NavigationConstants.Routes.ACCOUNT_GRAPH
    )
}

sealed class MainScreens(val route: String) {
    data object Budget : MainScreens(route = NavigationConstants.Routes.BUDGET_SCREEN)
    data object AddTransaction : MainScreens(route = NavigationConstants.Routes.ADD_TRANSACTION_SCREEN)
    data object Transactions : MainScreens(route = NavigationConstants.Routes.TRANSACTIONS_SCREEN)
    data object Account : MainScreens(route = NavigationConstants.Routes.ACCOUNT_SCREEN)
    data object BulkCategorization : MainScreens(route = NavigationConstants.Routes.BULK_CATEGORIZATION_SCREEN)
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
            composable(MainScreens.Budget.route) {
                BudgetScreen()
            }
            composable(MainScreens.BulkCategorization.route) {
                BulkCategorizationScreen(
                    onNavigateUp = {
                        budgetNavController.navigateUp()
                    }
                )
            }
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
            composable(MainScreens.AddTransaction.route) {
                AddTransaction()
            }
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
                AccountScreen(
                    onNavigateToTransactionScreen = { accountId ->
                        accountNavController.navigateToTransactions(accountId)
                    }
                )
            }
            composable(
                route = MainScreens.Transactions.route,
                arguments = listOf(
                    navArgument(NavigationConstants.ARG_ACCOUNT_ID) {
                        type = NavType.LongType
                    }
                )
            ) {
                TransactionScreen(
                    onNavigateUp = {
                        accountNavController.navigateUp()
                    }
                )
            }
        }
    }
}