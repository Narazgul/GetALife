package app.tinygiants.getalife.presentation.main_app.budget.composables.category

import app.tinygiants.getalife.domain.model.Money
import kotlinx.datetime.LocalDate

object TargetValidation {

    data class ValidationResult(
        val value: Double?,
        val errorMessage: String?
    )

    /**
     * Parses and validates monetary input with helpful error messages
     */
    fun validateAmount(input: String): ValidationResult {
        if (input.isBlank()) {
            return ValidationResult(null, null) // Empty input is valid (optional)
        }

        // Remove common currency symbols and whitespace
        val cleaned = input.trim()
            .replace("€", "")
            .replace("$", "")
            .replace("£", "")
            .replace("¥", "")
            .replace(",", ".")
            .replace(" ", "")

        if (cleaned.isBlank()) {
            return ValidationResult(null, "Bitte geben Sie einen gültigen Betrag ein")
        }

        // Check for multiple decimal points
        if (cleaned.count { it == '.' } > 1) {
            return ValidationResult(null, "Ungültiges Format. Verwenden Sie nur einen Dezimalpunkt (z.B. 123.45)")
        }

        // Check for invalid characters
        if (!cleaned.all { it.isDigit() || it == '.' }) {
            return ValidationResult(null, "Nur Zahlen und Dezimalpunkt sind erlaubt (z.B. 123.45)")
        }

        val value = cleaned.toDoubleOrNull()
        return when {
            value == null -> ValidationResult(null, "Ungültiger Betrag. Verwenden Sie das Format: 123.45")
            value < 0.0 -> ValidationResult(null, "Der Betrag kann nicht negativ sein")
            value > 999999999.99 -> ValidationResult(null, "Betrag ist zu groß (Maximum: 999.999.999,99€)")
            else -> ValidationResult(value, null)
        }
    }

    @Deprecated("Use validateAmount instead for better error handling")
    fun parseNonNegativeAmount(input: String): Double? {
        return validateAmount(input).value
    }

    fun parseDateOrNull(input: String): LocalDate? {
        val trimmed = input.trim()
        return runCatching { LocalDate.parse(trimmed) }.getOrNull()
    }
}
