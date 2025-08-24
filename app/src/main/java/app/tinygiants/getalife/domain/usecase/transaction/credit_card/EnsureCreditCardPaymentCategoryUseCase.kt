package app.tinygiants.getalife.domain.usecase.transaction.credit_card

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Clock

/**
 * Use case responsible for ensuring credit card payment categories exist.
 * Creates payment categories and credit card groups when needed.
 */
class EnsureCreditCardPaymentCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val groupRepository: GroupRepository,
    private val categoryMonthlyStatusRepository: CategoryMonthlyStatusRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    /**
     * Ensures that a credit card payment category exists for the given account.
     * Creates the category and group if necessary.
     */
    suspend operator fun invoke(creditCardAccount: Account): Category = withContext(defaultDispatcher) {
        findOrCreateCreditCardPaymentCategory(creditCardAccount)
    }

    private suspend fun findOrCreateCreditCardPaymentCategory(creditCardAccount: Account): Category {
        // Look for existing credit card payment category for this account
        val existingCategory = categoryRepository.getCreditCardPaymentCategory(creditCardAccount.id)
        return existingCategory ?: createCreditCardPaymentCategory(creditCardAccount)
    }

    private suspend fun createCreditCardPaymentCategory(creditCardAccount: Account): Category {
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
        createInitialMonthlyStatus(category)

        // Force a small delay to ensure database consistency
        delay(100)

        return category
    }

    private suspend fun createInitialMonthlyStatus(category: Category) {
        val currentMonth = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val yearMonth = kotlinx.datetime.YearMonth(currentMonth.year, currentMonth.month)

        val initialStatus = app.tinygiants.getalife.domain.model.CategoryMonthlyStatus(
            category = category,
            assignedAmount = Money(0.0),
            isCarryOverEnabled = true,
            spentAmount = Money(0.0),
            availableAmount = Money(0.0),
            progress = app.tinygiants.getalife.domain.model.EmptyProgress(),
            suggestedAmount = null,
            targetContribution = null // No target contribution for credit card payment categories
        )
        categoryMonthlyStatusRepository.saveStatus(initialStatus, yearMonth)
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
        groupRepository.getGroupByName(groupName)

        return groupId
    }
}