package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.usecase.transaction.AddTransactionUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

class AddAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val addTransaction: AddTransactionUseCase,
    private val categoryRepository: CategoryRepository,
    private val groupRepository: GroupRepository,
    private val categoryMonthlyStatusRepository: CategoryMonthlyStatusRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        name: String,
        balance: Money,
        type: AccountType,
        startingBalanceName: String,
        startingBalanceDescription: String
    ) {
        val accountId = abs(Random.nextLong())

        createAccount(
            accountId = accountId,
            name = name,
            type = type
        )

        addStartingBalanceTransaction(
            accountId = accountId,
            amount = balance,
            startingBalanceName = startingBalanceName,
            startingBalanceDescription = startingBalanceDescription
        )

        // If this is a credit card account, create the credit card payment group and category
        if (type == AccountType.CreditCard) {
            val account = accountRepository.getAccount(accountId)!!
            createCreditCardPaymentCategory(account)
        }
    }

    private suspend fun createAccount(accountId: Long, name: String, type: AccountType) {
        val accounts = accountRepository.getAccountsFlow().first()
        val account = withContext(defaultDispatcher) {

            val highestListPosition = accounts.maxOfOrNull { it.listPosition }
            val endOfListPosition = if (highestListPosition == null) 0 else highestListPosition + 1
            val timeOfCreation = kotlin.time.Clock.System.now()

            Account(
                id = accountId,
                name = name,
                balance = EmptyMoney(),
                type = type,
                listPosition = endOfListPosition,
                updatedAt = timeOfCreation,
                createdAt = timeOfCreation
            )
        }

        accountRepository.addAccount(account)
    }

    private suspend fun addStartingBalanceTransaction(
        accountId: Long,
        amount: Money,
        startingBalanceName: String,
        startingBalanceDescription: String,
    ) {
        val direction = if (amount >= Money(value = 0.0)) TransactionDirection.Inflow else TransactionDirection.Outflow

        addTransaction(
            accountId = accountId,
            categoryId = null,
            amount = amount,
            direction = direction,
            transactionPartner = startingBalanceName,
            description = startingBalanceDescription
        )
    }

    /**
     * Creates the Credit Card Payment category for a newly created credit card account.
     * This ensures the payment category exists immediately after credit card creation.
     */
    private suspend fun createCreditCardPaymentCategory(creditCardAccount: Account) {
        // Check if category already exists (shouldn't happen, but safety first)
        val existingCategory = categoryRepository.getCreditCardPaymentCategory(creditCardAccount.id)
        if (existingCategory != null) return

        val categoryId = abs(Random.nextLong())
        val now = kotlin.time.Clock.System.now()

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
        val currentMonth = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val yearMonth = kotlinx.datetime.YearMonth(currentMonth.year, currentMonth.month)

        val initialStatus = app.tinygiants.getalife.domain.model.CategoryMonthlyStatus(
            category = category,
            assignedAmount = EmptyMoney(), // User needs to assign manually
            isCarryOverEnabled = true,
            spentAmount = EmptyMoney(),
            availableAmount = EmptyMoney(), // Will be calculated
            progress = app.tinygiants.getalife.domain.model.EmptyProgress(), // Will be calculated
            suggestedAmount = category.targetAmount, // Suggest the target amount for convenience
            targetContribution = category.targetAmount // For credit card payments, contribution equals target
        )
        categoryMonthlyStatusRepository.saveStatus(initialStatus, yearMonth)

        // Force a small delay to ensure database consistency
        delay(100)
    }

    /**
     * Finds or creates the "Credit Card Payments" group.
     * This group will be placed at the top of the budget.
     */
    private suspend fun findOrCreateCreditCardPaymentsGroup(): Long {
        val groupName = "Kreditkarten" // Using German directly since this will be replaced by string resource
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

        return groupId
    }
}