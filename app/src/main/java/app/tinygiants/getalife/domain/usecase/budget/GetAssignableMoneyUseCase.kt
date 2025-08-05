package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.includeInBudget
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.presentation.shared_composables.UiText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock

typealias AssignableMoney = Money
typealias OverspentCategoryText = UiText.StringResource?
typealias AssignableMoneyBanner = Pair<AssignableMoney, OverspentCategoryText>

data class AssignableMoneyException(override val message: String) : Exception(message)

class GetAssignableMoneyUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val statusRepository: CategoryMonthlyStatusRepository,
    private val categoryRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher,
    private val clock: Clock = Clock.System
) {

    operator fun invoke(): Flow<Result<AssignableMoneyBanner>> =
        combine(
            accountRepository.getAccountsFlow(),
            statusRepository.getStatusForMonthFlow(
                clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).run {
                    YearMonth(year, month)
                }
            ),
            categoryRepository.getCategoriesFlow()
        ) { accounts: List<Account>, statuses: List<CategoryMonthlyStatus>, categories: List<Category> ->
            // Calculate sum of balances for all accounts included in budget (excluding closed accounts)
            val totalBudgetBalance = accounts
                .filter { it.type.includeInBudget && !it.isClosed }
                .sumOf { account ->
                    when (account.type) {
                        AccountType.CreditCard -> {
                            // For credit cards: only include positive balances (credit/overpayment)
                            // Negative balances (debt) should not reduce assignable money
                            if (account.balance.asDouble() > 0) {
                                account.balance.asDouble()
                            } else {
                                0.0 // Don't count debt against assignable money
                            }
                        }

                        else -> {
                            // For all other account types, include the full balance
                            account.balance.asDouble()
                        }
                    }
                }

            // Calculate sum of assigned amounts for the month across all categories
            val totalAssigned = statuses.sumOf { it.assignedAmount.asDouble() }

            val assignableMoney = totalBudgetBalance - totalAssigned
            val overspentText: UiText.StringResource? = getOverspentCategoriesText(Money(assignableMoney), categories)

            Result.success(AssignableMoneyBanner(Money(assignableMoney), overspentText))
        }.catch { e ->
            emit(Result.failure(AssignableMoneyException(message = e.message ?: "Unknown error")))
        }.flowOn(defaultDispatcher)

    private fun getOverspentCategoriesText(assignableMoney: Money, categories: List<Category>): UiText.StringResource? {
        return null
    }
}