package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
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
        if (transaction.account == null) return

        val transactionEntity = transaction.run {
            TransactionEntity(
                id = id,
                accountId = account!!.id,
                categoryId = category?.id,
                amount = amount.value,
                transactionPartner = transactionPartner,
                transactionDirection = transactionDirection,
                description = description,
                updatedAt = Clock.System.now(),
                createdAt = createdAt
            )
        }

        transactionRepository.updateTransaction(transactionEntity)
    }

    private suspend fun updateOldCategory(category: Category?, amount: Money) {
        if (category == null) return

        val updatedAvailableMoney = category.availableMoney - amount

        val oldCategoryEntity = category.run {
            CategoryEntity(
                id = id,
                groupId = groupId,
                emoji = emoji,
                name = name,
                budgetTarget = budgetTarget.value,
                assignedMoney = assignedMoney.value,
                availableMoney = updatedAvailableMoney.value,
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = Clock.System.now(),
                createdAt = createdAt,
            )
        }

        categoryRepository.updateCategory(oldCategoryEntity)
    }

    private suspend fun updateNewCategory(category: Category, amount: Money) {

        val updatedAvailableMoney = category.availableMoney + amount

        val oldCategoryEntity = category.run {
            CategoryEntity(
                id = id,
                groupId = groupId,
                emoji = emoji,
                name = name,
                budgetTarget = budgetTarget.value,
                assignedMoney = assignedMoney.value,
                availableMoney = updatedAvailableMoney.value,
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = Clock.System.now(),
                createdAt = createdAt,
            )
        }

        categoryRepository.updateCategory(oldCategoryEntity)
    }
}