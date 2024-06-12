package app.tinygiants.getalife.domain.model

import androidx.compose.runtime.Immutable
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

@Immutable
data class Money(
    val value: Double,
    val maximumDigits: Int = 2,
    val locale: Locale = Locale.getDefault()
) {
    val currencySymbol: String = getCurrencySymbol()
    val formattedMoney: String = value.toCurrencyFormattedString()
    val formattedPositiveMoney: String = abs(value).toCurrencyFormattedString()

    operator fun compareTo(other: Money) = value.compareTo(other.value)
    operator fun plus(other: Money) = Money(value = this.value + other.value)
    operator fun minus(other: Money) = Money(value = this.value - other.value)

    private fun getCurrencySymbol(locale: Locale = Locale.getDefault()): String = Currency.getInstance(locale).symbol

    private fun Double.toCurrencyFormattedString(): String {
        val numberFormat = NumberFormat.getCurrencyInstance()
        numberFormat.maximumFractionDigits = maximumDigits
        numberFormat.currency = Currency.getInstance(locale)
        return numberFormat.format(this)
    }

    override fun toString() = formattedMoney
}