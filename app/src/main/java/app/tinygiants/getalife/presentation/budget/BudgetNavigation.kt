package app.tinygiants.getalife.presentation.budget

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.tinygiants.getalife.NestedNavigation
import app.tinygiants.getalife.Screens

fun NavGraphBuilder.budgetGraph() {
    navigation(
        startDestination = Screens.Budget.route,
        route = NestedNavigation.BudgetNavigation.route
    ) {
        composable(Screens.Budget.route) {
            val viewModel: BudgetViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            BudgetScreen(uiState)
        }
    }
}