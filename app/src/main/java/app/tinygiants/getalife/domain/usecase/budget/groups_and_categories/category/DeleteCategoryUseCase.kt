package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category

import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.DeleteCategoryStatus.CategoryHasTransactionsException
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.DeleteCategoryStatus.SuccessfullyDeleted
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed class DeleteCategoryStatus {
    data object SuccessfullyDeleted : DeleteCategoryStatus()
    data class CategoryHasTransactionsException(override val message: String) : Exception(message)
}

class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) {

    suspend operator fun invoke(category: Category): Result<DeleteCategoryStatus> {

        val transactions = transactionRepository.getTransactionsByCategoryFlow(categoryId = category.id).first()
        if (transactions.isNotEmpty())
            return Result.failure(CategoryHasTransactionsException(message = "Category still has transactions"))

        categoryRepository.deleteCategory(category = category)
        return Result.success(SuccessfullyDeleted)
    }
}