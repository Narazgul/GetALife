package app.tinygiants.getalife.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.tinygiants.getalife.GetALifeNavHost
import app.tinygiants.getalife.NestedNavGraph
import app.tinygiants.getalife.navigateToGraph
import app.tinygiants.getalife.theme.GetALifeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            GetALifeTheme {
                val bottomBarNavController = rememberNavController()

                Scaffold(
                    bottomBar = { BottomNavigation(bottomBarNavController = bottomBarNavController) }
                ) { innerPadding ->

                    val bottomPadding = innerPadding.calculateBottomPadding()
                    GetALifeNavHost(
                        bottomBarNavController = bottomBarNavController,
                        modifier = Modifier.padding(PaddingValues(bottom = bottomPadding)))
                }
            }
        }
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
                onClick = { navigateToGraph(bottomBarNavController = bottomBarNavController, graph = nestedGraph) }
            )
        }
    }
}

val bottomNavigationItems = listOf(
    NestedNavGraph.BudgetNavGraph,
    NestedNavGraph.AddTransactionGraph,
    NestedNavGraph.AccountNavGraph
)