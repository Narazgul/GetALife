package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        amount: Money,
        direction: TransactionDirection,
        accountId: Long,
        category: Category?,
        transactionPartner: String,
        description: String,
        isAccountCreation: Boolean = false
    ) {
        val account = accountRepository.getAccount(accountId)
        val transformedAmount = transformAmount(direction = direction, amount = amount)

        withContext(defaultDispatcher) {

            addTransaction(
                accountId = accountId,
                category = category,
                amount = transformedAmount,
                direction = direction,
                transactionPartner = transactionPartner,
                description = description,
                addTransaction = transactionRepository::addTransaction
            )

            updateAccount(
                account = account,
                direction = direction,
                amount = transformedAmount,
                isAccountCreation = isAccountCreation,
                updateAccount = accountRepository::updateAccount,
            )

            updateCategory(
                category = category,
                absoluteAmountValue = transformedAmount,
                direction = direction,
                updateCategory = categoryRepository::updateCategory,
            )
        }
    }

    private fun transformAmount(direction: TransactionDirection, amount: Money) =
        when (direction) {
            TransactionDirection.Inflow -> abs(amount.value)
            TransactionDirection.Outflow -> -abs(amount.value)
            TransactionDirection.Unknown -> amount.value
        }

    private suspend fun addTransaction(
        accountId: Long,
        category: Category?,
        amount: Double,
        direction: TransactionDirection,
        transactionPartner: String,
        description: String,
        addTransaction: suspend (TransactionEntity) -> Unit
    ) {

        val currentTime = Clock.System.now()
        val transactionEntity = TransactionEntity(
            id = Random.nextLong(),
            accountId = accountId,
            categoryId = category?.id,
            amount = amount,
            transactionDirection = direction,
            transactionPartner = transactionPartner,
            description = description,
            updatedAt = currentTime,
            createdAt = currentTime
        )

        addTransaction(transactionEntity)
    }

    private suspend fun updateAccount(
        account: AccountEntity,
        direction: TransactionDirection,
        amount: Double,
        isAccountCreation: Boolean,
        updateAccount: suspend (AccountEntity) -> Unit
    ) {
        if (direction == TransactionDirection.Unknown) return
        if (isAccountCreation) return

        val updatedAccountBalance =
            if (direction == TransactionDirection.Inflow) account.balance + abs(amount)
            else account.balance - abs(amount)
        val updatedAccount = account.copy(balance = updatedAccountBalance, updatedAt = Clock.System.now())

        updateAccount(updatedAccount)
    }

    private suspend fun updateCategory(
        category: Category?,
        absoluteAmountValue: Double,
        direction: TransactionDirection,
        updateCategory: suspend (CategoryEntity) -> Unit
    ) {

        if (category == null) return
        if (direction == TransactionDirection.Unknown) return

        val updatedAvailableMoney =
            if (direction == TransactionDirection.Inflow) category.availableMoney.value + absoluteAmountValue
            else category.availableMoney.value - absoluteAmountValue

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