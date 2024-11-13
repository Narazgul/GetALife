package app.tinygiants.getalife.domain.usecase.categories.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Progress
import app.tinygiants.getalife.domain.model.ProgressColor
import javax.inject.Inject
import kotlin.math.abs

class CalculateCategoryProgressUseCase @Inject constructor() {

    operator fun invoke(categoryEntity: CategoryEntity): Progress {
        val budgetTarget = categoryEntity.budgetTarget
        val assignedMoney = categoryEntity.assignedMoney
        val availableMoney = categoryEntity.availableMoney

        return when {
            budgetTarget == null || budgetTarget == 0.0 ->
                getProgressWithoutBudgetTarget(
                    assignedMoney = assignedMoney,
                    availableMoney = availableMoney
                )

            else -> getProgressWithBudgetTarget(budgetTarget = budgetTarget)

        }
    }

    private fun getProgressWithoutBudgetTarget(assignedMoney: Double, availableMoney: Double): Progress {

        fun someMoneyHasBeenAssigned(assignedMoney: Double, availableMoney: Double): Progress {
            return when {
                availableMoney == assignedMoney -> Progress(
                    bar1 = 1f,
                    bar1Color = ProgressColor.Green
                )

                availableMoney == 0.0 -> Progress(
                    bar1Lite = 1f,
                    bar1LiteColor = ProgressColor.GreenLite,
                    optionalText = "TODO all spent"
                )

                availableMoney < 0.0 -> Progress(
                    bar1 = 1f,
                    bar1Lite = (assignedMoney / (assignedMoney + abs(availableMoney))).toFloat(),
                    bar1Color = ProgressColor.Red,
                    bar1LiteColor = ProgressColor.GreenLite,
                    optionalText = "TODO Spent ${abs(availableMoney)}€ more than available"
                )

                availableMoney < assignedMoney -> Progress(
                    bar1 = 1f,
                    bar1Lite = (1 - availableMoney / assignedMoney).toFloat(),
                    bar1Color = ProgressColor.Green,
                    bar1LiteColor = ProgressColor.GreenLite,
                    optionalText = "TODO ${assignedMoney - availableMoney}€ spent"
                )
                else -> EmptyProgress()
            }
        }

        fun noMoneyHasBeenAssigned(): Progress {
            return Progress(
                bar1 = 1f,
                bar1Color = ProgressColor.Red,
                optionalText = "TODO Assign money to category or remove spending!"
            )
        }

        return when {
            assignedMoney > 0.0 -> someMoneyHasBeenAssigned(assignedMoney = assignedMoney, availableMoney = availableMoney)
            assignedMoney == 0.0 && availableMoney < 0.0 -> noMoneyHasBeenAssigned()
            else -> EmptyProgress()
        }
    }

    private fun getProgressWithBudgetTarget(budgetTarget: Double): Progress {
        return EmptyProgress()
    }
}