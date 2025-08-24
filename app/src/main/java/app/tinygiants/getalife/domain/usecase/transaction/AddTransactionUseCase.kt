package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.budget.RecalculateCategoryMonthlyStatusUseCase
import app.tinygiants.getalife.domain.usecase.transaction.credit_card.EnsureCreditCardPaymentCategoryUseCase
import app.tinygiants.getalife.domain.usecase.transaction.recurrence.CalculateRecurrenceDatesUseCase
import app.tinygiants.getalife.domain.usecase.transaction.validation.ValidateTransactionDataUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Clock

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val groupRepository: GroupRepository,
    private val validateTransactionData: ValidateTransactionDataUseCase,
    private val ensureCreditCardPaymentCategory: EnsureCreditCardPaymentCategoryUseCase,
    private val calculateRecurrenceDates: CalculateRecurrenceDatesUseCase,
    private val recalculateCategoryMonthlyStatus: RecalculateCategoryMonthlyStatusUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher,
    private val categoryMonthlyStatusRepository: CategoryMonthlyStatusRepository
) {

    suspend operator fun invoke(
        accountId: Long,
        categoryId: Long? = null,
        category: Category? = null,
        amount: Money,
        direction: TransactionDirection,
        transactionPartner: String,
        description: String,
        dateOfTransaction: kotlin.time.Instant = Clock.System.now(),
        recurrenceFrequency: RecurrenceFrequency? = null
    ) = withContext(defaultDispatcher) {

        // 1. Validate input data
        val validationData = ValidateTransactionDataUseCase.TransactionValidationData(
            accountId = accountId,
            amount = amount,
            direction = direction,
            transactionPartner = transactionPartner,
            description = description,
            recurrenceFrequency = recurrenceFrequency
        )
        validateTransactionData(validationData)

        // 2. Get account and resolve category
        val account = getAccount(accountId)
        val resolvedCategory = resolveCategory(categoryId, category)

        // 3. Create and save transaction
        val transaction = createTransaction(
            account = account,
            category = resolvedCategory,
            amount = amount,
            direction = direction,
            transactionPartner = transactionPartner,
            description = description,
            dateOfTransaction = dateOfTransaction,
            recurrenceFrequency = recurrenceFrequency
        )

        transactionRepository.addTransaction(transaction)

        // 4. Update account balance
        updateAccountBalance(account, transaction.amount)

        // 5. Handle credit card specific logic
        if (account.type == AccountType.CreditCard && direction == TransactionDirection.Outflow) {
            ensureCreditCardPaymentCategory(account)
        }

        // 6. Trigger budget recalculation
        resolvedCategory?.let { cat ->
            triggerBudgetRecalculation(cat, dateOfTransaction)
        }
    }

    private suspend fun getAccount(accountId: Long): Account {
        return accountRepository.getAccount(accountId)
            ?: throw IllegalArgumentException("Account with ID $accountId not found")
    }

    private suspend fun resolveCategory(categoryId: Long?, category: Category?): Category? {
        return category ?: categoryId?.let { categoryRepository.getCategory(it) }
    }

    private suspend fun createTransaction(
        account: Account,
        category: Category?,
        amount: Money,
        direction: TransactionDirection,
        transactionPartner: String,
        description: String,
        dateOfTransaction: kotlin.time.Instant,
        recurrenceFrequency: RecurrenceFrequency?
    ): Transaction {
        val transformedAmount = transformAmount(direction, amount)
        val now = Clock.System.now()

        val nextPaymentDate = if (recurrenceFrequency != null) {
            calculateRecurrenceDates(dateOfTransaction, recurrenceFrequency)
        } else null

        return Transaction(
            id = abs(Random.nextLong()),
            amount = transformedAmount,
            account = account,
            category = category,
            transactionPartner = transactionPartner,
            transactionDirection = direction,
            description = description,
            dateOfTransaction = dateOfTransaction,
            updatedAt = now,
            createdAt = now,
            isRecurring = recurrenceFrequency != null,
            recurrenceFrequency = recurrenceFrequency,
            nextPaymentDate = nextPaymentDate
        )
    }

    private suspend fun updateAccountBalance(account: Account, amount: Money) {
        val updatedAccount = account.copy(
            balance = account.balance + amount,
            updatedAt = Clock.System.now()
        )
        accountRepository.updateAccount(updatedAccount)
    }

    private suspend fun triggerBudgetRecalculation(category: Category, dateOfTransaction: kotlin.time.Instant) {
        val transactionMonth = dateOfTransaction.toLocalDateTime(TimeZone.currentSystemDefault())
        val yearMonth = kotlinx.datetime.YearMonth(transactionMonth.year, transactionMonth.month)
        recalculateCategoryMonthlyStatus(category.id, yearMonth)
    }

    private fun transformAmount(direction: TransactionDirection, amount: Money): Money {
        return when (direction) {
            TransactionDirection.Inflow -> Money(abs(amount.asDouble()))
            TransactionDirection.Outflow -> Money(-abs(amount.asDouble()))
            TransactionDirection.AccountTransfer -> amount
            TransactionDirection.CreditCardPayment -> Money(abs(amount.asDouble()))
            TransactionDirection.Unknown -> amount
        }
    }
}