package app.tinygiants.getalife

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.tinygiants.getalife.domain.model.SubscriptionStatus
import app.tinygiants.getalife.domain.model.SubscriptionStatus.Active
import app.tinygiants.getalife.domain.usecase.subscription.GetUserSubscriptionStatusUseCase
import app.tinygiants.getalife.presentation.main_app.MainScreen
import app.tinygiants.getalife.presentation.onboarding.OnboardingScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class Path(val route: String) {
    data object Onboarding : Path(route = "onboardingPath")
    data object Main : Path(route = "budgetPath")
}

@Composable
fun GetALifeNavHost() {
    val viewModel: GetALifeNavHostViewModel = hiltViewModel()
    val subscriptionStatus by viewModel.subscriptionStatus.collectAsStateWithLifecycle()

    val navController = rememberNavController()

    val onNavigateToMainAppGraph = {
        navController.navigate(Path.Main.route) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        // Hardcoded for testing purposes - keep as requested
        startDestination = Path.Main.route
    ) {
        composable(route = Path.Onboarding.route) {
            OnboardingScreen(
                subscriptionStatus = subscriptionStatus,
                onNavigateToMainApp = onNavigateToMainAppGraph
            )
        }

        composable(route = Path.Main.route) {
            MainScreen()
        }
    }
}

@HiltViewModel
class GetALifeNavHostViewModel @Inject constructor(
    getSubscription: GetUserSubscriptionStatusUseCase
) : ViewModel() {

    val subscriptionStatus: StateFlow<SubscriptionStatus> = getSubscription()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SubscriptionStatus.Unknown
        )

    // This can be used later when dynamic start destination is needed
    val dynamicStartDestination: StateFlow<String> = subscriptionStatus.map { status ->
        when (status) {
            Active -> Path.Main.route
            else -> Path.Onboarding.route
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Path.Onboarding.route
    )
}