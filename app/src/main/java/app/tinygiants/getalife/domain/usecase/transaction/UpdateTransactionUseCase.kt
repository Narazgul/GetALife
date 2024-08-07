package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
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

            repository.updateTransaction(transaction = transactionEntity)
        }
    }
}
