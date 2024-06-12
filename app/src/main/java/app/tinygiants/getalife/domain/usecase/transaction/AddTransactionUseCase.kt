package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
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
        amount: Money?,
        direction: TransactionDirection?,
        transactionPartner: String?,
        description: String?,
        account: Account?,
        category: Category?
    ) {

        withContext(defaultDispatcher) {

            if (amount == null) return@withContext
            if (direction == null) return@withContext
            if (direction == TransactionDirection.Outflow && category == null) return@withContext
            if (account == null) return@withContext

            val newBalance =
                if (direction == TransactionDirection.Inflow) account.balance.value + amount.value
                else account.balance.value - amount.value

            val transactionEntity = TransactionEntity(
                id = Random.nextLong(),
                accountId = account.id,
                categoryId = category?.id,
                amount = amount.value,
                transactionType = direction,
                transactionPartner = transactionPartner ?: "",
                description = description ?: "",
                timestamp = Timestamp(System.currentTimeMillis())
            )
            val accountEntity = AccountEntity(
                id = account.id,
                name = account.name,
                balance = newBalance,
                type = account.type,
                listPosition = account.listPosition
            )
            val categoryEntity = if (direction == TransactionDirection.Outflow && category != null) {

                val newAvailableAmount = category.assignedMoney.value - amount.value

                CategoryEntity(
                    id = category.id,
                    headerId = category.headerId,
                    emoji = category.emoji,
                    name = category.name,
                    budgetTarget = category.budgetTarget.value,
                    budgetPurpose = category.budgetPurpose,
                    assignedMoney = category.assignedMoney.value,
                    availableMoney = newAvailableAmount,
                    optionalText = category.optionalText,
                    listPosition = category.listPosition,
                    isInitialCategory = category.isInitialCategory
                )
            } else null

            transactionRepository.addTransaction(
                transaction = transactionEntity,
                account = accountEntity,
                category = categoryEntity
            )
        }
    }
}