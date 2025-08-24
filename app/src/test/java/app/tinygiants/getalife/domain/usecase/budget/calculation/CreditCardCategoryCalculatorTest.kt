package app.tinygiants.getalife.domain.usecase.budget.calculation

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toInstant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

@DisplayName("CreditCardCategoryCalculator")
class CreditCardCategoryCalculatorTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var creditCardCategoryCalculator: CreditCardCategoryCalculator

    @BeforeEach
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        creditCardCategoryCalculator = CreditCardCategoryCalculator(testDispatcher)
    }

    private fun createInstant(year: Int, month: Int, day: Int, hour: Int = 12): Instant {
        return LocalDateTime(year, month, day, hour, 0).toInstant(TimeZone.currentSystemDefault())
    }

    private fun createMockTransaction(
        categoryId: Long?,
        direction: TransactionDirection,
        amount: Money,
        account: Account,
        dateOfTransaction: Instant
    ): Transaction {
        return mockk<Transaction>().also {
            every { it.category?.id } returns categoryId
            every { it.transactionDirection } returns direction
            every { it.amount } returns amount
            every { it.account } returns account
            every { it.dateOfTransaction } returns dateOfTransaction
        }
    }

    private fun createMockAccount(id: Long, type: AccountType): Account {
        return mockk<Account>().also {
            every { it.id } returns id
            every { it.type } returns type
        }
    }

    private fun createMockCategory(id: Long): Category {
        return mockk<Category>().also {
            every { it.id } returns id
        }
    }

    @Nested
    @DisplayName("Credit card payment calculations")
    inner class CreditCardPaymentCalculations {

        @Test
        @DisplayName("should calculate spent amount for credit card payments")
        fun calculatesSpentAmountForCreditCardPayments() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 2)
            val creditCardAccount = createMockAccount(1L, AccountType.CreditCard)
            val checkingAccount = createMockAccount(2L, AccountType.Checking)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.CreditCardPayment,
                    amount = Money(-100.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 15)
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.CreditCardPayment,
                    amount = Money(-75.50),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 20)
                )
            )

            // Act
            val result = creditCardCategoryCalculator.calculateSpentAmount(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(175.50)) // Sum of absolute amounts
        }

        @Test
        @DisplayName("should exclude transactions from other categories")
        fun excludesTransactionsFromOtherCategories() = runTest {
            // Arrange
            val categoryId = 1L
            val otherCategoryId = 2L
            val yearMonth = YearMonth(2024, 2)
            val checkingAccount = createMockAccount(1L, AccountType.Checking)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.CreditCardPayment,
                    amount = Money(-100.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 15)
                ),
                createMockTransaction(
                    categoryId = otherCategoryId, // Different category
                    direction = TransactionDirection.CreditCardPayment,
                    amount = Money(-50.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 20)
                )
            )

            // Act
            val result = creditCardCategoryCalculator.calculateSpentAmount(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(100.0)) // Only first transaction
        }

        @Test
        @DisplayName("should exclude non-credit card payment transactions")
        fun excludesNonCreditCardPaymentTransactions() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 2)
            val checkingAccount = createMockAccount(1L, AccountType.Checking)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.CreditCardPayment,
                    amount = Money(-100.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 15)
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow, // Not credit card payment
                    amount = Money(-75.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 20)
                )
            )

            // Act
            val result = creditCardCategoryCalculator.calculateSpentAmount(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(100.0)) // Only credit card payment
        }

        @Test
        @DisplayName("should exclude transactions from other months")
        fun excludesTransactionsFromOtherMonths() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 2) // February
            val checkingAccount = createMockAccount(1L, AccountType.Checking)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.CreditCardPayment,
                    amount = Money(-100.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 15) // February - included
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.CreditCardPayment,
                    amount = Money(-75.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 1, 20) // January - excluded
                )
            )

            // Act
            val result = creditCardCategoryCalculator.calculateSpentAmount(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(100.0)) // Only February transaction
        }

        @Test
        @DisplayName("should return zero when no credit card payments exist")
        fun returnsZeroWhenNoPayments() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 2)
            val transactions = emptyList<Transaction>()

            // Act
            val result = creditCardCategoryCalculator.calculateSpentAmount(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(0.0))
        }
    }

    @Nested
    @DisplayName("Invisible inflow from credit card spending")
    inner class InvisibleInflowCalculations {

        @Test
        @DisplayName("should calculate invisible inflow from credit card spending")
        fun calculatesInvisibleInflowFromCreditCardSpending() = runTest {
            // Arrange
            val creditCardAccountId = 1L
            val yearMonth = YearMonth(2024, 2)
            val creditCardAccount = createMockAccount(creditCardAccountId, AccountType.CreditCard)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = 1L,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-50.0),
                    account = creditCardAccount,
                    dateOfTransaction = createInstant(2024, 2, 10)
                ),
                createMockTransaction(
                    categoryId = 2L,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-75.25),
                    account = creditCardAccount,
                    dateOfTransaction = createInstant(2024, 2, 15)
                )
            )

            // Act
            val result = creditCardCategoryCalculator.calculateInvisibleInflowFromCreditCardSpending(
                creditCardAccountId = creditCardAccountId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(125.25)) // Sum of absolute amounts
        }

        @Test
        @DisplayName("should exclude transactions from non-credit card accounts")
        fun excludesNonCreditCardAccounts() = runTest {
            // Arrange
            val creditCardAccountId = 1L
            val checkingAccountId = 2L
            val yearMonth = YearMonth(2024, 2)

            val creditCardAccount = createMockAccount(creditCardAccountId, AccountType.CreditCard)
            val checkingAccount = createMockAccount(checkingAccountId, AccountType.Checking)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = 1L,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-50.0),
                    account = creditCardAccount, // Credit card - included
                    dateOfTransaction = createInstant(2024, 2, 10)
                ),
                createMockTransaction(
                    categoryId = 1L,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-75.0),
                    account = checkingAccount, // Checking account - excluded
                    dateOfTransaction = createInstant(2024, 2, 15)
                )
            )

            // Act
            val result = creditCardCategoryCalculator.calculateInvisibleInflowFromCreditCardSpending(
                creditCardAccountId = creditCardAccountId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(50.0)) // Only credit card transaction
        }

        @Test
        @DisplayName("should exclude non-outflow transactions from credit cards")
        fun excludesNonOutflowTransactions() = runTest {
            // Arrange
            val creditCardAccountId = 1L
            val yearMonth = YearMonth(2024, 2)
            val creditCardAccount = createMockAccount(creditCardAccountId, AccountType.CreditCard)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = 1L,
                    direction = TransactionDirection.Outflow, // Outflow - included
                    amount = Money(-50.0),
                    account = creditCardAccount,
                    dateOfTransaction = createInstant(2024, 2, 10)
                ),
                createMockTransaction(
                    categoryId = 1L,
                    direction = TransactionDirection.Inflow, // Inflow - excluded
                    amount = Money(25.0),
                    account = creditCardAccount,
                    dateOfTransaction = createInstant(2024, 2, 15)
                )
            )

            // Act
            val result = creditCardCategoryCalculator.calculateInvisibleInflowFromCreditCardSpending(
                creditCardAccountId = creditCardAccountId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(50.0)) // Only outflow transaction
        }

        @Test
        @DisplayName("should exclude transactions from different credit card accounts")
        fun excludesDifferentCreditCardAccounts() = runTest {
            // Arrange
            val targetCreditCardId = 1L
            val otherCreditCardId = 2L
            val yearMonth = YearMonth(2024, 2)

            val targetCreditCard = createMockAccount(targetCreditCardId, AccountType.CreditCard)
            val otherCreditCard = createMockAccount(otherCreditCardId, AccountType.CreditCard)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = 1L,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-50.0),
                    account = targetCreditCard, // Target card - included
                    dateOfTransaction = createInstant(2024, 2, 10)
                ),
                createMockTransaction(
                    categoryId = 1L,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-30.0),
                    account = otherCreditCard, // Other card - excluded
                    dateOfTransaction = createInstant(2024, 2, 15)
                )
            )

            // Act
            val result = creditCardCategoryCalculator.calculateInvisibleInflowFromCreditCardSpending(
                creditCardAccountId = targetCreditCardId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(50.0)) // Only target card transaction
        }

        @Test
        @DisplayName("should exclude transactions from other months")
        fun excludesTransactionsFromOtherMonths() = runTest {
            // Arrange
            val creditCardAccountId = 1L
            val yearMonth = YearMonth(2024, 2) // February
            val creditCardAccount = createMockAccount(creditCardAccountId, AccountType.CreditCard)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = 1L,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-50.0),
                    account = creditCardAccount,
                    dateOfTransaction = createInstant(2024, 2, 10) // February - included
                ),
                createMockTransaction(
                    categoryId = 1L,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-30.0),
                    account = creditCardAccount,
                    dateOfTransaction = createInstant(2024, 3, 5) // March - excluded
                )
            )

            // Act
            val result = creditCardCategoryCalculator.calculateInvisibleInflowFromCreditCardSpending(
                creditCardAccountId = creditCardAccountId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(50.0)) // Only February transaction
        }

        @Test
        @DisplayName("should return zero when no credit card spending exists")
        fun returnsZeroWhenNoSpending() = runTest {
            // Arrange
            val creditCardAccountId = 1L
            val yearMonth = YearMonth(2024, 2)
            val transactions = emptyList<Transaction>()

            // Act
            val result = creditCardCategoryCalculator.calculateInvisibleInflowFromCreditCardSpending(
                creditCardAccountId = creditCardAccountId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(0.0))
        }
    }

    @Nested
    @DisplayName("YNAB-style invisible money movement scenarios")
    inner class YNABStyleScenarios {

        @Test
        @DisplayName("should handle realistic credit card scenario")
        fun handlesRealisticCreditCardScenario() = runTest {
            // Arrange - User spends $200 on credit card and makes $100 payment
            val creditCardAccountId = 1L
            val paymentCategoryId = 10L
            val yearMonth = YearMonth(2024, 2)

            val creditCardAccount = createMockAccount(creditCardAccountId, AccountType.CreditCard)
            val checkingAccount = createMockAccount(2L, AccountType.Checking)

            val transactions = listOf(
                // Credit card spending (should create invisible inflow)
                createMockTransaction(
                    categoryId = 1L, // Groceries
                    direction = TransactionDirection.Outflow,
                    amount = Money(-120.0),
                    account = creditCardAccount,
                    dateOfTransaction = createInstant(2024, 2, 5)
                ),
                createMockTransaction(
                    categoryId = 2L, // Gas
                    direction = TransactionDirection.Outflow,
                    amount = Money(-80.0),
                    account = creditCardAccount,
                    dateOfTransaction = createInstant(2024, 2, 12)
                ),
                // Credit card payment (should reduce payment category available)
                createMockTransaction(
                    categoryId = paymentCategoryId,
                    direction = TransactionDirection.CreditCardPayment,
                    amount = Money(-100.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 25)
                )
            )

            // Act - Calculate invisible inflow for payment category
            val invisibleInflow = creditCardCategoryCalculator.calculateInvisibleInflowFromCreditCardSpending(
                creditCardAccountId = creditCardAccountId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Act - Calculate spent amount for payment category
            val spentAmount = creditCardCategoryCalculator.calculateSpentAmount(
                categoryId = paymentCategoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert - YNAB logic: spending creates inflow, payments reduce available
            assertThat(invisibleInflow).isEqualTo(Money(200.0)) // $120 + $80 spending
            assertThat(spentAmount).isEqualTo(Money(100.0)) // $100 payment
            // Net effect: Payment category should have $100 available (200 - 100)
        }

        @Test
        @DisplayName("should handle multiple credit cards correctly")
        fun handlesMultipleCreditCards() = runTest {
            // Arrange
            val visa1AccountId = 1L
            val visa2AccountId = 2L
            val visa1PaymentCategoryId = 10L
            val visa2PaymentCategoryId = 11L
            val yearMonth = YearMonth(2024, 2)

            val visa1Account = createMockAccount(visa1AccountId, AccountType.CreditCard)
            val visa2Account = createMockAccount(visa2AccountId, AccountType.CreditCard)

            val transactions = listOf(
                // Visa 1 spending
                createMockTransaction(
                    categoryId = 1L,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-100.0),
                    account = visa1Account,
                    dateOfTransaction = createInstant(2024, 2, 10)
                ),
                // Visa 2 spending  
                createMockTransaction(
                    categoryId = 1L,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-50.0),
                    account = visa2Account,
                    dateOfTransaction = createInstant(2024, 2, 15)
                )
            )

            // Act
            val visa1Inflow = creditCardCategoryCalculator.calculateInvisibleInflowFromCreditCardSpending(
                creditCardAccountId = visa1AccountId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            val visa2Inflow = creditCardCategoryCalculator.calculateInvisibleInflowFromCreditCardSpending(
                creditCardAccountId = visa2AccountId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert - Each card's spending should only affect its own payment category
            assertThat(visa1Inflow).isEqualTo(Money(100.0)) // Only Visa 1 spending
            assertThat(visa2Inflow).isEqualTo(Money(50.0))  // Only Visa 2 spending
        }
    }
}