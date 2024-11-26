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

class UpdateTransactionUseCase @Inject constructor(
    private val updateAssignableMoney: UpdateAssignableMoneyUseCase,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(transaction: Transaction) {

        if (transaction.account == null) return


        withContext(defaultDispatcher) {

            val transformedAmount = transformAmount(direction = transaction.transactionDirection, amount = transaction.amount)
            val transformedTransaction = transaction.copy(amount = Money(transformedAmount))

            adjustAssignableMoney(transaction = transformedTransaction)
            updateAccount(transaction = transformedTransaction)
            updateCategory(transaction = transformedTransaction)
            updateTransaction(transaction = transformedTransaction)
        }
    }

    private suspend fun updateTransaction(transaction: Transaction) {

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

        transactionRepository.updateTransaction(transactionEntity)
    }

    private suspend fun updateAccount(transaction: Transaction) {

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

        accountRepository.updateAccount(updatedAccount)
    }

    private suspend fun updateCategory(transaction: Transaction) {

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
                budgetTarget = budgetTarget.value,
                assignedMoney = assignedMoney.value,
                availableMoney = updatedAvailableMoney,
                listPosition = listPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = Clock.System.now(),
                createdAt = createdAt
            )
        }

        categoryRepository.updateCategory(updatedCategory)
    }

    private suspend fun adjustAssignableMoney(transaction: Transaction) {
        if (transaction.transactionDirection != TransactionDirection.Inflow) return

        val updatedBalance = calculateAssignableMoneyDifference(transaction = transaction)

        updateAssignableMoney(Money(updatedBalance))
    }

    private suspend fun calculateAssignableMoneyDifference(transaction: Transaction): Double {
        val previousTransactionAmount =
            transactionRepository.getTransaction(transactionId = transaction.id)?.amount
        return transaction.amount.value - (previousTransactionAmount ?: 0.0)
    }

    private suspend fun calculateUpdatedBalance(
        transaction: Transaction,
        toBeUpdatedBalance: Double
    ): Double {

        val previousTransactionAmount =
            transactionRepository.getTransaction(transactionId = transaction.id)?.amount ?: return toBeUpdatedBalance
        val differenceBetweenCurrentAndPreviousTransactionAmount = transaction.amount.value - previousTransactionAmount

        return toBeUpdatedBalance + differenceBetweenCurrentAndPreviousTransactionAmount
    }

    private fun transformAmount(direction: TransactionDirection, amount: Money) =
        when (direction) {
            TransactionDirection.Inflow -> abs(amount.value)
            TransactionDirection.Outflow -> -abs(amount.value)
            TransactionDirection.Unknown -> amount.value
        }
}