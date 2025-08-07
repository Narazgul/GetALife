package app.tinygiants.getalife.domain.model.onboarding

enum class FinancialFeeling(val displayName: String) {
    OVERWHELMED("Etwas überfordert"),
    COULD_BE_BETTER("Es geht so, aber es könnte besser sein"),
    CLUELESS("Ich habe keine Ahnung, wo mein Geld hingeht"),
    OPTIMIZING("Eigentlich ganz gut, ich will nur optimieren")
}

enum class FinancialChallenge(val displayName: String) {
    UNEXPECTED_BILLS("Unerwartete Rechnungen"),
    HIDDEN_SUBSCRIPTIONS("Versteckte Abos & Verträge"),
    SPONTANEOUS_PURCHASES("Spontankäufe & \"To-Go\"-Ausgaben"),
    HIGH_FIXED_COSTS("Zu hohe Fixkosten"),
    DEBT_REPAYMENT("Schulden & Ratenzahlungen"),
    NO_RETIREMENT_PLAN("Kein System für die Altersvorsorge")
}

enum class SavingGoal(val displayName: String) {
    BECOME_DEBT_FREE("Endlich schuldenfrei sein"),
    BUILD_EMERGENCY_FUND("Ein finanzielles Sicherheitspolster aufbauen"),
    SAVE_FOR_VACATION("Für einen großen Urlaub sparen"),
    SAVE_FOR_PROPERTY("Für eine Immobilie sparen"),
    MORE_FINANCIAL_FREEDOM("Mehr finanzielle Freiheit im Alltag"),
    INVEST_AND_GROW_WEALTH("Investieren & Vermögen aufbauen")
}

enum class HousingSituation(val displayName: String) {
    RENT("Miete"),
    OWN_WITH_MORTGAGE("Eigentum (mit Kredit)"),
    OWN_PAID_OFF("Eigentum (abbezahlt)"),
    SHARED_LIVING("Wohngemeinschaft / Bei den Eltern")
}

enum class Pet(val displayName: String) {
    DOG("Hund"),
    CAT("Katze"),
    SMALL_ANIMAL("Kleintier"),
    NONE("Kein Haustier")
}

enum class TransportationType(val displayName: String) {
    CAR("Eigenes Auto"),
    MOTORCYCLE("Motorrad / Roller"),
    PUBLIC_TRANSPORT("Öffentliche Verkehrsmittel"),
    BIKE_OR_FOOT("Fahrrad / Zu Fuß")
}

enum class FinancialDependant(val displayName: String) {
    ONLY_MYSELF("Nur für mich"),
    PARTNER("Partner/in"),
    CHILDREN("Kind/er")
}

enum class InsuranceType(val displayName: String) {
    LIABILITY("Haftpflicht"),
    HOUSEHOLD("Hausrat"),
    DISABILITY("Berufsunfähigkeit"),
    LEGAL("Rechtsschutz")
}

enum class DailyExpense(val displayName: String) {
    GROCERIES("Wocheneinkauf"),
    TAKEAWAY("Essen gehen / Lieferservice"),
    COFFEE_SNACKS("Kaffee & Snacks")
}

enum class HealthExpense(val displayName: String) {
    GYM_SPORTS("Fitnessstudio / Sportverein"),
    PHARMACY("Medikamente / Apotheke"),
    DRUGSTORE("Drogerieartikel")
}

enum class ShoppingExpense(val displayName: String) {
    CLOTHING_SHOES("Kleidung & Schuhe"),
    TECH_ELECTRONICS("Technik & Elektronik"),
    BOOKS_MEDIA("Bücher & Medien")
}

enum class LeisureExpense(val displayName: String) {
    STREAMING("Streaming (Netflix etc.)"),
    EVENTS("Kino / Konzerte"),
    GAMING("Gaming"),
    HOBBIES("Hobbies"),
    TRAVEL("Reisen & Ausflüge")
}

enum class LifeGoal(val displayName: String) {
    DREAM_VACATION("Der nächste Traum-Urlaub"),
    NEW_CAR("Ein neues Auto / Motorrad"),
    WEDDING("Meine Hochzeit"),
    CHILD("Sparen für ein Kind"),
    SPECIAL_GIFT("Ein besonderes Geschenk"),
    MAJOR_PURCHASE("Eine größere Anschaffung"),
    EMERGENCY_FUND("Mein Notgroschen")
}