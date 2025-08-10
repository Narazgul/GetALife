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
import app.tinygiants.getalife.domain.usecase.GetCurrentBudgetUseCase
import app.tinygiants.getalife.domain.usecase.OnboardingPrefsUseCase
import app.tinygiants.getalife.domain.usecase.subscription.GetUserSubscriptionStatusUseCase
import app.tinygiants.getalife.presentation.main_app.MainScreen
import app.tinygiants.getalife.presentation.onboarding.OnboardingNavGraph
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class Path(val route: String) {
    data object Onboarding : Path(route = "onboardingPath")
    data object Main : Path(route = "budgetPath")
}

@Composable
fun GetALifeNavHost() {
    val viewModel: GetALifeNavHostViewModel = hiltViewModel()
    val startDestination by viewModel.dynamicStartDestination.collectAsStateWithLifecycle()

    val navController = rememberNavController()

    val onNavigateToMainAppGraph = {
        navController.navigate(Path.Main.route) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Path.Onboarding.route) {
            OnboardingNavGraph(
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
    getSubscription: GetUserSubscriptionStatusUseCase,
    getCurrentBudget: GetCurrentBudgetUseCase,
    onboardingPrefsUseCase: OnboardingPrefsUseCase
) : ViewModel() {

    // Keep live subscription status running in background for updates
    val subscriptionStatus: StateFlow<SubscriptionStatus> = getSubscription()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SubscriptionStatus.Unknown
        )

    // Use cached status for instant startup navigation
    val dynamicStartDestination: StateFlow<String> = combine(
        getSubscription.getCachedStatus(), // Use cached status for instant startup
        getCurrentBudget.currentBudgetIdOrDefaultFlow,
        onboardingPrefsUseCase.isOnboardingCompletedFlow
    ) { subscription, budgetId, onboardingCompleted ->
        when {
            // If user has active subscription and budget exists, go to main app
            subscription == Active -> Path.Main.route

            // If onboarding was completed and budget exists, go to main app  
            onboardingCompleted -> Path.Main.route

            // Otherwise, show onboarding (no more loading state needed)
            else -> Path.Onboarding.route
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Path.Onboarding.route // Back to onboarding as initial - will be updated instantly
    )
}