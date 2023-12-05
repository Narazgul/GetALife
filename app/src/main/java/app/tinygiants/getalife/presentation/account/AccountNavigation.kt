package app.tinygiants.getalife.presentation.account

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.tinygiants.getalife.NestedNavigation
import app.tinygiants.getalife.Screens

fun NavGraphBuilder.accountGraph() {
    navigation(
        startDestination = Screens.Account.route,
        route = NestedNavigation.AccountNavigation.route
    ) {
        composable(Screens.Account.route) { AccountScreen() }
    }
}