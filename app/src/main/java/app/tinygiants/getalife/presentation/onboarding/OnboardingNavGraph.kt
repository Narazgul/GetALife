package app.tinygiants.getalife.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.tinygiants.getalife.presentation.analytics.TrackScreenView
import app.tinygiants.getalife.presentation.onboarding.screens.PaywallNeverSubscribed
import app.tinygiants.getalife.presentation.onboarding.screens.PlanGenerationScreen
import app.tinygiants.getalife.presentation.onboarding.screens.Step1EmotionalCheck
import app.tinygiants.getalife.presentation.onboarding.screens.Step2Challenges
import app.tinygiants.getalife.presentation.onboarding.screens.Step3Goals
import app.tinygiants.getalife.presentation.onboarding.screens.Step4LifeBasics
import app.tinygiants.getalife.presentation.onboarding.screens.Step5Commitments
import app.tinygiants.getalife.presentation.onboarding.screens.Step6DailyAndLeisure
import app.tinygiants.getalife.presentation.onboarding.screens.Step7LifeGoals
import app.tinygiants.getalife.presentation.onboarding.screens.WelcomeScreen

@Composable
fun OnboardingNavGraph(
    onNavigateToMainApp: (() -> Unit)? = null
) {
    val navController = rememberNavController()
    val viewModel: OnboardingViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = OnboardingScreen.Welcome.route) {
        composable(OnboardingScreen.Welcome.route) {
            TrackScreenView("Onboarding_Welcome")
            WelcomeScreen(
                onStartClicked = { navController.navigate(OnboardingScreen.Step1.route) },
                onLoginClicked = {
                    // TODO: Navigate to login screen or handle existing user login
                    onNavigateToMainApp?.invoke()
                },
                onDebugSkipClicked = onNavigateToMainApp
            )
        }
        composable(OnboardingScreen.Step1.route) {
            TrackScreenView("Onboarding_Step1_EmotionalCheck")
            Step1EmotionalCheck(
                navController = navController,
                onFeelingSelected = {
                    viewModel.setFinancialFeeling(it)
                    navController.navigate(OnboardingScreen.Step2.route)
                }
            )
        }
        composable(OnboardingScreen.Step2.route) {
            TrackScreenView("Onboarding_Step2_Challenges")
            Step2Challenges(
                navController = navController,
                onChallengesSelected = { viewModel.setChallenges(it) },
                onNextClicked = { navController.navigate(OnboardingScreen.Step3.route) }
            )
        }
        composable(OnboardingScreen.Step3.route) {
            TrackScreenView("Onboarding_Step3_Goals")
            Step3Goals(
                navController = navController,
                onGoalsSelected = { viewModel.setTopGoals(it) },
                onNextClicked = { navController.navigate(OnboardingScreen.Step4.route) }
            )
        }
        composable(OnboardingScreen.Step4.route) {
            TrackScreenView("Onboarding_Step4_LifeBasics")
            Step4LifeBasics(
                navController = navController,
                onNextClicked = { housing, transport, pets ->
                    viewModel.setLifeBasics(housing, transport, pets)
                    navController.navigate(OnboardingScreen.Step5.route)
                }
            )
        }
        composable(OnboardingScreen.Step5.route) {
            TrackScreenView("Onboarding_Step5_Commitments")
            Step5Commitments(
                navController = navController,
                onNextClicked = { dependants, insurances, debts ->
                    viewModel.setCommitments(dependants, insurances, debts)
                    navController.navigate(OnboardingScreen.Step6.route)
                }
            )
        }
        composable(OnboardingScreen.Step6.route) {
            TrackScreenView("Onboarding_Step6_DailyAndLeisure")
            Step6DailyAndLeisure(
                navController = navController,
                onNextClicked = { daily, health, shopping, leisure ->
                    viewModel.setDailyAndLeisure(daily, health, shopping, leisure)
                    navController.navigate(OnboardingScreen.Step7.route)
                }
            )
        }
        composable(OnboardingScreen.Step7.route) {
            TrackScreenView("Onboarding_Step7_LifeGoals")
            Step7LifeGoals(
                navController = navController,
                onNextClicked = { goals ->
                    viewModel.setLifeGoals(goals)
                    navController.navigate(OnboardingScreen.PlanGeneration.route)
                }
            )
        }
        composable(OnboardingScreen.PlanGeneration.route) {
            TrackScreenView("Onboarding_PlanGeneration")
            PlanGenerationScreen(
                viewModel = viewModel,
                onPlanGenerated = { navController.navigate(OnboardingScreen.Paywall.route) }
            )
        }
        composable(OnboardingScreen.Paywall.route) {
            TrackScreenView("Onboarding_Paywall")
            PaywallNeverSubscribed(
                onNavigateToMainApp = {
                    // After successful purchase and login, navigate to main app
                    onNavigateToMainApp?.invoke()
                }
            )
        }
    }
}

sealed class OnboardingScreen(val route: String) {
    object Welcome : OnboardingScreen("welcome")
    object Step1 : OnboardingScreen("step1_emotional_check")
    object Step2 : OnboardingScreen("step2_challenges")
    object Step3 : OnboardingScreen("step3_goals")
    object Step4 : OnboardingScreen("step4_life_basics")
    object Step5 : OnboardingScreen("step5_commitments")
    object Step6 : OnboardingScreen("step6_daily_leisure")
    object Step7 : OnboardingScreen("step7_life_goals")
    object PlanGeneration : OnboardingScreen("plan_generation")
    object Paywall : OnboardingScreen("paywall")
}