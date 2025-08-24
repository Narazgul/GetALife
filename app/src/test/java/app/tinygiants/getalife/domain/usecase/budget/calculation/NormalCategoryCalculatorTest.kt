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

@DisplayName("NormalCategoryCalculator")
class NormalCategoryCalculatorTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var normalCategoryCalculator: NormalCategoryCalculator

    @BeforeEach
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        normalCategoryCalculator = NormalCategoryCalculator(testDispatcher)
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
    @DisplayName("Normal category spending calculations")
    inner class NormalCategorySpendingCalculations {

        @Test
        @DisplayName("should calculate spent amount for normal category from checking account")
        fun calculatesSpentAmountFromCheckingAccount() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 2)
            val checkingAccount = createMockAccount(1L, AccountType.Checking)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-100.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 15)
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-50.75),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 20)
                )
            )

            // Act
            val result = normalCategoryCalculator(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(150.75)) // Sum of absolute amounts
        }

        @Test
        @DisplayName("should include spending from savings accounts")
        fun includesSpendingFromSavingsAccounts() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 2)
            val savingsAccount = createMockAccount(1L, AccountType.Savings)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-200.0),
                    account = savingsAccount,
                    dateOfTransaction = createInstant(2024, 2, 10)
                )
            )

            // Act
            val result = normalCategoryCalculator(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(200.0))
        }

        @Test
        @DisplayName("should exclude credit card spending")
        fun excludesCreditCardSpending() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 2)
            val checkingAccount = createMockAccount(1L, AccountType.Checking)
            val creditCardAccount = createMockAccount(2L, AccountType.CreditCard)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-100.0),
                    account = checkingAccount, // Checking account - included
                    dateOfTransaction = createInstant(2024, 2, 15)
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-75.0),
                    account = creditCardAccount, // Credit card - excluded
                    dateOfTransaction = createInstant(2024, 2, 20)
                )
            )

            // Act
            val result = normalCategoryCalculator(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(100.0)) // Only checking account transaction
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
                    direction = TransactionDirection.Outflow,
                    amount = Money(-100.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 15)
                ),
                createMockTransaction(
                    categoryId = otherCategoryId, // Different category
                    direction = TransactionDirection.Outflow,
                    amount = Money(-50.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 20)
                )
            )

            // Act
            val result = normalCategoryCalculator(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(100.0)) // Only first transaction
        }

        @Test
        @DisplayName("should exclude non-outflow transactions")
        fun excludesNonOutflowTransactions() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 2)
            val checkingAccount = createMockAccount(1L, AccountType.Checking)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow, // Outflow - included
                    amount = Money(-100.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 15)
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Inflow, // Inflow - excluded
                    amount = Money(50.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 20)
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.AccountTransfer, // Transfer - excluded
                    amount = Money(-25.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 25)
                )
            )

            // Act
            val result = normalCategoryCalculator(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(100.0)) // Only outflow transaction
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
                    direction = TransactionDirection.Outflow,
                    amount = Money(-100.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 15) // February - included
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-75.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 1, 20) // January - excluded
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-50.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 3, 5) // March - excluded
                )
            )

            // Act
            val result = normalCategoryCalculator(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(100.0)) // Only February transaction
        }

        @Test
        @DisplayName("should handle transactions with null categories")
        fun handlesTransactionsWithNullCategories() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 2)
            val checkingAccount = createMockAccount(1L, AccountType.Checking)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-100.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 15)
                ),
                createMockTransaction(
                    categoryId = null, // No category - excluded
                    direction = TransactionDirection.Outflow,
                    amount = Money(-50.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 20)
                )
            )

            // Act
            val result = normalCategoryCalculator(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(100.0)) // Only categorized transaction
        }

        @Test
        @DisplayName("should return zero when no matching transactions exist")
        fun returnsZeroWhenNoMatches() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 2)
            val transactions = emptyList<Transaction>()

            // Act
            val result = normalCategoryCalculator(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(0.0))
        }
    }

    @Nested
    @DisplayName("Mixed account type scenarios")
    inner class MixedAccountTypeScenarios {

        @Test
        @DisplayName("should handle mixed account types correctly")
        fun handlesMixedAccountTypes() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 2)

            val checkingAccount = createMockAccount(1L, AccountType.Checking)
            val savingsAccount = createMockAccount(2L, AccountType.Savings)
            val creditCardAccount = createMockAccount(3L, AccountType.CreditCard)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-100.0),
                    account = checkingAccount, // Included
                    dateOfTransaction = createInstant(2024, 2, 10)
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-75.0),
                    account = savingsAccount, // Included
                    dateOfTransaction = createInstant(2024, 2, 15)
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-50.0),
                    account = creditCardAccount, // Excluded
                    dateOfTransaction = createInstant(2024, 2, 20)
                )
            )

            // Act
            val result = normalCategoryCalculator(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(175.0)) // Only checking and savings
        }

        @Test
        @DisplayName("should handle realistic spending scenario")
        fun handlesRealisticSpendingScenario() = runTest {
            // Arrange - User spends $300 total: $200 cash, $100 credit card
            val groceriesCategoryId = 1L
            val yearMonth = YearMonth(2024, 2)

            val checkingAccount = createMockAccount(1L, AccountType.Checking)
            val creditCardAccount = createMockAccount(2L, AccountType.CreditCard)

            val transactions = listOf(
                // Cash spending (should be included in normal category calculation)
                createMockTransaction(
                    categoryId = groceriesCategoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-120.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 5)
                ),
                createMockTransaction(
                    categoryId = groceriesCategoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-80.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 12)
                ),
                // Credit card spending (should be excluded - handled by YNAB invisible movement)
                createMockTransaction(
                    categoryId = groceriesCategoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-100.0),
                    account = creditCardAccount,
                    dateOfTransaction = createInstant(2024, 2, 18)
                )
            )

            // Act
            val result = normalCategoryCalculator(
                categoryId = groceriesCategoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert - Only cash spending counted, credit card excluded
            assertThat(result).isEqualTo(Money(200.0)) // $120 + $80 cash spending only
        }
    }

    @Nested
    @DisplayName("Edge cases")
    inner class EdgeCases {

        @Test
        @DisplayName("should handle zero amount transactions")
        fun handlesZeroAmountTransactions() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 2)
            val checkingAccount = createMockAccount(1L, AccountType.Checking)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(0.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 15)
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-100.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 20)
                )
            )

            // Act
            val result = normalCategoryCalculator(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(100.0)) // Zero amount contributes 0, other contributes 100
        }

        @Test
        @DisplayName("should handle large transaction amounts")
        fun handlesLargeTransactionAmounts() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 2)
            val checkingAccount = createMockAccount(1L, AccountType.Checking)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-10000.99),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 15)
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-5000.01),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 2, 20)
                )
            )

            // Act
            val result = normalCategoryCalculator(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(15001.0)) // Sum of absolute amounts
        }

        @Test
        @DisplayName("should handle year boundary correctly")
        fun handlesYearBoundaryCorrectly() = runTest {
            // Arrange
            val categoryId = 1L
            val yearMonth = YearMonth(2024, 1) // January 2024
            val checkingAccount = createMockAccount(1L, AccountType.Checking)

            val transactions = listOf(
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-100.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2024, 1, 15) // January 2024 - included
                ),
                createMockTransaction(
                    categoryId = categoryId,
                    direction = TransactionDirection.Outflow,
                    amount = Money(-75.0),
                    account = checkingAccount,
                    dateOfTransaction = createInstant(2023, 12, 20) // December 2023 - excluded
                )
            )

            // Act
            val result = normalCategoryCalculator(
                categoryId = categoryId,
                yearMonth = yearMonth,
                allTransactions = transactions
            )

            // Assert
            assertThat(result).isEqualTo(Money(100.0)) // Only January 2024 transaction
        }
    }
}