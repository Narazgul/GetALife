package app.tinygiants.getalife.domain.model

import androidx.compose.runtime.Immutable
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

typealias EmptyMoney = Money

@Immutable
data class Money(
    private val value: BigDecimal = BigDecimal.valueOf(0.0),
    val locale: Locale = Locale.getDefault()
) {
    private val currency: Currency = Currency.getInstance(locale)

    val currencySymbol: String = currency.symbol
    val formattedMoney: String = formatMoney(value)
    val formattedPositiveMoney: String = formatMoney(value.abs())

    constructor(value: Double, locale: Locale = Locale.getDefault()): this(
        value = BigDecimal.valueOf(value),
        locale = locale
    )

    fun asBigDecimal() = value
    fun asDouble() = value.toDouble()
    fun positiveMoney() = Money(value = value.abs(), locale = locale)
    fun negativeMoney() = Money(value = -value.abs(), locale = locale)
    fun toFloat() = value.toFloat()

    operator fun compareTo(other: Money): Int {
        require(this.locale == other.locale) { "Locales must match for comparison" }
        return this.value.compareTo(other.value)
    }

    operator fun plus(other: Money): Money {
        require(this.locale == other.locale) { "Locales must match for addition" }
        return Money(value = this.value.add(other.value), locale = this.locale)
    }

    operator fun minus(other: Money): Money {
        require(this.locale == other.locale) { "Locales must match for subtraction" }
        return Money(value = this.value.subtract(other.value), locale = this.locale)
    }

    operator fun div(other: Money): Money {
        require(this.locale == other.locale) { "Locales must match for division" }
        return Money(value = this.value.div(other.value), locale = this.locale)
    }

    private fun formatMoney(amount: BigDecimal): String {
        val numberFormat = NumberFormat.getCurrencyInstance(locale)
        numberFormat.currency = currency
        return numberFormat.format(amount)
    }
}