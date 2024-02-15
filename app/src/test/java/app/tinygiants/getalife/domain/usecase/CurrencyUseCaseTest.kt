package app.tinygiants.getalife.domain.usecase

import app.tinygiants.getalife.domain.model.Money
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.Locale


class CurrencyUseCaseTest {

    @ParameterizedTest
    @CsvSource(
        "1.00:1,00 €",
        "1.5:1,50 €",
        "2.123456:2,12 €",
        "3.999:4,00 €",
        "1000:1.000,00 €",
        "1000000:1.000.000,00 €",
        "0.5:0,50 €",
        delimiter = ':'
    )
    fun `Format currency correctly with German Locale`(input: Double, expected: String) {
        val result = Money(value = input, locale = Locale.GERMANY)
        assertThat(result.formattedMoney).isEqualTo(expected)
    }

    @ParameterizedTest
    @CsvSource(
        "1.00:1,00 $",
        "1.5:1,50 $",
        "2.123456:2,12 $",
        "3.999:4,00 $",
        "1000:1.000,00 $",
        "1000000:1.000.000,00 $",
        "0.5:0,50 $",
        delimiter = ':'
    )
    fun `Format currency correctly with US Locale`(input: Double, expected: String) {
        val result = Money(value = input, locale = Locale.US)
        assertThat(result.formattedMoney).isEqualTo(expected)
    }
}