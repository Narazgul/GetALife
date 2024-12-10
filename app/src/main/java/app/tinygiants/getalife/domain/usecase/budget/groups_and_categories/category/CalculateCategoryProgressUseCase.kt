package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Progress
import app.tinygiants.getalife.domain.model.ProgressColor
import app.tinygiants.getalife.domain.model.UserHint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CalculateCategoryProgressUseCase @Inject constructor(@Default private val defaultDispatcher: CoroutineDispatcher) {

    suspend operator fun invoke(category: Category) = withContext(defaultDispatcher) {

        val budgetTarget = category.budgetTarget
        val assignedMoney = category.assignedMoney
        val availableMoney = category.availableMoney

        if (budgetTarget == EmptyMoney()) getProgressWithoutSetBudgetTarget(assignedMoney, availableMoney)
        else getProgressWithSetBudgetTarget(budgetTarget, assignedMoney, availableMoney)

    }

    private fun getProgressWithoutSetBudgetTarget(assignedMoney: Money, availableMoney: Money): Progress {

        return when {
            assignedMoney > EmptyMoney() -> someMoneyHasBeenAssigned(
                assignedMoney = assignedMoney,
                availableMoney = availableMoney
            )

            assignedMoney == EmptyMoney() && availableMoney < EmptyMoney() -> Progress(
                bar1 = 1f,
                bar1Color = ProgressColor.Red,
                userHint = UserHint.AssignMoreOrRemoveSpending(amount = availableMoney.formattedPositiveMoney)
            )

            else -> EmptyProgress()
        }
    }

    private fun getProgressWithSetBudgetTarget(budgetTarget: Money, assignedMoney: Money, availableMoney: Money): Progress {

        return when {
            assignedMoney > EmptyMoney() -> someMoneyHasBeenAssigned(
                budgetTarget = budgetTarget,
                assignedMoney = assignedMoney,
                availableMoney = availableMoney
            )

            assignedMoney == EmptyMoney() -> noMoneyHasBeenAssigned(budgetTarget = budgetTarget, availableMoney = availableMoney)
            else -> EmptyProgress()
        }
    }

    private fun someMoneyHasBeenAssigned(assignedMoney: Money, availableMoney: Money) =
        when {
            availableMoney == assignedMoney -> Progress(
                bar1 = 1f,
                bar1Color = ProgressColor.Green
            )

            availableMoney == EmptyMoney() -> Progress(
                bar1Lite = 1f,
                bar1LiteColor = ProgressColor.GreenLite,
                userHint = UserHint.AllSpent
            )

            availableMoney < EmptyMoney() -> Progress(
                bar1 = 1f,
                bar1Lite = (assignedMoney.toFloat() / (assignedMoney + availableMoney.positiveMoney()).toFloat()),
                bar1Color = ProgressColor.Red,
                bar1LiteColor = ProgressColor.GreenLite,
                userHint = UserHint.SpentMoreThanAvailable(amount = availableMoney.formattedPositiveMoney)
            )

            availableMoney < assignedMoney -> Progress(
                bar1 = 1f,
                bar1Lite = (1 - (availableMoney / assignedMoney).asDouble()).toFloat(),
                bar1Color = ProgressColor.Green,
                bar1LiteColor = ProgressColor.GreenLite,
                userHint = UserHint.Spent(amount = (assignedMoney - availableMoney).formattedPositiveMoney)
            )

            else -> EmptyProgress()
        }

    private fun someMoneyHasBeenAssigned(budgetTarget: Money, assignedMoney: Money, availableMoney: Money): Progress {

        return when {
            assignedMoney < budgetTarget && assignedMoney == EmptyMoney() -> Progress(
                bar1 = 1f,
                bar1Color = ProgressColor.Grey,
                userHint = UserHint.MoreNeedForBudgetTarget(amount = budgetTarget.formattedPositiveMoney)
            )

            assignedMoney < budgetTarget && availableMoney == EmptyMoney() -> Progress(
                bar1 = (assignedMoney / budgetTarget).toFloat(),
                bar1Color = ProgressColor.YellowLite,
                userHint = UserHint.MoreNeedForBudgetTarget(amount = (budgetTarget - assignedMoney).formattedPositiveMoney)
            )

            assignedMoney < budgetTarget
                    && availableMoney < EmptyMoney()
                    && assignedMoney + availableMoney.positiveMoney() > budgetTarget ->
                Progress(
                    bar1 = (budgetTarget.toFloat() / (assignedMoney.toFloat() + availableMoney.positiveMoney().toFloat())),
                    bar1Lite = assignedMoney.toFloat() / (assignedMoney.toFloat() + availableMoney.positiveMoney().toFloat()),
                    bar2 = budgetTarget.toFloat() / (assignedMoney.toFloat() + availableMoney.positiveMoney().toFloat()),
                    bar2Lite = budgetTarget.toFloat() / (assignedMoney.toFloat() + availableMoney.positiveMoney().toFloat()),
                    bar1Color = ProgressColor.Red,
                    bar1LiteColor = ProgressColor.YellowLite,
                    bar2Color = ProgressColor.Red,
                    showColorOnSecondBar = true,
                    userHint = UserHint.AssignMoreOrRemoveSpending(amount = availableMoney.formattedPositiveMoney)
                )

            assignedMoney < budgetTarget && availableMoney < EmptyMoney() -> Progress(
                bar1 = ((assignedMoney.toFloat() + availableMoney.positiveMoney().toFloat()) / budgetTarget.toFloat()),
                bar1Lite = assignedMoney.toFloat() / budgetTarget.toFloat(),
                bar1Color = ProgressColor.Red,
                bar1LiteColor = ProgressColor.YellowLite,
                userHint = UserHint.AssignMoreOrRemoveSpending(amount = availableMoney.formattedPositiveMoney)
            )

            assignedMoney < budgetTarget && availableMoney < assignedMoney -> Progress(
                bar1 = assignedMoney.toFloat() / budgetTarget.toFloat(),
                bar1Lite = ((assignedMoney - availableMoney).toFloat() / budgetTarget.toFloat()),
                bar1Color = ProgressColor.Yellow,
                bar1LiteColor = ProgressColor.YellowLite,
                userHint = UserHint.MoreNeedForBudgetTarget(amount = (budgetTarget - assignedMoney).formattedPositiveMoney)
            )

            assignedMoney < budgetTarget -> Progress(
                bar1 = (availableMoney / budgetTarget).toFloat(),
                bar2 = (availableMoney / budgetTarget).toFloat(),
                bar1Color = ProgressColor.Yellow,
                userHint = UserHint.MoreNeedForBudgetTarget(amount = (budgetTarget - assignedMoney).formattedPositiveMoney)
            )

            assignedMoney == budgetTarget && availableMoney == EmptyMoney() -> Progress(
                bar1 = 1f,
                bar1Color = ProgressColor.GreenLite
            )

            assignedMoney == budgetTarget && availableMoney < EmptyMoney() -> Progress(
                bar1 = budgetTarget.toFloat() / (budgetTarget + availableMoney.positiveMoney()).toFloat(),
                bar2 = budgetTarget.toFloat() / (budgetTarget + availableMoney.positiveMoney()).toFloat(),
                bar2Lite = budgetTarget.toFloat() / (budgetTarget + availableMoney.positiveMoney()).toFloat(),
                bar1Color = ProgressColor.GreenLite,
                bar2Color = ProgressColor.Red,
                showColorOnSecondBar = true,
                userHint = UserHint.AssignMoreOrRemoveSpending(amount = availableMoney.formattedPositiveMoney)
            )

            assignedMoney == budgetTarget && availableMoney < assignedMoney -> Progress(
                bar1 = 1f,
                bar1Lite = (budgetTarget - availableMoney).toFloat() / budgetTarget.toFloat(),
                bar1Color = ProgressColor.Green,
                bar1LiteColor = ProgressColor.GreenLite
            )

            assignedMoney == budgetTarget -> Progress(
                bar1 = 1f,
                bar1Color = ProgressColor.Green,
                userHint = UserHint.FullyFunded
            )

            assignedMoney > budgetTarget && availableMoney == EmptyMoney() -> Progress(
                bar1 = budgetTarget.toFloat() / assignedMoney.toFloat(),
                bar1Lite = budgetTarget.toFloat() / assignedMoney.toFloat(),
                bar1Color = ProgressColor.Green,
                bar1LiteColor = ProgressColor.GreenLite,
                bar2 = budgetTarget.toFloat() / assignedMoney.toFloat(),
                bar2Lite = ((budgetTarget + availableMoney).toFloat() / assignedMoney.toFloat()),
                showColorOnSecondBar = true,
                bar2Color = ProgressColor.PrimaryLite
            )

            assignedMoney > budgetTarget && availableMoney < EmptyMoney() -> Progress(
                bar1 = (budgetTarget.toFloat() / (assignedMoney + availableMoney.positiveMoney()).toFloat()),
                bar2 = (budgetTarget.toFloat() / (assignedMoney + availableMoney.positiveMoney()).toFloat()),
                bar2Lite = (assignedMoney.toFloat() / (assignedMoney + availableMoney.positiveMoney()).toFloat()),
                bar1Color = ProgressColor.GreenLite,
                bar2Color = ProgressColor.PrimaryLite,
                bar2LiteColor = ProgressColor.Red,
                showColorOnSecondBar = true,
                userHint = UserHint.AssignMoreOrRemoveSpending(amount = availableMoney.formattedPositiveMoney)
            )

            assignedMoney > budgetTarget && (assignedMoney - budgetTarget) > availableMoney -> {
                val x = Progress(
                    bar1 = budgetTarget.toFloat() / assignedMoney.toFloat(),
                    bar1Lite = budgetTarget.toFloat() / assignedMoney.toFloat(),
                    bar1Color = ProgressColor.Green,
                    bar1LiteColor = ProgressColor.GreenLite,
                    bar2 = budgetTarget.toFloat() / assignedMoney.toFloat(),
                    bar2Lite = (assignedMoney.toFloat() - availableMoney.toFloat()) / assignedMoney.toFloat(),
                    showColorOnSecondBar = true,
                    bar2Color = ProgressColor.PrimaryLite,
                    bar2LiteColor = ProgressColor.Primary
                )
                return x
            }

            assignedMoney > budgetTarget && availableMoney < assignedMoney -> Progress(
                bar1 = (budgetTarget.toFloat() / assignedMoney.toFloat()),
                bar1Lite = ((assignedMoney - availableMoney).toFloat() / assignedMoney.toFloat()),
                bar1Color = ProgressColor.Green,
                bar1LiteColor = ProgressColor.GreenLite,
                bar2 = (budgetTarget.toFloat() / assignedMoney.toFloat()),
                bar2Lite = (budgetTarget.toFloat() / assignedMoney.toFloat()),
                showColorOnSecondBar = true,
                bar2Color = ProgressColor.Primary,
                userHint = UserHint.ExtraMoney(amount = (assignedMoney - budgetTarget).formattedPositiveMoney)
            )

            assignedMoney > budgetTarget -> Progress(
                bar1 = (budgetTarget.toFloat() / assignedMoney.toFloat()),
                bar2 = (budgetTarget.toFloat() / assignedMoney.toFloat()),
                bar2Lite = (budgetTarget.toFloat() / assignedMoney.toFloat()),
                bar1Color = ProgressColor.Green,
                bar2Color = ProgressColor.Primary,
                showColorOnSecondBar = true,
                userHint = UserHint.ExtraMoney(amount = (assignedMoney - budgetTarget).formattedPositiveMoney)
            )

            else -> Progress(
                bar1 = 1f,
                bar1Color = ProgressColor.Grey,
                userHint = UserHint.MoreNeedForBudgetTarget(amount = budgetTarget.formattedPositiveMoney)
            )
        }
    }

    private fun noMoneyHasBeenAssigned(budgetTarget: Money, availableMoney: Money): Progress {
        return when {
            availableMoney < EmptyMoney() -> Progress(
                bar1 = 1f,
                bar1Color = ProgressColor.Red,
                userHint = UserHint.AssignMoreOrRemoveSpending(amount = availableMoney.formattedPositiveMoney)
            )

            else -> Progress(
                bar1 = 1f,
                bar1Color = ProgressColor.Grey,
                userHint = UserHint.MoreNeedForBudgetTarget(amount = budgetTarget.formattedPositiveMoney)
            )
        }
    }
}