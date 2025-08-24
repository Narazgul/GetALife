package app.tinygiants.getalife.domain.usecase.budget.calculation

import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("CarryOverCalculator")
class CarryOverCalculatorTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var mockStatusRepository: CategoryMonthlyStatusRepository
    private lateinit var carryOverCalculator: CarryOverCalculator

    @BeforeEach
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        mockStatusRepository = mockk()
        carryOverCalculator = CarryOverCalculator(mockStatusRepository, testDispatcher)
    }

    private fun createMockCategoryMonthlyStatus(availableAmount: Money): CategoryMonthlyStatus {
        return CategoryMonthlyStatus(
            category = mockk(),
            assignedAmount = Money(0.0),
            isCarryOverEnabled = true,
            spentAmount = Money(0.0),
            availableAmount = availableAmount,
            progress = EmptyProgress(),
            suggestedAmount = null,
            targetContribution = null
        )
    }

    @Nested
    @DisplayName("Normal month transitions")
    inner class NormalMonthTransitions {

        @Test
        @DisplayName("should calculate carry-over from previous month")
        fun calculatesCarryOverFromPreviousMonth() = runTest {
            // Arrange
            val categoryId = 1L
            val currentMonth = YearMonth(2024, Month.FEBRUARY)
            val previousMonth = YearMonth(2024, Month.JANUARY)
            val availableAmount = Money(150.0)

            val mockStatus = createMockCategoryMonthlyStatus(availableAmount)
            coEvery { mockStatusRepository.getStatus(categoryId, previousMonth) } returns mockStatus

            // Act
            val result = carryOverCalculator(categoryId, currentMonth)

            // Assert
            assertThat(result).isEqualTo(availableAmount)
        }

        @Test
        @DisplayName("should return EmptyMoney when no previous status exists")
        fun returnsEmptyMoneyWhenNoPreviousStatus() = runTest {
            // Arrange
            val categoryId = 1L
            val currentMonth = YearMonth(2024, Month.FEBRUARY)
            val previousMonth = YearMonth(2024, Month.JANUARY)

            coEvery { mockStatusRepository.getStatus(categoryId, previousMonth) } returns null

            // Act
            val result = carryOverCalculator(categoryId, currentMonth)

            // Assert
            assertThat(result).isEqualTo(EmptyMoney())
        }

        @Test
        @DisplayName("should handle carry-over from March to April")
        fun handlesCarryOverMarchToApril() = runTest {
            // Arrange
            val categoryId = 1L
            val currentMonth = YearMonth(2024, Month.APRIL)
            val previousMonth = YearMonth(2024, Month.MARCH)
            val availableAmount = Money(75.50)

            val mockStatus = createMockCategoryMonthlyStatus(availableAmount)
            coEvery { mockStatusRepository.getStatus(categoryId, previousMonth) } returns mockStatus

            // Act
            val result = carryOverCalculator(categoryId, currentMonth)

            // Assert
            assertThat(result).isEqualTo(availableAmount)
        }

        @Test
        @DisplayName("should handle negative carry-over amounts")
        fun handlesNegativeCarryOver() = runTest {
            // Arrange
            val categoryId = 1L
            val currentMonth = YearMonth(2024, Month.FEBRUARY)
            val previousMonth = YearMonth(2024, Month.JANUARY)
            val availableAmount = Money(-25.0) // Overspent in previous month

            val mockStatus = createMockCategoryMonthlyStatus(availableAmount)
            coEvery { mockStatusRepository.getStatus(categoryId, previousMonth) } returns mockStatus

            // Act
            val result = carryOverCalculator(categoryId, currentMonth)

            // Assert
            assertThat(result).isEqualTo(availableAmount)
        }
    }

    @Nested
    @DisplayName("Year boundary transitions")
    inner class YearBoundaryTransitions {

        @Test
        @DisplayName("should handle transition from December to January")
        fun handlesYearBoundaryTransition() = runTest {
            // Arrange
            val categoryId = 1L
            val currentMonth = YearMonth(2024, Month.JANUARY)
            val previousMonth = YearMonth(2023, Month.DECEMBER)
            val availableAmount = Money(200.0)

            val mockStatus = createMockCategoryMonthlyStatus(availableAmount)
            coEvery { mockStatusRepository.getStatus(categoryId, previousMonth) } returns mockStatus

            // Act
            val result = carryOverCalculator(categoryId, currentMonth)

            // Assert
            assertThat(result).isEqualTo(availableAmount)
        }

        @Test
        @DisplayName("should handle transition from January of current year")
        fun handlesJanuaryTransition() = runTest {
            // Arrange
            val categoryId = 1L
            val currentMonth = YearMonth(2025, Month.JANUARY)
            val previousMonth = YearMonth(2024, Month.DECEMBER)
            val availableAmount = Money(500.0)

            val mockStatus = createMockCategoryMonthlyStatus(availableAmount)
            coEvery { mockStatusRepository.getStatus(categoryId, previousMonth) } returns mockStatus

            // Act
            val result = carryOverCalculator(categoryId, currentMonth)

            // Assert
            assertThat(result).isEqualTo(availableAmount)
        }

        @Test
        @DisplayName("should return EmptyMoney for January when no December status exists")
        fun returnsEmptyMoneyForJanuaryWithoutDecember() = runTest {
            // Arrange
            val categoryId = 1L
            val currentMonth = YearMonth(2024, Month.JANUARY)
            val previousMonth = YearMonth(2023, Month.DECEMBER)

            coEvery { mockStatusRepository.getStatus(categoryId, previousMonth) } returns null

            // Act
            val result = carryOverCalculator(categoryId, currentMonth)

            // Assert
            assertThat(result).isEqualTo(EmptyMoney())
        }
    }

    @Nested
    @DisplayName("Edge cases")
    inner class EdgeCases {

        @Test
        @DisplayName("should handle zero available amount carry-over")
        fun handlesZeroCarryOver() = runTest {
            // Arrange
            val categoryId = 1L
            val currentMonth = YearMonth(2024, Month.FEBRUARY)
            val previousMonth = YearMonth(2024, Month.JANUARY)
            val availableAmount = Money(0.0)

            val mockStatus = createMockCategoryMonthlyStatus(availableAmount)
            coEvery { mockStatusRepository.getStatus(categoryId, previousMonth) } returns mockStatus

            // Act
            val result = carryOverCalculator(categoryId, currentMonth)

            // Assert
            assertThat(result).isEqualTo(Money(0.0))
        }

        @Test
        @DisplayName("should handle large carry-over amounts")
        fun handlesLargeCarryOver() = runTest {
            // Arrange
            val categoryId = 1L
            val currentMonth = YearMonth(2024, Month.FEBRUARY)
            val previousMonth = YearMonth(2024, Month.JANUARY)
            val availableAmount = Money(10000.99)

            val mockStatus = createMockCategoryMonthlyStatus(availableAmount)
            coEvery { mockStatusRepository.getStatus(categoryId, previousMonth) } returns mockStatus

            // Act
            val result = carryOverCalculator(categoryId, currentMonth)

            // Assert
            assertThat(result).isEqualTo(availableAmount)
        }

        @Test
        @DisplayName("should handle different category IDs correctly")
        fun handlesDifferentCategoryIds() = runTest {
            // Arrange
            val categoryId1 = 1L
            val categoryId2 = 2L
            val currentMonth = YearMonth(2024, Month.FEBRUARY)
            val previousMonth = YearMonth(2024, Month.JANUARY)
            val availableAmount1 = Money(100.0)
            val availableAmount2 = Money(200.0)

            val mockStatus1 = createMockCategoryMonthlyStatus(availableAmount1)
            val mockStatus2 = createMockCategoryMonthlyStatus(availableAmount2)

            coEvery { mockStatusRepository.getStatus(categoryId1, previousMonth) } returns mockStatus1
            coEvery { mockStatusRepository.getStatus(categoryId2, previousMonth) } returns mockStatus2

            // Act
            val result1 = carryOverCalculator(categoryId1, currentMonth)
            val result2 = carryOverCalculator(categoryId2, currentMonth)

            // Assert
            assertThat(result1).isEqualTo(availableAmount1)
            assertThat(result2).isEqualTo(availableAmount2)
        }
    }

    @Nested
    @DisplayName("Previous month calculation logic")
    inner class PreviousMonthCalculation {

        @Test
        @DisplayName("should calculate previous month correctly for all months")
        fun calculatesAllPreviousMonthsCorrectly() = runTest {
            // Test data: current month to expected previous month
            val testCases = mapOf(
                YearMonth(2024, Month.FEBRUARY) to YearMonth(2024, Month.JANUARY),
                YearMonth(2024, Month.MARCH) to YearMonth(2024, Month.FEBRUARY),
                YearMonth(2024, Month.APRIL) to YearMonth(2024, Month.MARCH),
                YearMonth(2024, Month.MAY) to YearMonth(2024, Month.APRIL),
                YearMonth(2024, Month.JUNE) to YearMonth(2024, Month.MAY),
                YearMonth(2024, Month.JULY) to YearMonth(2024, Month.JUNE),
                YearMonth(2024, Month.AUGUST) to YearMonth(2024, Month.JULY),
                YearMonth(2024, Month.SEPTEMBER) to YearMonth(2024, Month.AUGUST),
                YearMonth(2024, Month.OCTOBER) to YearMonth(2024, Month.SEPTEMBER),
                YearMonth(2024, Month.NOVEMBER) to YearMonth(2024, Month.OCTOBER),
                YearMonth(2024, Month.DECEMBER) to YearMonth(2024, Month.NOVEMBER),
                YearMonth(2024, Month.JANUARY) to YearMonth(2023, Month.DECEMBER)
            )

            val categoryId = 1L
            val availableAmount = Money(100.0)
            val mockStatus = createMockCategoryMonthlyStatus(availableAmount)

            testCases.forEach { (currentMonth, expectedPreviousMonth) ->
                // Arrange
                coEvery { mockStatusRepository.getStatus(categoryId, expectedPreviousMonth) } returns mockStatus

                // Act
                val result = carryOverCalculator(categoryId, currentMonth)

                // Assert
                assertThat(result).isEqualTo(availableAmount)
            }
        }
    }
}