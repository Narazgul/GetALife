package app.tinygiants.getalife.domain.usecase.transaction.validation

import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.usecase.transaction.validation.ValidateTransactionDataUseCase.TransactionValidationData
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import assertk.assertions.messageContains
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ValidateTransactionDataUseCase")
class ValidateTransactionDataUseCaseTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var validateTransactionData: ValidateTransactionDataUseCase

    @BeforeEach
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        validateTransactionData = ValidateTransactionDataUseCase(testDispatcher)
    }

    @Nested
    @DisplayName("Valid transaction data")
    inner class ValidTransactionData {

        @Test
        @DisplayName("should pass validation for valid data")
        fun validDataPassesValidation() = runTest {
            // Arrange
            val validData = TransactionValidationData(
                accountId = 1L,
                amount = Money(100.0),
                direction = TransactionDirection.Outflow,
                transactionPartner = "Test Partner",
                description = "Test Description",
                recurrenceFrequency = null
            )

            // Act & Assert
            val result = runCatching { validateTransactionData(validData) }
            assertThat(result).isSuccess()
        }

        @Test
        @DisplayName("should pass validation for recurring transaction")
        fun validRecurringTransactionPassesValidation() = runTest {
            // Arrange
            val validData = TransactionValidationData(
                accountId = 1L,
                amount = Money(50.0),
                direction = TransactionDirection.Inflow,
                transactionPartner = "Salary",
                description = "Monthly Salary",
                recurrenceFrequency = RecurrenceFrequency.MONTHLY
            )

            // Act & Assert
            val result = runCatching { validateTransactionData(validData) }
            assertThat(result).isSuccess()
        }
    }

    @Nested
    @DisplayName("Transaction direction validation")
    inner class TransactionDirectionValidation {

        @Test
        @DisplayName("should fail for Unknown transaction direction")
        fun unknownDirectionFailsValidation() = runTest {
            // Arrange
            val invalidData = TransactionValidationData(
                accountId = 1L,
                amount = Money(100.0),
                direction = TransactionDirection.Unknown,
                transactionPartner = "Test Partner",
                description = "Test Description",
                recurrenceFrequency = null
            )

            // Act & Assert
            val result = runCatching { validateTransactionData(invalidData) }
            assertThat(result).isFailure().messageContains("Transaction direction cannot be Unknown")
        }

        @Test
        @DisplayName("should pass for all valid transaction directions")
        fun allValidDirectionsPassValidation() = runTest {
            // Arrange
            val validDirections = listOf(
                TransactionDirection.Inflow,
                TransactionDirection.Outflow,
                TransactionDirection.AccountTransfer,
                TransactionDirection.CreditCardPayment
            )

            // Act & Assert
            validDirections.forEach { direction ->
                val validData = TransactionValidationData(
                    accountId = 1L,
                    amount = Money(100.0),
                    direction = direction,
                    transactionPartner = "Test Partner",
                    description = "Test Description",
                    recurrenceFrequency = null
                )

                val result = runCatching { validateTransactionData(validData) }
                assertThat(result).isSuccess()
            }
        }
    }

    @Nested
    @DisplayName("Transaction amount validation")
    inner class TransactionAmountValidation {

        @Test
        @DisplayName("should fail for zero amount")
        fun zeroAmountFailsValidation() = runTest {
            // Arrange
            val invalidData = TransactionValidationData(
                accountId = 1L,
                amount = Money(0.0),
                direction = TransactionDirection.Outflow,
                transactionPartner = "Test Partner",
                description = "Test Description",
                recurrenceFrequency = null
            )

            // Act & Assert
            val result = runCatching { validateTransactionData(invalidData) }
            assertThat(result).isFailure().messageContains("Transaction amount must be positive")
        }

        @Test
        @DisplayName("should fail for negative amount")
        fun negativeAmountFailsValidation() = runTest {
            // Arrange
            val invalidData = TransactionValidationData(
                accountId = 1L,
                amount = Money(-50.0),
                direction = TransactionDirection.Outflow,
                transactionPartner = "Test Partner",
                description = "Test Description",
                recurrenceFrequency = null
            )

            // Act & Assert
            val result = runCatching { validateTransactionData(invalidData) }
            assertThat(result).isFailure().messageContains("Transaction amount must be positive")
        }

        @Test
        @DisplayName("should pass for positive amounts")
        fun positiveAmountsPassValidation() = runTest {
            // Arrange
            val validAmounts = listOf(0.01, 1.0, 100.0, 1000.99)

            // Act & Assert
            validAmounts.forEach { amount ->
                val validData = TransactionValidationData(
                    accountId = 1L,
                    amount = Money(amount),
                    direction = TransactionDirection.Outflow,
                    transactionPartner = "Test Partner",
                    description = "Test Description",
                    recurrenceFrequency = null
                )

                val result = runCatching { validateTransactionData(validData) }
                assertThat(result).isSuccess()
            }
        }
    }

    @Nested
    @DisplayName("Recurrence frequency validation")
    inner class RecurrenceFrequencyValidation {

        @Test
        @DisplayName("should fail for NEVER frequency on recurring transaction")
        fun neverFrequencyFailsValidation() = runTest {
            // Arrange
            val invalidData = TransactionValidationData(
                accountId = 1L,
                amount = Money(100.0),
                direction = TransactionDirection.Outflow,
                transactionPartner = "Test Partner",
                description = "Test Description",
                recurrenceFrequency = RecurrenceFrequency.NEVER
            )

            // Act & Assert
            val result = runCatching { validateTransactionData(invalidData) }
            assertThat(result).isFailure().messageContains("Recurring transactions cannot have frequency NEVER")
        }

        @Test
        @DisplayName("should pass for null frequency (non-recurring)")
        fun nullFrequencyPassesValidation() = runTest {
            // Arrange
            val validData = TransactionValidationData(
                accountId = 1L,
                amount = Money(100.0),
                direction = TransactionDirection.Outflow,
                transactionPartner = "Test Partner",
                description = "Test Description",
                recurrenceFrequency = null
            )

            // Act & Assert
            val result = runCatching { validateTransactionData(validData) }
            assertThat(result).isSuccess()
        }

        @Test
        @DisplayName("should pass for all valid recurrence frequencies")
        fun allValidFrequenciesPassValidation() = runTest {
            // Arrange
            val validFrequencies = RecurrenceFrequency.values().filter { it != RecurrenceFrequency.NEVER }

            // Act & Assert
            validFrequencies.forEach { frequency ->
                val validData = TransactionValidationData(
                    accountId = 1L,
                    amount = Money(100.0),
                    direction = TransactionDirection.Outflow,
                    transactionPartner = "Test Partner",
                    description = "Test Description",
                    recurrenceFrequency = frequency
                )

                val result = runCatching { validateTransactionData(validData) }
                assertThat(result).isSuccess()
            }
        }
    }

    @Nested
    @DisplayName("Transaction partner validation")
    inner class TransactionPartnerValidation {

        @Test
        @DisplayName("should fail for blank transaction partner")
        fun blankPartnerFailsValidation() = runTest {
            // Arrange
            val invalidData = TransactionValidationData(
                accountId = 1L,
                amount = Money(100.0),
                direction = TransactionDirection.Outflow,
                transactionPartner = "   ",
                description = "Test Description",
                recurrenceFrequency = null
            )

            // Act & Assert
            val result = runCatching { validateTransactionData(invalidData) }
            assertThat(result).isFailure().messageContains("Transaction partner cannot be blank")
        }

        @Test
        @DisplayName("should fail for empty transaction partner")
        fun emptyPartnerFailsValidation() = runTest {
            // Arrange
            val invalidData = TransactionValidationData(
                accountId = 1L,
                amount = Money(100.0),
                direction = TransactionDirection.Outflow,
                transactionPartner = "",
                description = "Test Description",
                recurrenceFrequency = null
            )

            // Act & Assert
            val result = runCatching { validateTransactionData(invalidData) }
            assertThat(result).isFailure().messageContains("Transaction partner cannot be blank")
        }

        @Test
        @DisplayName("should fail for too long transaction partner")
        fun tooLongPartnerFailsValidation() = runTest {
            // Arrange
            val tooLongName = "A".repeat(101) // 101 characters
            val invalidData = TransactionValidationData(
                accountId = 1L,
                amount = Money(100.0),
                direction = TransactionDirection.Outflow,
                transactionPartner = tooLongName,
                description = "Test Description",
                recurrenceFrequency = null
            )

            // Act & Assert
            val result = runCatching { validateTransactionData(invalidData) }
            assertThat(result).isFailure().messageContains("Transaction partner name too long")
        }

        @Test
        @DisplayName("should pass for valid transaction partner names")
        fun validPartnerNamesPassValidation() = runTest {
            // Arrange
            val validNames = listOf(
                "A",
                "Valid Partner",
                "X".repeat(100) // Exactly 100 characters - should pass
            )

            // Act & Assert
            validNames.forEach { partnerName ->
                val validData = TransactionValidationData(
                    accountId = 1L,
                    amount = Money(100.0),
                    direction = TransactionDirection.Outflow,
                    transactionPartner = partnerName,
                    description = "Test Description",
                    recurrenceFrequency = null
                )

                val result = runCatching { validateTransactionData(validData) }
                assertThat(result).isSuccess()
            }
        }
    }

    @Nested
    @DisplayName("Account ID validation")
    inner class AccountIdValidation {

        @Test
        @DisplayName("should fail for zero account ID")
        fun zeroAccountIdFailsValidation() = runTest {
            // Arrange
            val invalidData = TransactionValidationData(
                accountId = 0L,
                amount = Money(100.0),
                direction = TransactionDirection.Outflow,
                transactionPartner = "Test Partner",
                description = "Test Description",
                recurrenceFrequency = null
            )

            // Act & Assert
            val result = runCatching { validateTransactionData(invalidData) }
            assertThat(result).isFailure().messageContains("Account ID must be positive, was: 0")
        }

        @Test
        @DisplayName("should fail for negative account ID")
        fun negativeAccountIdFailsValidation() = runTest {
            // Arrange
            val invalidData = TransactionValidationData(
                accountId = -1L,
                amount = Money(100.0),
                direction = TransactionDirection.Outflow,
                transactionPartner = "Test Partner",
                description = "Test Description",
                recurrenceFrequency = null
            )

            // Act & Assert
            val result = runCatching { validateTransactionData(invalidData) }
            assertThat(result).isFailure().messageContains("Account ID must be positive, was: -1")
        }

        @Test
        @DisplayName("should pass for positive account IDs")
        fun positiveAccountIdPassesValidation() = runTest {
            // Arrange
            val validData = TransactionValidationData(
                accountId = 1L,
                amount = Money(100.0),
                direction = TransactionDirection.Outflow,
                transactionPartner = "Test Partner",
                description = "Test Description",
                recurrenceFrequency = null
            )

            // Act & Assert
            val result = runCatching { validateTransactionData(validData) }
            assertThat(result).isSuccess()
        }
    }
}