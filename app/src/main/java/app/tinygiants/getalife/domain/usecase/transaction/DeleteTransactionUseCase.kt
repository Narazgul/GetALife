package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.budget.UpdateAssignableMoneyUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.math.abs

class DeleteTransactionUseCase @Inject constructor(
    private val updateAssignableMoney: UpdateAssignableMoneyUseCase,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(transaction: Transaction) {
        if (transaction.account == null) return

        val transformedAmount = transformAmount(direction = transaction.transactionDirection, amount = transaction.amount)
        val transactionWithTransformedAmount = transaction.copy(amount = Money(transformedAmount))

        withContext(defaultDispatcher) {
            deleteTransaction(transaction = transactionWithTransformedAmount)
            updateAccount(transaction = transactionWithTransformedAmount)
            updateCategory(transaction = transactionWithTransformedAmount)
            reduceAssignableMoney(transaction = transactionWithTransformedAmount)
        }
    }

    private fun transformAmount(direction: TransactionDirection, amount: Money) =
        when (direction) {
            TransactionDirection.Inflow -> -abs(amount.value)
            TransactionDirection.Outflow -> abs(amount.value)
            TransactionDirection.Unknown -> amount.value
        }

    private suspend fun deleteTransaction(transaction: Transaction) {

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

        transactionRepository.deleteTransaction(transactionEntity)
    }

    private suspend fun updateAccount(transaction: Transaction) {
        if (transaction.account == null) return

        val account = transaction.account
        val updatedAccountBalance = account.balance + transaction.amount
        val updatedAccountEntity = account.run {
            AccountEntity(
                id = id,
                name = name,
                balance = updatedAccountBalance.value,
                type = type,
                listPosition = listPosition,
                updatedAt = Clock.System.now(),
                createdAt = createdAt
            )
        }

        accountRepository.updateAccount(updatedAccountEntity)
    }

    private suspend fun updateCategory(transaction: Transaction) {

        if (transaction.category == null) return
        if (transaction.transactionDirection == TransactionDirection.Unknown) return

        val category = transaction.category
        val updatedAvailableMoney = category.availableMoney.value + transaction.amount.value

        val updatedCategoryEntity = category.run {
            CategoryEntity(
                id = id,
                groupId = groupId,
                emoji = emoji,
                name = name,
                budgetTarget = budgetTarget.value,
                assignedMoney = assignedMoney.value,
                availableMoney = updatedAvailableMoney,
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = Clock.System.now(),
                createdAt = createdAt
            )
        }

        categoryRepository.updateCategory(updatedCategoryEntity)
    }

    private suspend fun reduceAssignableMoney(transaction: Transaction) {
        if (transaction.transactionDirection != TransactionDirection.Inflow) return

        updateAssignableMoney(newAmount = transaction.amount)
    }
}