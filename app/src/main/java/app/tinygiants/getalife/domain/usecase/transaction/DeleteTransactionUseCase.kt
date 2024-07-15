package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(transaction: Transaction) {

        if (transaction.account == null) return

        withContext(defaultDispatcher) {
            val transactionEntity = TransactionEntity(
                id = transaction.id,
                accountId = transaction.account.id,
                categoryId = transaction.category?.id,
                amount = transaction.amount.value,
                transactionPartner = transaction.transactionPartner,
                transactionDirection = transaction.direction,
                description = transaction.description,
                timestamp = transaction.timestamp
            )
            val accountEntity = transaction.account.run {
                AccountEntity(
                    id = id,
                    name = name,
                    balance = balance.value,
                    type = type,
                    listPosition = listPosition
                )
            }
            val categoryEntity = transaction.category?.run {
                CategoryEntity(
                    id = id,
                    headerId = headerId,
                    emoji = emoji,
                    name = name,
                    budgetTarget = budgetTarget.value,
                    budgetPurpose = budgetPurpose,
                    assignedMoney = assignedMoney.value,
                    availableMoney = availableMoney.value,
                    optionalText = optionalText,
                    listPosition = listPosition,
                    isInitialCategory = isInitialCategory
                )
            }

            repository.deleteTransaction(
                transaction = transactionEntity,
                account = accountEntity,
                category = categoryEntity
            )
        }
    }
}