package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Progress
import app.tinygiants.getalife.domain.model.UserHint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CalculateCategoryProgressUseCase @Inject constructor(@Default private val defaultDispatcher: CoroutineDispatcher) {

    suspend operator fun invoke(category: Category, status: CategoryMonthlyStatus?, spentAmount: Money): Progress =
        withContext(defaultDispatcher) {
            val assignedAmount = status?.assignedAmount?.asDouble() ?: 0.0
            val spentAmountValue = spentAmount.asDouble()

            val userHint = when {
                assignedAmount == 0.0 -> UserHint.NoHint
                spentAmountValue == 0.0 && assignedAmount > 0 -> UserHint.FullyFunded
                spentAmountValue > 0 && spentAmountValue <= assignedAmount ->
                    UserHint.Spent(Money(spentAmountValue).formattedPositiveMoney)

                spentAmountValue > assignedAmount -> {
                    val overspent = Money(spentAmountValue - assignedAmount)
                    UserHint.SpentMoreThanAvailable(overspent.formattedPositiveMoney)
                }
                else -> UserHint.NoHint
            }

            val progressRatio = if (assignedAmount > 0.0) {
                (spentAmountValue / assignedAmount).coerceAtMost(1.0).toFloat()
            } else {
                0f
            }

            Progress(
                bar1 = progressRatio,
                bar1Color = when {
                    spentAmountValue == 0.0 -> app.tinygiants.getalife.domain.model.ProgressColor.Grey
                    spentAmountValue <= assignedAmount -> app.tinygiants.getalife.domain.model.ProgressColor.Green
                    else -> app.tinygiants.getalife.domain.model.ProgressColor.Red
                },
                userHint = userHint
            )
        }
}