package app.tinygiants.getalife.domain.model

typealias EmptyProgress = Progress

data class Progress(
    val budgetTargetState: BudgetTargetState = BudgetTargetState.Unknown,
    val budgetTargetProgress: Float = 0f,
    val spentProgress: Float = 0f,
    val overspentProgress: Float = 0f,
)

enum class BudgetTargetState { Unknown, NoTargetSet, TargetSet }