package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(transaction: Transaction) {

        if (transaction.account == null) return

        withContext(defaultDispatcher) {

            updateAccount(transaction = transaction, updateAccount = accountRepository::updateAccount)
            updateCategory(transaction = transaction, updateCategory = categoryRepository::updateCategory)
            updateTransaction(transaction = transaction, updateTransaction = transactionRepository::updateTransaction)
        }
    }

    private suspend fun updateTransaction(transaction: Transaction, updateTransaction: suspend (TransactionEntity) -> Unit) {

        val transactionEntity = TransactionEntity(
            id = transaction.id,
            accountId = transaction.account!!.id,
            categoryId = transaction.category?.id,
            amount = transaction.amount.value,
            transactionPartner = transaction.transactionPartner,
            transactionDirection = transaction.transactionDirection,
            description = transaction.description,
            updatedAt = transaction.updatedAt,
            createdAt = transaction.createdAt
        )

        updateTransaction(transactionEntity)
    }

    private suspend fun updateAccount(transaction: Transaction, updateAccount: suspend (AccountEntity) -> Unit) {

        val updatedBalance = calculateUpdatedBalance(
            transaction = transaction,
            toBeUpdatedBalance = transaction.account!!.balance.value
        )

        val updatedAccount = transaction.account.run {
            AccountEntity(
                id = id,
                name = name,
                balance = updatedBalance,
                type = type,
                listPosition = listPosition,
                updatedAt = Clock.System.now(),
                createdAt = createdAt,
            )
        }

        updateAccount(updatedAccount)
    }

    private suspend fun updateCategory(transaction: Transaction, updateCategory: suspend (CategoryEntity) -> Unit) {

        if (transaction.category == null) return

        val updatedAvailableMoney = calculateUpdatedBalance(
            transaction = transaction,
            toBeUpdatedBalance = transaction.category.availableMoney.value
        )
        val updatedCategory = transaction.category.run {
            CategoryEntity(
                id = id,
                groupId = groupId,
                emoji = emoji,
                name = name,
                budgetTarget = budgetTarget?.value,
                budgetPurpose = budgetPurpose,
                assignedMoney = assignedMoney.value,
                availableMoney = updatedAvailableMoney,
                optionalText = optionalText,
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = Clock.System.now(),
                createdAt = createdAt
            )
        }

        updateCategory(updatedCategory)
    }

    private suspend fun calculateUpdatedBalance(
        transaction: Transaction,
        toBeUpdatedBalance: Double
    ): Double {

        val previousTransactionAmount =
            transactionRepository.getTransaction(transactionId = transaction.id)?.amount ?: return toBeUpdatedBalance
        val differenceBetweenCurrentAndPreviousTransactionAmount = transaction.amount.value - previousTransactionAmount

        return when {
            differenceBetweenCurrentAndPreviousTransactionAmount == 0.0 -> toBeUpdatedBalance
            else -> toBeUpdatedBalance - differenceBetweenCurrentAndPreviousTransactionAmount
        }
    }
}