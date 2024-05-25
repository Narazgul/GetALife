package app.tinygiants.getalife.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import app.tinygiants.getalife.GetALifeNavHost
import app.tinygiants.getalife.Screens
import app.tinygiants.getalife.theme.GetALifeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GetALifeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Box {
                        Scaffold(
                            bottomBar = { BottomNavigation(navController = navController) }
                        ) { innerPadding ->
                            GetALifeNavHost(
                                navController = navController,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        //CustomFabAdd()
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigation(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        bottomNavigationItems.forEach { screen ->
            NavigationBarItem(
                icon = {
                    if (screen !is Screens.Transaction)
                        Icon(
                            painter = painterResource(id = screen.iconId),
                            contentDescription = "${screen.label} Icon"
                        )
                },
                label = {
                    Text(
                        text = if (screen != Screens.Transaction) stringResource(id = screen.label) else "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                selected = currentDestination?.hierarchy?.any { navDestination ->
                    navDestination.route == screen.route
                } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

val bottomNavigationItems = listOf(
    Screens.Budget,
    //Screens.Transaction,
    Screens.Account
)