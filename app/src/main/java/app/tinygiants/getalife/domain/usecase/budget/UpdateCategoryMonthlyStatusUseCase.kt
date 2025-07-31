package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import kotlinx.datetime.YearMonth
import javax.inject.Inject
import kotlin.time.Clock

class UpdateCategoryMonthlyStatusUseCase @Inject constructor(
    private val statusRepository: CategoryMonthlyStatusRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(
        categoryId: Long,
        yearMonth: YearMonth,
        newAssignedAmount: Money
    ) {
        Clock.System.now()
        val existingStatus = statusRepository.getStatus(categoryId, yearMonth)

        val status = if (existingStatus != null) {
            existingStatus.copy(
                assignedAmount = newAssignedAmount
                // availableAmount and spentAmount will be calculated reactively by GetBudgetForMonthUseCase
            )
        } else {
            // Create new status - we need to get the category
            val category = categoryRepository.getCategory(categoryId)
                ?: throw IllegalArgumentException("Category with id $categoryId not found")

            CategoryMonthlyStatus(
                category = category,
                assignedAmount = newAssignedAmount,
                isCarryOverEnabled = true,
                spentAmount = EmptyMoney(), // Will be calculated reactively
                availableAmount = EmptyMoney(), // Will be calculated reactively
                progress = EmptyProgress(), // Will be calculated reactively
                suggestedAmount = null
            )
        }

        statusRepository.saveStatus(status, yearMonth)
    }
}