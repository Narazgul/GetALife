package app.tinygiants.getalife.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.model.onboarding.DailyExpense
import app.tinygiants.getalife.domain.model.onboarding.FinancialChallenge
import app.tinygiants.getalife.domain.model.onboarding.FinancialDependant
import app.tinygiants.getalife.domain.model.onboarding.FinancialFeeling
import app.tinygiants.getalife.domain.model.onboarding.HealthExpense
import app.tinygiants.getalife.domain.model.onboarding.HousingSituation
import app.tinygiants.getalife.domain.model.onboarding.InsuranceType
import app.tinygiants.getalife.domain.model.onboarding.LeisureExpense
import app.tinygiants.getalife.domain.model.onboarding.LifeGoal
import app.tinygiants.getalife.domain.model.onboarding.OnboardingAnswers
import app.tinygiants.getalife.domain.model.onboarding.Pet
import app.tinygiants.getalife.domain.model.onboarding.SavingGoal
import app.tinygiants.getalife.domain.model.onboarding.ShoppingExpense
import app.tinygiants.getalife.domain.model.onboarding.TransportationType
import app.tinygiants.getalife.domain.model.onboarding.onbaordinganswers.Debt
import app.tinygiants.getalife.domain.usecase.analytics.TrackScreenViewUseCase
import app.tinygiants.getalife.domain.usecase.onboarding.CreateBudgetFromOnboardingAnswersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val trackScreenView: TrackScreenViewUseCase,
    private val createBudgetFromOnboardingAnswers: CreateBudgetFromOnboardingAnswersUseCase
) : ViewModel() {

    private val _answers = MutableStateFlow(OnboardingAnswers())

    fun setFinancialFeeling(feeling: FinancialFeeling) {
        _answers.update { it.copy(financialFeeling = feeling) }
    }

    fun setChallenges(challenges: List<FinancialChallenge>) {
        _answers.update { it.copy(challenges = challenges) }
    }

    fun setTopGoals(goals: List<SavingGoal>) {
        _answers.update { it.copy(topGoals = goals) }
    }

    fun setLifeBasics(housing: HousingSituation?, transport: List<TransportationType>, pets: List<Pet>) {
        _answers.update {
            it.copy(
                housingSituation = housing,
                transportation = transport,
                pets = pets
            )
        }
    }

    fun setCommitments(dependants: List<FinancialDependant>, insurances: List<InsuranceType>, debts: List<Debt>) {
        _answers.update {
            it.copy(
                dependants = dependants,
                insurances = insurances,
                debts = debts
            )
        }
    }

    fun setDailyAndLeisure(daily: List<DailyExpense>, health: List<HealthExpense>, shopping: List<ShoppingExpense>, leisure: List<LeisureExpense>) {
        _answers.update {
            it.copy(
                dailyExpenses = daily,
                healthExpenses = health,
                shoppingExpenses = shopping,
                leisureExpenses = leisure
            )
        }
    }

    fun setLifeGoals(goals: List<LifeGoal>) {
        _answers.update { it.copy(lifeGoals = goals) }
    }

    fun trackScreen(screenName: String) {
        trackScreenView(screenName, "Onboarding")
    }

    fun generateBudgetFromAnswers() = viewModelScope.launch {
        createBudgetFromOnboardingAnswers(_answers.value)
    }
}
