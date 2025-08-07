package app.tinygiants.getalife.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.onbaordinganswers.Debt
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
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.usecase.OnboardingPrefsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Clock

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val categoryRepository: CategoryRepository,
    private val onboardingPrefsUseCase: OnboardingPrefsUseCase
) : ViewModel() {

    private val _answers = MutableStateFlow(OnboardingAnswers())
    val answers = _answers.asStateFlow()

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

    fun generateBudgetFromAnswers() = viewModelScope.launch {
        val answers = _answers.value
        var groupPosition = 0
        var categoryPosition = 0

        suspend fun createGroupWithCategories(groupName: String, categoryNames: List<String>) {
            if (categoryNames.isEmpty()) return

            val group = Group(
                id = Random.nextLong(),
                name = groupName,
                sumOfAvailableMoney = EmptyMoney(),
                listPosition = groupPosition++,
                isExpanded = true
            )
            groupRepository.addGroup(group)

            categoryNames.forEach { categoryName ->
                val now = Clock.System.now()
                val category = Category(
                    id = Random.nextLong(),
                    groupId = group.id,
                    emoji = "⚪️",
                    name = categoryName,
                    budgetTarget = EmptyMoney(),
                    monthlyTargetAmount = null,
                    targetMonthsRemaining = null,
                    listPosition = categoryPosition++,
                    isInitialCategory = false,
                    updatedAt = now,
                    createdAt = now
                )
                categoryRepository.addCategory(category)
            }
        }

        // --- Generate Budget based on Answers ---

        // Group: Wohnen & Haushalt
        val housingCategories = mutableListOf<String>()
        answers.housingSituation?.let { housingCategories.add(it.displayName) }
        if (answers.pets.any { it != Pet.NONE }) {
            housingCategories.add("Haustiere")
        }
        createGroupWithCategories("Wohnen & Haushalt", housingCategories)

        // Group: Mobilität
        createGroupWithCategories("Mobilität", answers.transportation.map { it.displayName })

        // Group: Verpflichtungen
        val commitmentCategories = mutableListOf<String>()
        commitmentCategories.addAll(answers.insurances.map { it.displayName })
        commitmentCategories.addAll(answers.debts.filter { it != Debt.NoDebt }.map { it.displayName })
        createGroupWithCategories("Verpflichtungen", commitmentCategories)

        // Group: Tägliche Ausgaben
        val dailyCategories = mutableListOf<String>()
        dailyCategories.addAll(answers.dailyExpenses.map { it.displayName })
        dailyCategories.addAll(answers.healthExpenses.map { it.displayName })
        dailyCategories.addAll(answers.shoppingExpenses.map { it.displayName })
        createGroupWithCategories("Tägliche Ausgaben", dailyCategories)

        // Group: Freizeit & Unterhaltung
        createGroupWithCategories("Freizeit & Unterhaltung", answers.leisureExpenses.map { it.displayName })

        // Group: Sparziele
        createGroupWithCategories("Sparziele", answers.lifeGoals.map { it.displayName })

        // Mark onboarding as completed
        onboardingPrefsUseCase.markOnboardingCompleted()
    }
}
