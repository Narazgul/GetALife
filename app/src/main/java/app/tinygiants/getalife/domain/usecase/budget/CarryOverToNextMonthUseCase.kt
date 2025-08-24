package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import kotlinx.datetime.YearMonth
import javax.inject.Inject
import kotlin.time.Clock

class CarryOverToNextMonthUseCase @Inject constructor(
    private val statusRepository: CategoryMonthlyStatusRepository
) {
    suspend operator fun invoke(fromMonth: YearMonth, toMonth: YearMonth) {
        val statusesFromMonth = statusRepository.getStatusForMonth(fromMonth)

        statusesFromMonth.forEach { status ->
            if (!status.isCarryOverEnabled) return@forEach

            val existingStatus = statusRepository.getStatus(status.category.id, toMonth)
            Clock.System.now()

            if (existingStatus != null) {
                val updatedStatus = existingStatus.copy(
                    assignedAmount = existingStatus.assignedAmount + status.assignedAmount
                )
                statusRepository.saveStatus(updatedStatus, toMonth)
            } else {
                val newStatus = CategoryMonthlyStatus(
                    category = status.category,
                    assignedAmount = status.assignedAmount,
                    isCarryOverEnabled = true,
                    spentAmount = status.spentAmount,
                    availableAmount = status.assignedAmount,
                    progress = status.progress,
                    suggestedAmount = status.suggestedAmount,
                    targetContribution = status.targetContribution // Carry over existing target contribution
                )
                statusRepository.saveStatus(newStatus, toMonth)
            }
        }
    }
}