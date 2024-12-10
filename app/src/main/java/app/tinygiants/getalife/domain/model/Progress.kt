package app.tinygiants.getalife.domain.model

typealias EmptyProgress = Progress

data class Progress(

    val showColorOnSecondBar: Boolean = false,
    val userHint: UserHint = UserHint.NoHint,

    val bar1: Float = 0f,
    val bar1Lite: Float = 0f,
    val bar2: Float = 0f,
    val bar2Lite: Float = 0f,

    val bar1Color: ProgressColor = ProgressColor.Unknown,
    val bar1LiteColor: ProgressColor = ProgressColor.Unknown,
    val bar2Color: ProgressColor = ProgressColor.Unknown,
    val bar2LiteColor: ProgressColor = ProgressColor.Unknown
)

enum class ProgressColor {
    Unknown,
    Grey,
    Green, Yellow, Red, Primary,
    GreenLite, YellowLite, PrimaryLite
}

sealed class UserHint {
    data object NoHint : UserHint()
    data object AllSpent : UserHint()
    data object FullyFunded : UserHint()
    data class Spent(val amount: String) : UserHint()
    data class ExtraMoney(val amount: String) : UserHint()
    data class MoreNeedForBudgetTarget(val amount: String) : UserHint()
    data class AssignMoreOrRemoveSpending(val amount: String) : UserHint()
    data class SpentMoreThanAvailable(val amount: String) : UserHint()
}