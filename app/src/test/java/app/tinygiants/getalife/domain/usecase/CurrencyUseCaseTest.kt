package app.tinygiants.getalife.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.Locale


class CurrencyUseCaseTest {

    @ParameterizedTest
    @CsvSource(
        "1.00:1,00 €",
        "2.00:2,00 €",
        "3:3,00 €",
        "1000:1.000,00 €",
        "1000000:1.000.000,00 €",
        "0.5:0,50 €",
        delimiter = ':'
    )
    fun `formattingCurrency GivenValidInput ReturnsExpectedFormattedString`(input: Double, expected: String) {
        val result = input.toCurrencyFormattedString(locale = Locale.GERMANY)
        assertThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @CsvSource(
        "1.5:1,50 €",
        "2.123456:2,12 €",
        "3.999:4,00 €",
        delimiter = ':'
    )
    fun `formattingCurrency GivenDifferentFractionalDigits ReturnsExpectedFormattedString`(input: Double, expected: String) {
        val result = input.toCurrencyFormattedString(maximumDigits = 2, locale = Locale.GERMANY)
        assertThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @CsvSource(
        "1.00:1,00 $",
        "2.00:2,00 $",
        "3:3,00 $",
        delimiter = ':'
    )
    fun `formattingCurrency GivenDifferentLocale ReturnsExpectedFormattedString`(input: Double, expected: String) {
        val result = input.toCurrencyFormattedString(locale = Locale.US)
        assertThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @CsvSource(
        "1.00:1,00 €",
        "2.00:2,00 €",
        "3:3,00 €",
        delimiter = ':'
    )
    fun `formattingCurrency GivenDifferentDelimiter ReturnsExpectedFormattedString`(input: Double, expected: String) {
        val result = input.toCurrencyFormattedString(locale = Locale.GERMANY)
        assertThat(result).isEqualTo(expected)
    }
}