package app.tinygiants.getalife.presentation.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.tinygiants.getalife.domain.model.SubscriptionStatus
import app.tinygiants.getalife.domain.model.SubscriptionStatus.NeverSubscribed
import app.tinygiants.getalife.domain.model.SubscriptionStatus.PastSubscriber
import app.tinygiants.getalife.presentation.onboarding.welcome.WelcomeScreen
import app.tinygiants.getalife.theme.spacing
import com.superwall.sdk.composable.PaywallComposable
import com.superwall.sdk.paywall.presentation.internal.state.PaywallResult
import com.superwall.sdk.paywall.vc.PaywallView
import com.superwall.sdk.paywall.vc.delegate.PaywallViewCallback

sealed class OnboardingScreens(val route: String) {
    data object Welcome : OnboardingScreens(route = "welcome")
    data object Step1 : OnboardingScreens(route = "step1")
}

sealed class Paywall {
    data object NeverSubscribed : OnboardingScreens(route = "never_subscribed")
    data object PastSubscriber : OnboardingScreens(route = "past_subscriber")
}

@Composable
fun OnboardingNavHost(
    subscriptionStatus: SubscriptionStatus,
    onNavigateToMainApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val startDestination = when (subscriptionStatus) {
        NeverSubscribed -> OnboardingScreens.Welcome.route
        PastSubscriber -> Paywall.PastSubscriber.route
        else -> OnboardingScreens.Welcome.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(OnboardingScreens.Welcome.route) {
            Box(Modifier.fillMaxSize()) {
                Text(text = "Welcome", modifier = Modifier.align(Alignment.Center))
                Column(
                    modifier = Modifier
                        .padding(bottom = spacing.xl)
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { navController.navigate(OnboardingScreens.Step1.route) }
                    ) { Text(text = "Start here") }
                    TextButton(
                        onClick = { navController.navigate(Paywall.NeverSubscribed.route) }
                    ) { Text(text = "Paywall") }
                }
            }
        }

        composable(OnboardingScreens.Step1.route) {
            WelcomeScreen(onNavigateToPaywall = {
                navController.navigate(Paywall.NeverSubscribed.route) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                    launchSingleTop = true
                }
            })
        }
        composable(Paywall.NeverSubscribed.route) {
            PaywallComposable(
                event = "NeverSubscribed",
                delegate = object : PaywallViewCallback {
                    override fun onFinished(paywall: PaywallView, result: PaywallResult, shouldDismiss: Boolean) {
                        if (result is PaywallResult.Purchased) onNavigateToMainApp()
                    }
                })
        }
        composable(Paywall.PastSubscriber.route) {
            PaywallComposable(
                event = "PastSubscriber",
                delegate = object : PaywallViewCallback {
                    override fun onFinished(paywall: PaywallView, result: PaywallResult, shouldDismiss: Boolean) {
                        if (result is PaywallResult.Purchased) onNavigateToMainApp()
                    }
                })
        }
    }
}