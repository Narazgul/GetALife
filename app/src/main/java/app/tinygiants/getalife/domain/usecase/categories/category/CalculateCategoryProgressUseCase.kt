package app.tinygiants.getalife.domain.usecase.categories.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Progress
import app.tinygiants.getalife.domain.model.ProgressColor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs

class CalculateCategoryProgressUseCase @Inject constructor(
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(categoryEntity: CategoryEntity): Progress {

        return withContext(defaultDispatcher) {

            val budgetTarget = categoryEntity.budgetTarget
            val assignedMoney = categoryEntity.assignedMoney
            val availableMoney = categoryEntity.availableMoney

            when {
                budgetTarget == null || budgetTarget == 0.0 ->
                    getProgressWithoutSetBudgetTarget(
                        assignedMoney = assignedMoney,
                        availableMoney = availableMoney
                    )

                else -> getProgressWithSetBudgetTarget(
                    budgetTarget = budgetTarget,
                    assignedMoney = assignedMoney,
                    availableMoney = availableMoney
                )
            }
        }
    }

    private fun getProgressWithoutSetBudgetTarget(assignedMoney: Double, availableMoney: Double): Progress {

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
                optionalText = "TODO Assign at least ${abs(availableMoney)}€ to category or remove spending!"
            )
        }

        return when {
            assignedMoney > 0.0 -> someMoneyHasBeenAssigned(assignedMoney = assignedMoney, availableMoney = availableMoney)
            assignedMoney == 0.0 && availableMoney < 0.0 -> noMoneyHasBeenAssigned()
            else -> EmptyProgress()
        }
    }

    private fun getProgressWithSetBudgetTarget(budgetTarget: Double, assignedMoney: Double, availableMoney: Double): Progress {

        fun someMoneyHasBeenAssigned(budgetTarget: Double, assignedMoney: Double, availableMoney: Double): Progress {

            return when {
                assignedMoney < budgetTarget && assignedMoney == 0.0 -> Progress(
                    bar1 = 1f,
                    bar1Color = ProgressColor.Grey,
                    optionalText = "TODO $budgetTarget€ more needed to reach budget target"
                )

                assignedMoney < budgetTarget && availableMoney == 0.0 -> Progress(
                    bar1 = (assignedMoney / budgetTarget).toFloat(),
                    bar1Color = ProgressColor.YellowLite,
                    optionalText = "TODO ${budgetTarget - assignedMoney}€ more needed to reach budget target"
                )

                assignedMoney < budgetTarget && availableMoney < 0.0 && assignedMoney + abs(availableMoney) > budgetTarget -> Progress(
                    bar1 = (budgetTarget / (assignedMoney + abs(availableMoney))).toFloat(),
                    bar1Lite = (assignedMoney / (assignedMoney + abs(availableMoney))).toFloat(),
                    bar2 = (budgetTarget / (assignedMoney + abs(availableMoney))).toFloat(),
                    bar2Lite = (budgetTarget / (assignedMoney + abs(availableMoney))).toFloat(),
                    bar1Color = ProgressColor.Red,
                    bar1LiteColor = ProgressColor.YellowLite,
                    bar2Color = ProgressColor.Red,
                    showColorOnSecondBar = true,
                    optionalText = "TODO Assign at least ${abs(availableMoney)}€ to category or remove spending!"
                )

                assignedMoney < budgetTarget && availableMoney < 0.0 -> Progress(
                    bar1 = ((assignedMoney + abs(availableMoney)) / budgetTarget).toFloat(),
                    bar1Lite = (assignedMoney / budgetTarget).toFloat(),
                    bar1Color = ProgressColor.Red,
                    bar1LiteColor = ProgressColor.YellowLite,
                    optionalText = "TODO Assign at least ${abs(availableMoney)}€ to category or remove spending!"
                )

                assignedMoney < budgetTarget && availableMoney < assignedMoney -> Progress(
                    bar1 = (assignedMoney / budgetTarget).toFloat(),
                    bar1Lite = ((assignedMoney - availableMoney) / budgetTarget).toFloat(),
                    bar1Color = ProgressColor.Yellow,
                    bar1LiteColor = ProgressColor.YellowLite,
                    optionalText = "TODO ${budgetTarget - assignedMoney}€ more needed to reach budget target"
                )

                assignedMoney < budgetTarget -> Progress(
                    bar1 = (availableMoney / budgetTarget).toFloat(),
                    bar2 = (availableMoney / budgetTarget).toFloat(),
                    bar1Color = ProgressColor.Yellow,
                    optionalText = "TODO ${budgetTarget - assignedMoney}€ more needed to reach budget target"
                )

                assignedMoney == budgetTarget && availableMoney == 0.0 -> Progress(
                    bar1 = 1f,
                    bar1Color = ProgressColor.GreenLite
                )

                assignedMoney == budgetTarget && availableMoney < 0.0 -> Progress(
                    bar1 = (budgetTarget / (budgetTarget + abs(availableMoney))).toFloat(),
                    bar2 = (budgetTarget / (budgetTarget + abs(availableMoney))).toFloat(),
                    bar2Lite = (budgetTarget / (budgetTarget + abs(availableMoney))).toFloat(),
                    bar1Color = ProgressColor.GreenLite,
                    bar2Color = ProgressColor.Red,
                    showColorOnSecondBar = true,
                    optionalText = "TODO Assign at least ${abs(availableMoney)}€ to category or remove spending!"
                )

                assignedMoney == budgetTarget && availableMoney < assignedMoney -> Progress(
                    bar1 = 1f,
                    bar1Lite = ((budgetTarget - availableMoney) / budgetTarget).toFloat(),
                    bar1Color = ProgressColor.Green,
                    bar1LiteColor = ProgressColor.GreenLite
                )

                assignedMoney == budgetTarget -> Progress(
                    bar1 = 1f,
                    bar1Color = ProgressColor.Green,
                    optionalText = "TODO Fully funded 👌"
                )

                assignedMoney > budgetTarget && availableMoney < 0.0 -> Progress(
                    bar1 = (budgetTarget / (assignedMoney + abs(availableMoney))).toFloat(),
                    bar2 = (budgetTarget / (assignedMoney + abs(availableMoney))).toFloat(),
                    bar2Lite = (assignedMoney / (assignedMoney + abs(availableMoney))).toFloat(),
                    bar1Color = ProgressColor.GreenLite,
                    bar2Color = ProgressColor.PrimaryLite,
                    bar2LiteColor = ProgressColor.Red,
                    showColorOnSecondBar = true,
                    optionalText = "TODO Assign at least ${abs(availableMoney)}€ to category or remove spending!"
                )

                assignedMoney > budgetTarget && (assignedMoney - budgetTarget) > availableMoney -> Progress(
                    bar1 = (budgetTarget / assignedMoney).toFloat(),
                    bar1Lite = (budgetTarget / assignedMoney).toFloat(),
                    bar1Color = ProgressColor.Green,
                    bar1LiteColor = ProgressColor.GreenLite,
                    bar2 = (budgetTarget / assignedMoney).toFloat(),
                    bar2Lite = ((budgetTarget + availableMoney) / assignedMoney).toFloat(),
                    showColorOnSecondBar = true,
                    bar2Color = ProgressColor.PrimaryLite,
                    bar2LiteColor = ProgressColor.Primary,
                    optionalText = ""
                )

                assignedMoney > budgetTarget && availableMoney < assignedMoney -> Progress(
                    bar1 = (budgetTarget / assignedMoney).toFloat(),
                    bar1Lite = ((assignedMoney - availableMoney) / assignedMoney).toFloat(),
                    bar1Color = ProgressColor.Green,
                    bar1LiteColor = ProgressColor.GreenLite,
                    bar2 = (budgetTarget / assignedMoney).toFloat(),
                    bar2Lite = (budgetTarget / assignedMoney).toFloat(),
                    showColorOnSecondBar = true,
                    bar2Color = ProgressColor.Primary,
                    optionalText = "TODO Enjoy your ${assignedMoney - budgetTarget}€ extra"
                )

                assignedMoney > budgetTarget -> Progress(
                    bar1 = (budgetTarget / assignedMoney).toFloat(),
                    bar2 = (budgetTarget / assignedMoney).toFloat(),
                    bar2Lite = (budgetTarget / assignedMoney).toFloat(),
                    bar1Color = ProgressColor.Green,
                    bar2Color = ProgressColor.Primary,
                    showColorOnSecondBar = true,
                    optionalText = "TODO Enjoy your ${assignedMoney - budgetTarget}€ extra"
                )

                else -> Progress(
                    bar1 = 1f,
                    bar1Color = ProgressColor.Grey,
                    optionalText = "TODO $budgetTarget€ more needed to reach budget target"
                )
            }
        }

        fun noMoneyHasBeenAssigned(): Progress {
            return when {
                availableMoney < 0.0 -> Progress(
                    bar1 = 1f,
                    bar1Color = ProgressColor.Red,
                    optionalText = "TODO Assign at least ${abs(availableMoney)}€ to category or remove spending!"
                )

                else -> Progress(
                    bar1 = 1f,
                    bar1Color = ProgressColor.Grey,
                    optionalText = "TODO $budgetTarget€ more needed to reach budget target"
                )
            }
        }

        return when {
            assignedMoney > 0.0 -> someMoneyHasBeenAssigned(
                budgetTarget = budgetTarget,
                assignedMoney = assignedMoney,
                availableMoney = availableMoney
            )

            assignedMoney == 0.0 -> noMoneyHasBeenAssigned()
            else -> EmptyProgress()
        }
    }
}