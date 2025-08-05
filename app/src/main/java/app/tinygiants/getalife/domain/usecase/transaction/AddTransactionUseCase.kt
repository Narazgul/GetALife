package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
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
        if (direction == TransactionDirection.Unknown) return@withContext

        val account = accountRepository.getAccount(accountId) ?: return@withContext
        val categoryFromDb = category ?: categoryId?.let { categoryRepository.getCategory(it) }

        val transformedAmount = transformAmount(direction = direction, amount = amount)

        val transaction = createTransaction(
            amount = transformedAmount,
            account = account,
            category = categoryFromDb,
            transactionPartner = transactionPartner,
            direction = direction,
            description = description,
            dateOfTransaction = dateOfTransaction,
            recurrenceFrequency = recurrenceFrequency
        )

        // Validate recurring payment data integrity
        if (transaction.isRecurring && transaction.recurrenceFrequency == null) {
            throw IllegalArgumentException("Recurring transactions must have a recurrence frequency")
        }

        transactionRepository.addTransaction(transaction = transaction)

        // Handle credit card transactions:
        if (account.type == AccountType.CreditCard && direction == TransactionDirection.Outflow) {
            // If there is no payment category yet, create it, so budget recalculation can act correctly.
            findOrCreateCreditCardPaymentCategory(account)
            // Just update the account/debt; invisible budget movement will be handled on recalc.
            updateAccount(account = account, amount = transformedAmount)
        } else {
            updateAccount(account = account, amount = transformedAmount)
        }

        // Trigger recalculation for the affected category and month
        categoryFromDb?.let { cat ->
            val transactionMonth = dateOfTransaction.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            val yearMonth = kotlinx.datetime.YearMonth(transactionMonth.year, transactionMonth.month)
            recalculateCategoryMonthlyStatus(cat.id, yearMonth)
        }
    }

    private suspend fun findOrCreateCreditCardPaymentCategory(creditCardAccount: Account): Category {
        // Look for existing credit card payment category for this account
        val existingCategory = categoryRepository.getCreditCardPaymentCategory(creditCardAccount.id)
        return existingCategory ?: createCreditCardPaymentCategory(creditCardAccount)
    }

    private suspend fun createCreditCardPaymentCategory(creditCardAccount: Account): Category {
        // Create a new credit card payment category
        val categoryId = abs(Random.nextLong())
        val now = Clock.System.now()

        // Find or create Credit Card Payments group
        val groupId = findOrCreateCreditCardPaymentsGroup()

        val category = Category(
            id = categoryId,
            groupId = groupId,
            emoji = "💳",
            name = "${creditCardAccount.name} Payment",
            budgetTarget = Money(0.0),
            monthlyTargetAmount = null,
            targetMonthsRemaining = null,
            listPosition = 0,
            isInitialCategory = false,
            linkedAccountId = creditCardAccount.id,
            updatedAt = now,
            createdAt = now
        )

        categoryRepository.addCategory(category)

        // Create initial monthly status so it shows up in the budget immediately
        val currentMonth = Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        val yearMonth = kotlinx.datetime.YearMonth(currentMonth.year, currentMonth.month)

        val initialStatus = app.tinygiants.getalife.domain.model.CategoryMonthlyStatus(
            category = category,
            assignedAmount = Money(0.0),
            isCarryOverEnabled = true,
            spentAmount = Money(0.0),
            availableAmount = Money(0.0),
            progress = app.tinygiants.getalife.domain.model.EmptyProgress(),
            suggestedAmount = null
        )
        categoryMonthlyStatusRepository.saveStatus(initialStatus, yearMonth)

        // Force a small delay to ensure database consistency
        delay(100)

        return category
    }

    private suspend fun findOrCreateCreditCardPaymentsGroup(): Long {
        val groupName = "Kreditkarten"
        val existingGroup = groupRepository.getGroupByName(groupName)
        if (existingGroup != null) {
            return existingGroup.id
        }

        // Create new Credit Card Payments group at the top (position 0)
        val groups = groupRepository.getGroupsFlow().first()
        val groupId = abs(Random.nextLong())

        // Move all existing groups down by 1 position
        groups.forEach { group ->
            val updatedGroup = group.copy(listPosition = group.listPosition + 1)
            groupRepository.updateGroup(updatedGroup)
        }

        val creditCardPaymentsGroup = Group(
            id = groupId,
            name = groupName,
            sumOfAvailableMoney = Money(0.0),
            listPosition = 0, // Put at the top
            isExpanded = true // Always expanded to be visible
        )

        groupRepository.addGroup(creditCardPaymentsGroup)

        // Force a small delay to ensure database consistency
        delay(100)

        // Verify the group was created
        val verifyGroup = groupRepository.getGroupByName(groupName)

        return groupId
    }

    private fun transformAmount(direction: TransactionDirection, amount: Money): Money {
        return when (direction) {
            TransactionDirection.Inflow -> Money(abs(amount.asDouble()))
            TransactionDirection.Outflow -> Money(-abs(amount.asDouble()))
            TransactionDirection.AccountTransfer -> amount // Keep original amount for transfers
            TransactionDirection.CreditCardPayment -> Money(abs(amount.asDouble()))
            TransactionDirection.Unknown -> amount
        }
    }

    private suspend fun createTransaction(
        amount: Money,
        account: Account,
        category: Category?,
        transactionPartner: String,
        direction: TransactionDirection,
        description: String,
        dateOfTransaction: kotlin.time.Instant,
        recurrenceFrequency: RecurrenceFrequency?
    ): Transaction {
        val transactionId = abs(Random.nextLong())
        val now = Clock.System.now()

        return Transaction(
            id = transactionId,
            amount = amount,
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
            nextPaymentDate = recurrenceFrequency?.let { freq ->
                calculateNextPaymentDate(dateOfTransaction, freq)
            }
        )
    }

    private fun calculateNextPaymentDate(currentDate: kotlin.time.Instant, frequency: RecurrenceFrequency): kotlin.time.Instant {
        val timeZone = TimeZone.currentSystemDefault()
        val localDateTime = currentDate.toLocalDateTime(timeZone)
        val localDate = localDateTime.date
        val time = localDateTime.time

        val nextLocalDate = when (frequency) {
            RecurrenceFrequency.NEVER -> localDate // Should not be called for NEVER, but return same date as fallback
            // Day-based frequencies
            RecurrenceFrequency.DAILY -> localDate.plus(DatePeriod(days = 1))
            RecurrenceFrequency.WEEKLY -> localDate.plus(DatePeriod(days = 7))
            RecurrenceFrequency.EVERY_OTHER_WEEK -> localDate.plus(DatePeriod(days = 14))
            RecurrenceFrequency.EVERY_4_WEEKS -> localDate.plus(DatePeriod(days = 28))

            // Month-based frequencies (calendar-aware)
            RecurrenceFrequency.MONTHLY -> localDate.plus(DatePeriod(months = 1))
            RecurrenceFrequency.EVERY_OTHER_MONTH -> localDate.plus(DatePeriod(months = 2))
            RecurrenceFrequency.EVERY_3_MONTHS -> localDate.plus(DatePeriod(months = 3))
            RecurrenceFrequency.EVERY_4_MONTHS -> localDate.plus(DatePeriod(months = 4))
            RecurrenceFrequency.TWICE_A_YEAR -> localDate.plus(DatePeriod(months = 6))
            RecurrenceFrequency.YEARLY -> localDate.plus(DatePeriod(years = 1))

            // Special case: twice a month
            RecurrenceFrequency.TWICE_A_MONTH -> {
                val dayOfMonth = localDate.day
                if (dayOfMonth <= 15) {
                    // Move to 15th of same month
                    LocalDate(localDate.year, localDate.month, 15)
                } else {
                    // Move to 1st of next month
                    LocalDate(localDate.year, localDate.month, 1).plus(DatePeriod(months = 1))
                }
            }
        }

        // Convert back to Instant with same time
        return nextLocalDate.atTime(time).toInstant(timeZone)
    }

    private suspend fun updateAccount(account: Account, amount: Money) {
        val updatedAccount = account.copy(
            balance = account.balance + amount,
            updatedAt = Clock.System.now()
        )
        accountRepository.updateAccount(account = updatedAccount)
    }
}