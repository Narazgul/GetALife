package app.tinygiants.getalife.domain.usecase.transaction.recurrence

import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

@DisplayName("CalculateRecurrenceDatesUseCase")
class CalculateRecurrenceDatesUseCaseTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var calculateRecurrenceDates: CalculateRecurrenceDatesUseCase
    private val timeZone = TimeZone.currentSystemDefault()
    private val testTime = LocalTime(14, 30) // 2:30 PM

    @BeforeEach
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        calculateRecurrenceDates = CalculateRecurrenceDatesUseCase(testDispatcher)
    }

    private fun createInstant(year: Int, month: Int, day: Int): Instant {
        return LocalDate(year, month, day).atTime(testTime).toInstant(timeZone)
    }

    @Nested
    @DisplayName("Day-based frequencies")
    inner class DayBasedFrequencies {

        @Test
        @DisplayName("should calculate next day for DAILY frequency")
        fun dailyFrequencyCalculation() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 15)
            val expectedDate = createInstant(2024, 1, 16)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.DAILY)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should calculate next week for WEEKLY frequency")
        fun weeklyFrequencyCalculation() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 15) // Monday
            val expectedDate = createInstant(2024, 1, 22) // Next Monday

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.WEEKLY)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should calculate two weeks later for EVERY_OTHER_WEEK frequency")
        fun everyOtherWeekFrequencyCalculation() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 15)
            val expectedDate = createInstant(2024, 1, 29)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.EVERY_OTHER_WEEK)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should calculate 4 weeks later for EVERY_4_WEEKS frequency")
        fun every4WeeksFrequencyCalculation() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 15)
            val expectedDate = createInstant(2024, 2, 12)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.EVERY_4_WEEKS)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }
    }

    @Nested
    @DisplayName("Month-based frequencies")
    inner class MonthBasedFrequencies {

        @Test
        @DisplayName("should calculate next month for MONTHLY frequency")
        fun monthlyFrequencyCalculation() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 15)
            val expectedDate = createInstant(2024, 2, 15)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.MONTHLY)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should handle month-end dates correctly for MONTHLY frequency")
        fun monthlyFrequencyWithMonthEndDate() = runTest {
            // Arrange - January 31st
            val currentDate = createInstant(2024, 1, 31)
            val expectedDate = createInstant(2024, 2, 29) // 2024 is leap year, Feb has 29 days

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.MONTHLY)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should calculate 2 months later for EVERY_OTHER_MONTH frequency")
        fun everyOtherMonthFrequencyCalculation() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 15)
            val expectedDate = createInstant(2024, 3, 15)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.EVERY_OTHER_MONTH)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should calculate 3 months later for EVERY_3_MONTHS frequency")
        fun every3MonthsFrequencyCalculation() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 15)
            val expectedDate = createInstant(2024, 4, 15)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.EVERY_3_MONTHS)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should calculate 4 months later for EVERY_4_MONTHS frequency")
        fun every4MonthsFrequencyCalculation() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 15)
            val expectedDate = createInstant(2024, 5, 15)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.EVERY_4_MONTHS)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should calculate 6 months later for TWICE_A_YEAR frequency")
        fun twiceAYearFrequencyCalculation() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 15)
            val expectedDate = createInstant(2024, 7, 15)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.TWICE_A_YEAR)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should calculate next year for YEARLY frequency")
        fun yearlyFrequencyCalculation() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 15)
            val expectedDate = createInstant(2025, 1, 15)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.YEARLY)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should handle leap year correctly for YEARLY frequency")
        fun yearlyFrequencyWithLeapYear() = runTest {
            // Arrange - Feb 29 on leap year
            val currentDate = createInstant(2024, 2, 29)
            val expectedDate = createInstant(2025, 2, 28) // 2025 is not leap year

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.YEARLY)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }
    }

    @Nested
    @DisplayName("Special case: TWICE_A_MONTH frequency")
    inner class TwiceAMonthFrequency {

        @Test
        @DisplayName("should move to 15th when current date is before 15th")
        fun twiceMonthlyBeforeFifteenth() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 10)
            val expectedDate = createInstant(2024, 1, 15)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.TWICE_A_MONTH)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should move to 15th when current date is exactly 1st")
        fun twiceMonthlyOnFirstDay() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 1)
            val expectedDate = createInstant(2024, 1, 15)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.TWICE_A_MONTH)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should move to 1st of next month when current date is 15th")
        fun twiceMonthlyOnFifteenth() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 15)
            val expectedDate = createInstant(2024, 2, 1)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.TWICE_A_MONTH)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should move to 1st of next month when current date is after 15th")
        fun twiceMonthlyAfterFifteenth() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 25)
            val expectedDate = createInstant(2024, 2, 1)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.TWICE_A_MONTH)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }

        @Test
        @DisplayName("should handle year transition correctly")
        fun twiceMonthlyYearTransition() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 12, 25)
            val expectedDate = createInstant(2025, 1, 1)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.TWICE_A_MONTH)

            // Assert
            assertThat(result).isEqualTo(expectedDate)
        }
    }

    @Nested
    @DisplayName("Edge cases")
    inner class EdgeCases {

        @Test
        @DisplayName("should return same date for NEVER frequency")
        fun neverFrequencyReturnsCurrentDate() = runTest {
            // Arrange
            val currentDate = createInstant(2024, 1, 15)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.NEVER)

            // Assert
            assertThat(result).isEqualTo(currentDate)
        }

        @Test
        @DisplayName("should preserve original time in all calculations")
        fun preservesOriginalTime() = runTest {
            // Arrange
            val originalTime = LocalTime(9, 15, 30) // 9:15:30 AM
            val currentDate = LocalDate(2024, 1, 15).atTime(originalTime).toInstant(timeZone)

            // Act
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.WEEKLY)

            // Assert
            val resultTime = result.toLocalDateTime(timeZone).time
            assertThat(resultTime).isEqualTo(originalTime)
        }

        @Test
        @DisplayName("should handle month transitions correctly")
        fun handlesMonthTransitions() = runTest {
            // Arrange - Last day of January
            val currentDate = createInstant(2024, 1, 31)

            // Act - Daily should go to Feb 1st
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.DAILY)

            // Assert
            val expected = createInstant(2024, 2, 1)
            assertThat(result).isEqualTo(expected)
        }

        @Test
        @DisplayName("should handle year transitions correctly")
        fun handlesYearTransitions() = runTest {
            // Arrange - Last day of year
            val currentDate = createInstant(2024, 12, 31)

            // Act - Daily should go to Jan 1st next year
            val result = calculateRecurrenceDates(currentDate, RecurrenceFrequency.DAILY)

            // Assert
            val expected = createInstant(2025, 1, 1)
            assertThat(result).isEqualTo(expected)
        }
    }
}