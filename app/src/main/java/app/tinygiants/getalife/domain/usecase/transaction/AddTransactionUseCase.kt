package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.BudgetRepository
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
    private val budgetRepository: BudgetRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        amount: Money,
        direction: TransactionDirection,
        accountId: Long,
        category: Category?,
        transactionPartner: String,
        description: String
    ) {
        val account = accountRepository.getAccount(accountId) ?: return
        if (direction == TransactionDirection.Unknown) return

        withContext(defaultDispatcher) {

            val transformedAmount = transformAmount(direction = direction, amount = amount)

            addTransaction(
                accountId = accountId,
                category = category,
                amount = transformedAmount,
                direction = direction,
                transactionPartner = transactionPartner,
                description = description
            )

            updateAccount(
                account = account,
                amount = transformedAmount
            )

            updateCategory(
                category = category,
                amount = transformedAmount
            )

            updateReadyToAssignCategory(direction = direction, amount = amount.value)
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
        description: String
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

        transactionRepository.addTransaction(transactionEntity)
    }

    private suspend fun updateAccount(account: AccountEntity, amount: Double) {

        val updatedAccountBalance = account.balance + amount
        val updatedAccount = account.copy(balance = updatedAccountBalance, updatedAt = Clock.System.now())

        accountRepository.updateAccount(updatedAccount)
    }

    private suspend fun updateCategory(
        category: Category?,
        amount: Double
    ) {
        if (category == null) return

        updateUserCategory(category = category, amount = amount)
    }

    private suspend fun updateReadyToAssignCategory(direction: TransactionDirection, amount: Double) {
        if (direction != TransactionDirection.Inflow) return

        val readyToAssignCategory = budgetRepository.getBudget()

        val updatedReadyToAssignCategory = readyToAssignCategory.copy(
            readyToAssign =  readyToAssignCategory.readyToAssign + amount,
            updatedAt = Clock.System.now()
        )

        budgetRepository.updateBudget(updatedReadyToAssignCategory)
    }

    private suspend fun updateUserCategory(category: Category, amount: Double) {
        val updatedAvailableMoney = category.availableMoney.value + amount

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
}