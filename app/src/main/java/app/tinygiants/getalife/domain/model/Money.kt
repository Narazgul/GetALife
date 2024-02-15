package app.tinygiants.getalife.domain.model

import androidx.compose.runtime.Immutable
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Immutable
data class Money(
    val value: Double,
    val maximumDigits: Int = 2,
    val locale: Locale = Locale.getDefault()
) {
    val currencyCode: String = getCurrencyCode()
    val currencySymbol: String = getCurrencySymbol()
    val formattedMoney: String = value.toCurrencyFormattedString()

    private fun Double.toCurrencyFormattedString(): String {
        val numberFormat = NumberFormat.getCurrencyInstance()
        numberFormat.maximumFractionDigits = maximumDigits
        numberFormat.currency = Currency.getInstance(locale)
        return numberFormat.format(this)
    }

    private fun getCurrencyCode(locale: Locale = Locale.getDefault()): String = Currency.getInstance(locale).currencyCode
    private fun getCurrencySymbol(locale: Locale = Locale.getDefault()): String = Currency.getInstance(locale).symbol
}