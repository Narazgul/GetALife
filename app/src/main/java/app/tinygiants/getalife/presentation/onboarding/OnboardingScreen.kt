package app.tinygiants.getalife.presentation.onboarding

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.tinygiants.getalife.domain.model.SubscriptionStatus
import app.tinygiants.getalife.domain.model.SubscriptionStatus.NeverSubscribed
import app.tinygiants.getalife.domain.model.SubscriptionStatus.PastSubscriber
import app.tinygiants.getalife.presentation.onboarding.screens.BudgetPlanFinish
import app.tinygiants.getalife.presentation.onboarding.screens.CreateBudget
import app.tinygiants.getalife.presentation.onboarding.screens.PaywallNeverSubscribed
import app.tinygiants.getalife.presentation.onboarding.screens.PaywallPastSubscriber
import app.tinygiants.getalife.presentation.onboarding.screens.SocialProof1
import app.tinygiants.getalife.presentation.onboarding.screens.SocialProof2
import app.tinygiants.getalife.presentation.onboarding.screens.Step10GoodStuff
import app.tinygiants.getalife.presentation.onboarding.screens.Step1Name
import app.tinygiants.getalife.presentation.onboarding.screens.Step2HowYouKnowUs
import app.tinygiants.getalife.presentation.onboarding.screens.Step3Transportation
import app.tinygiants.getalife.presentation.onboarding.screens.Step4Debts
import app.tinygiants.getalife.presentation.onboarding.screens.Step5DailyCosts
import app.tinygiants.getalife.presentation.onboarding.screens.Step6Subscription
import app.tinygiants.getalife.presentation.onboarding.screens.Step7UnexpectedExpense
import app.tinygiants.getalife.presentation.onboarding.screens.Step8EmergencySaving
import app.tinygiants.getalife.presentation.onboarding.screens.Step9BeautifulLife
import app.tinygiants.getalife.presentation.onboarding.screens.WelcomeScreen

sealed class OnboardingScreens(val route: String) {
    data object Welcome : OnboardingScreens(route = "welcome")
    data object Step1 : OnboardingScreens(route = "step1")
    data object Step2 : OnboardingScreens(route = "step2")
    data object Step3 : OnboardingScreens(route = "step3")
    data object Step4 : OnboardingScreens(route = "step4")
    data object Step5 : OnboardingScreens(route = "step5")
    data object Step6 : OnboardingScreens(route = "step6")
    data object Step7 : OnboardingScreens(route = "step7")
    data object Step8 : OnboardingScreens(route = "step8")
    data object Step9 : OnboardingScreens(route = "step9")
    data object Step10 : OnboardingScreens(route = "step10")
}

sealed class CreateBudget(val route: String) {
    data object Creation : CreateBudget(route = "creation")
    data object Celebration : CreateBudget(route = "celebration")
}

sealed class SocialProof(val route: String) {
    data object SocialProof1 : SocialProof(route = "socialproof1")
    data object SocialProof2 : SocialProof(route = "socialproof2")
}

sealed class LoginScreens(val route: String) {
    data object LoginOrCreateAccount : LoginScreens(route = "login_or_create")
}

sealed class Paywall(val route: String) {
    data object NeverSubscribed : Paywall(route = "never_subscribed")
    data object PastSubscriber : Paywall(route = "past_subscriber")
}

@Composable
fun OnboardingScreen(
    subscriptionStatus: SubscriptionStatus,
    onNavigateToMainApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val navController = rememberNavController()

    val startDestination = when (subscriptionStatus) {
        NeverSubscribed -> OnboardingScreens.Welcome.route
        PastSubscriber -> Paywall.PastSubscriber.route
        else -> OnboardingScreens.Welcome.route
    }

    val notImplementedToast = Toast.makeText(LocalContext.current, "Not implemented yet", Toast.LENGTH_SHORT)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(OnboardingScreens.Welcome.route) {
            WelcomeScreen(
                onStartClicked = { navController.navigate(OnboardingScreens.Step1.route) },
                onLoginClicked = {
                    notImplementedToast.show()
                    // navController.navigate(LoginScreens.LoginOrCreateAccount.route)
                }
            )
        }
        composable(OnboardingScreens.Step1.route) {
            Step1Name(
                onNameChanged = onboardingViewModel::addName,
                onNextClicked = { navController.navigate(OnboardingScreens.Step2.route) }
            )
        }
        composable(OnboardingScreens.Step2.route) {
            Step2HowYouKnowUs(
                onHowYouKnowUsClicked = onboardingViewModel::addHowYouKnowUs,
                onNextClicked = { navController.navigate(OnboardingScreens.Step3.route) }
            )
        }
        composable(OnboardingScreens.Step3.route) {
            Step3Transportation(
                onTransportationClicked = onboardingViewModel::addTransportations,
                onNextClicked = { navController.navigate(OnboardingScreens.Step4.route) }
            )
        }
        composable(OnboardingScreens.Step4.route) {
            Step4Debts(
                onDebtClicked = onboardingViewModel::addDebts,
                onNextClicked = { navController.navigate(SocialProof.SocialProof1.route) }
            )
        }
        composable(SocialProof.SocialProof1.route) {
            SocialProof1(onNextClicked = { navController.navigate(OnboardingScreens.Step5.route) })
        }
        composable(OnboardingScreens.Step5.route) {
            Step5DailyCosts(
                onDailyCostsClicked = onboardingViewModel::addDailyCosts,
                onNextClicked = { navController.navigate(OnboardingScreens.Step6.route) }
            )
        }
        composable(OnboardingScreens.Step6.route) {
            Step6Subscription(
                onSubscriptionClicked = onboardingViewModel::addSubscriptions,
                onNextClicked = { navController.navigate(OnboardingScreens.Step7.route) }
            )
        }
        composable(OnboardingScreens.Step7.route) {
            Step7UnexpectedExpense(
                onExpensesClicked = onboardingViewModel::addUnexpectedExpenses,
                onNextClicked = { navController.navigate(SocialProof.SocialProof2.route) }
            )
        }
        composable(SocialProof.SocialProof2.route) {
            SocialProof2(onNextClicked = { navController.navigate(OnboardingScreens.Step8.route) })
        }
        composable(OnboardingScreens.Step8.route) {
            Step8EmergencySaving(
                onSavingsClicked = onboardingViewModel::addEmergencySavings,
                onNextClicked = { navController.navigate(OnboardingScreens.Step9.route) }
            )
        }
        composable(OnboardingScreens.Step9.route) {
            Step9BeautifulLife(
                onBeautifulLifeClicked = onboardingViewModel::addBeautifulLife,
                onNextClicked = { navController.navigate(OnboardingScreens.Step10.route) }
            )
        }
        composable(OnboardingScreens.Step10.route) {
            Step10GoodStuff(
                onGoodStuffClicked = onboardingViewModel::addGoodStuff,
                onNextClicked = { navController.navigate(CreateBudget.Creation.route) }
            )
        }
        composable(CreateBudget.Creation.route) {
            CreateBudget(onFinished = { navController.navigate(CreateBudget.Celebration.route) })
        }
        composable(CreateBudget.Celebration.route) {
            BudgetPlanFinish(onNextClicked = { navController.navigate(Paywall.NeverSubscribed.route) })
        }

        composable(Paywall.NeverSubscribed.route) { PaywallNeverSubscribed(onNavigateToMainApp = onNavigateToMainApp) }
        composable(Paywall.PastSubscriber.route) { PaywallPastSubscriber(onNavigateToMainApp = onNavigateToMainApp) }
    }
}