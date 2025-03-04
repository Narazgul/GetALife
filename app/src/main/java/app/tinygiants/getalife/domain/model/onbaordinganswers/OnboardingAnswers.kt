package app.tinygiants.getalife.domain.model.onbaordinganswers

data class OnboardingAnswers(
    val name: String,
    val howYouKnowUs: HowYouKnowUs,
    val transportations: List<Transportation>,
    val debts: List<Debt>,
    val dailyCosts: List<DailyCost>,
    val subscriptions: List<Subscription>,
    val unexpectedExpenses: List<UnexpectedExpense>,
    val emergencySavings: List<EmergencySaving>,
    val beautifulLife: List<BeautifulLife>,
    val goodStuff: List<GoodStuff>
)