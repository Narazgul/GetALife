package app.tinygiants.getalife.domain.model.onbaordinganswers

enum class Debt(val displayName: String) {
    CreditCard("Kreditkarte"),
    StudentLoan("Studienkredit"),
    CarLoan("Autokredit"),
    Mortgage("Hauskredit"),
    PersonalLoan("Persönliche Kredite"),
    NoDebt("Keine Schulden")
}