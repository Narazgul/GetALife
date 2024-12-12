package app.tinygiants.getalife.domain.model

import androidx.compose.runtime.Immutable
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

typealias EmptyMoney = Money

@Immutable
data class Money(
    private val value: BigDecimal = BigDecimal.ZERO,
    val locale: Locale = Locale.getDefault()
) {
    private val currency: Currency = Currency.getInstance(locale)

    val currencySymbol: String = currency.symbol
    val formattedMoney: String = formatMoney(value)
    val formattedPositiveMoney: String = formatMoney(value.abs())

    constructor(value: Double, locale: Locale = Locale.getDefault()) : this(
        value = BigDecimal(value.toString()),
        locale = locale
    )

    fun asBigDecimal(): BigDecimal = value
    fun asDouble(): Double = value.toDouble()
    fun positiveMoney(): Money = Money(value.abs(), locale)
    fun negativeMoney(): Money = Money(value.negate(), locale)
    fun toFloat(): Float = value.toFloat()

    operator fun compareTo(other: Money): Int {
        require(this.locale == other.locale) { "Locales must match for comparison" }
        return this.value.compareTo(other.value)
    }

    operator fun plus(other: Money): Money {
        require(this.locale == other.locale) { "Locales must match for addition" }
        return Money(this.value.add(other.value), this.locale)
    }

    operator fun minus(other: Money): Money {
        require(this.locale == other.locale) { "Locales must match for subtraction" }
        return Money(this.value.subtract(other.value), this.locale)
    }

    operator fun div(other: Money): Money {
        require(this.locale == other.locale) { "Locales must match for division" }
        require(other.value.compareTo(BigDecimal.ZERO) != 0) { "Division by zero is not allowed" }

        val result = this.value.divide(other.value, 2, RoundingMode.HALF_EVEN)
        return Money(result, this.locale)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Money) return false
        if (this.locale != other.locale) return false
        return this.value.compareTo(other.value) == 0
    }

    override fun hashCode(): Int {
        val normalizedValue = this.value.stripTrailingZeros()
        return 31 * normalizedValue.hashCode() + locale.hashCode()
    }

    private fun formatMoney(amount: BigDecimal): String {
        val numberFormat = NumberFormat.getCurrencyInstance(locale)
        numberFormat.currency = currency
        return numberFormat.format(amount)
    }
}