package app.tinygiants.getalife.domain.usecase

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun Double.toCurrencyFormattedString(maximumDigits: Int = 2, locale: Locale = Locale.getDefault()): String {
    val numberFormat = NumberFormat.getCurrencyInstance()
    numberFormat.maximumFractionDigits = maximumDigits
    numberFormat.currency = Currency.getInstance(locale)
    return numberFormat.format(this)
}

class CurrencyUseCase {
    companion object {
        fun getCurrencyCode(locale: Locale = Locale.getDefault()): String = Currency.getInstance(locale).currencyCode

        fun getCurrencySymbol(locale: Locale = Locale.getDefault()): String = Currency.getInstance(locale).symbol
    }
}