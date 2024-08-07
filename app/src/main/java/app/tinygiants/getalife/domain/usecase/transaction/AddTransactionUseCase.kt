package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.sql.Timestamp
import javax.inject.Inject
import kotlin.random.Random

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        amount: Money,
        direction: TransactionDirection,
        accountId: Long,
        category: Category?,
        transactionPartner: String?,
        description: String?,
    ) {
        withContext(defaultDispatcher) {

            val transactionEntity = TransactionEntity(
                id = Random.nextLong(),
                accountId = accountId,
                categoryId = category?.id,
                amount = amount.value,
                transactionDirection = direction,
                transactionPartner = transactionPartner ?: "",
                description = description ?: "",
                timestamp = Timestamp(System.currentTimeMillis())
            )

            transactionRepository.addTransaction(transaction = transactionEntity)
        }
    }
}