package app.tinygiants.getalife.domain.model.onboarding

data class OnboardingAnswers(
    val financialFeeling: FinancialFeeling? = null,
    val challenges: List<FinancialChallenge> = emptyList(),
    val topGoals: List<SavingGoal> = emptyList(),
    val housingSituation: HousingSituation? = null,
    val transportation: List<TransportationType> = emptyList(),
    val pets: List<Pet> = emptyList(),
    val dependants: List<FinancialDependant> = emptyList(),
    val insurances: List<InsuranceType> = emptyList(),
    val debts: List<app.tinygiants.getalife.domain.model.onbaordinganswers.Debt> = emptyList(),
    val dailyExpenses: List<DailyExpense> = emptyList(),
    val healthExpenses: List<HealthExpense> = emptyList(),
    val shoppingExpenses: List<ShoppingExpense> = emptyList(),
    val leisureExpenses: List<LeisureExpense> = emptyList(),
    val lifeGoals: List<LifeGoal> = emptyList()
)