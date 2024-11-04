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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.math.abs

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(transaction: Transaction) {

        val transformedAmount = transformAmount(direction = transaction.transactionDirection, amount = transaction.amount)
        val transactionWithTransformedAmount = transaction.copy(amount = Money(transformedAmount))

        if (transactionWithTransformedAmount.account == null) return

        withContext(defaultDispatcher) {

            deleteTransaction(
                transaction = transactionWithTransformedAmount,
                deleteTransaction = transactionRepository::deleteTransaction
            )

            updateAccount(
                transaction = transactionWithTransformedAmount,
                updateAccount = accountRepository::updateAccount
            )

            updateCategory(
                transaction = transactionWithTransformedAmount,
                updateCategory = categoryRepository::updateCategory
            )
        }
    }

    private fun transformAmount(direction: TransactionDirection, amount: Money) =
        when (direction) {
            TransactionDirection.Inflow -> -abs(amount.value)
            TransactionDirection.Outflow -> abs(amount.value)
            TransactionDirection.Unknown -> amount.value
        }

    private suspend fun deleteTransaction(transaction: Transaction, deleteTransaction: suspend (TransactionEntity) -> Unit) {

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

        deleteTransaction(transactionEntity)
    }

    private suspend fun updateAccount(
        transaction: Transaction,
        updateAccount: suspend (AccountEntity) -> Unit
    ) {
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

        updateAccount(updatedAccountEntity)
    }

    private suspend fun updateCategory(
        transaction: Transaction,
        updateCategory: suspend (CategoryEntity) -> Unit
    ) {

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

        updateCategory(updatedCategoryEntity)
    }
}