package app.tinygiants.getalife.presentation.shared_composables

import app.tinygiants.getalife.domain.model.Money

/**
 * Utility functions for consistent input validation across the application.
 * Provides standardized parsing and validation for common input types.
 */
object InputValidationUtils {

    /**
     * Safely parse amount input string to Money object
     * Handles common user input patterns like comma decimal separators
     *
     * @param input Raw user input string
     * @param fallbackValue Value to use if parsing fails
     * @return Valid Money object
     */
    fun parseAmountInput(input: String, fallbackValue: Money = Money(0.0)): Money {
        return try {
            val normalizedInput = input
                .replace(',', '.') // Handle European decimal separator
                .trim()

            val doubleValue = normalizedInput.toDoubleOrNull()

            when {
                doubleValue == null -> fallbackValue
                doubleValue < 0 -> Money(0.0) // Ensure non-negative for UI inputs
                doubleValue > MAX_TRANSACTION_AMOUNT -> Money(MAX_TRANSACTION_AMOUNT)
                else -> Money(doubleValue)
            }
        } catch (e: Exception) {
            fallbackValue
        }
    }

    /**
     * Safely parse balance input string to Double
     * Handles common user input patterns and allows negative values for credit cards
     *
     * @param input Raw user input string
     * @param fallbackValue Value to use if parsing fails
     * @return Valid Double value
     */
    fun parseBalanceInput(input: String, fallbackValue: Double = 0.0): Double {
        return try {
            val normalizedInput = input
                .replace(',', '.') // Handle European decimal separator
                .trim()

            val doubleValue = normalizedInput.toDoubleOrNull()

            when {
                doubleValue == null -> fallbackValue
                doubleValue > MAX_ACCOUNT_BALANCE -> MAX_ACCOUNT_BALANCE
                doubleValue < MIN_ACCOUNT_BALANCE -> MIN_ACCOUNT_BALANCE
                else -> doubleValue
            }
        } catch (e: Exception) {
            fallbackValue
        }
    }

    /**
     * Validate if an amount input string represents a valid transaction amount
     *
     * @param input Raw user input string
     * @return True if valid, false otherwise
     */
    fun isValidAmountInput(input: String): Boolean {
        return try {
            val normalizedInput = input.replace(',', '.').trim()
            val doubleValue = normalizedInput.toDoubleOrNull()

            doubleValue != null && doubleValue > 0 && doubleValue <= MAX_TRANSACTION_AMOUNT
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Validate if amount input is positive and within limits
     * Used for guided mode step validation
     */
    fun isPositiveValidAmount(input: String): Boolean {
        return try {
            val doubleValue = input.replace(',', '.').toDoubleOrNull()
            doubleValue != null && doubleValue > 0
        } catch (e: Exception) {
            false
        }
    }

    // Constants for validation limits
    private const val MAX_TRANSACTION_AMOUNT = 999_999_999.0
    private const val MAX_ACCOUNT_BALANCE = 999_999_999.0
    private const val MIN_ACCOUNT_BALANCE = -999_999_999.0
}