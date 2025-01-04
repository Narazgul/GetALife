package app.tinygiants.getalife.presentation.main_app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.tinygiants.getalife.NestedNavGraph

@Composable
fun MainScreen() {
    val bottomBarNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigation(bottomBarNavController = bottomBarNavController) }
    ) { innerPadding ->
        MainNavHost(
            bottomBarNavController = bottomBarNavController,
            modifier = Modifier.padding(PaddingValues(bottom = innerPadding.calculateBottomPadding()))
        )
    }
}

@Composable
fun BottomNavigation(bottomBarNavController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by bottomBarNavController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        bottomNavigationItems.forEach { nestedGraph ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = nestedGraph.iconId),
                        contentDescription = "${nestedGraph.label}"
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = nestedGraph.label),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                selected = currentDestination?.hierarchy?.any { navDestination -> navDestination.route == nestedGraph.route } == true,
                onClick = {
                    navigateToBottomNavigationGraph(
                        bottomBarNavController = bottomBarNavController,
                        graph = nestedGraph
                    )
                }
            )
        }
    }
}

val bottomNavigationItems = listOf(
    NestedNavGraph.BudgetNavGraph,
    NestedNavGraph.AddTransactionGraph,
    NestedNavGraph.AccountNavGraph
)

fun navigateToBottomNavigationGraph(bottomBarNavController: NavHostController, graph: NestedNavGraph) {
    bottomBarNavController.navigate(graph.route) {
        popUpTo(bottomBarNavController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}