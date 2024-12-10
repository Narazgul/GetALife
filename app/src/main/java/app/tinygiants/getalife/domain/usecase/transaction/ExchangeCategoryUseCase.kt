package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject

class ExchangeCategoryUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(transaction: Transaction, oldCategory: Category?) {

        if (transaction.category == null) return

        withContext(defaultDispatcher) {

            updateTransaction(transaction = transaction)
            updateOldCategory(category = oldCategory, amount = transaction.amount)
            updateNewCategory(category = transaction.category, amount = transaction.amount)
        }
    }

    private suspend fun updateTransaction(transaction: Transaction) {

        val updatedTransaction = transaction.copy(updatedAt = Clock.System.now())

        transactionRepository.updateTransaction(updatedTransaction)
    }

    private suspend fun updateOldCategory(category: Category?, amount: Money) {
        if (category == null) return

        val updatedAvailableMoney = category.availableMoney - amount

        val updatedCategory = category.copy(
            availableMoney = updatedAvailableMoney,
            updatedAt = Clock.System.now()
        )

        categoryRepository.updateCategory(updatedCategory)
    }

    private suspend fun updateNewCategory(category: Category, amount: Money) {

        val updatedAvailableMoney = category.availableMoney + amount

        val updatedCategory = category.copy(
            availableMoney = updatedAvailableMoney,
            updatedAt = Clock.System.now()
        )

        categoryRepository.updateCategory(updatedCategory)
    }
}