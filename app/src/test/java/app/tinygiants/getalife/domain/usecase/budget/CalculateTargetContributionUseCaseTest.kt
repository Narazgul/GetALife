package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TargetType
import kotlinx.datetime.LocalDate
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import kotlin.time.Clock

class CalculateTargetContributionUseCaseTest {

    private val useCase = CalculateTargetContributionUseCase()

    @Test
    fun `returns null for none target type`() {
        val category = createTestCategory(targetType = TargetType.NONE)

        val result = useCase(category)

        assertNull(result)
    }

    @Test
    fun `returns target amount for needed for spending`() {
        val category = createTestCategory(
            targetType = TargetType.NEEDED_FOR_SPENDING,
            targetAmount = Money(300.0)
        )

        val result = useCase(category)

        assertEquals(Money(300.0), result)
    }

    @Test
    fun `returns null for savings balance when targetAmount is null`() {
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = null
        )

        val result = useCase(category)

        assertNull(result)
    }

    @Test
    fun `returns null for savings balance when targetDate is null`() {
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(1000.0),
            targetDate = null
        )

        val result = useCase(category)

        assertNull(result)
    }

    @Test
    fun `returns zero when target date is in the past`() {
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(1000.0),
            targetDate = LocalDate(2020, 1, 1)
        )

        val result = useCase(category)

        assertEquals(Money(0.0), result)
    }

    @Test
    fun `calculates monthly need correctly for savings balance 12 months`() {
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(1200.0),
            targetDate = LocalDate(2026, 8, 31)
        )

        val result = useCase(category, availableAmount = Money(0.0))

        assertEquals(100.0, result?.asDouble() ?: 0.0, 10.0)
    }

    @Test
    fun `calculates correctly with available amount`() {
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(1200.0),
            targetDate = LocalDate(2026, 8, 31)
        )

        val result = useCase(category, availableAmount = Money(600.0))

        assertEquals(50.0, result?.asDouble() ?: 0.0, 10.0)
    }

    @Test
    fun `returns zero when target already reached`() {
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(1000.0),
            targetDate = LocalDate(2026, 8, 31)
        )

        val result = useCase(category, availableAmount = Money(1000.0))

        assertEquals(Money(0.0), result)
    }

    @Test
    fun `debug user scenario - 300 euros target sept 2024`() {
        // User's exact scenario: 300€ target, September 30, 2024
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(300.0),
            targetDate = LocalDate(2024, 9, 30) // Past date as user reported
        )

        println("=== DEBUG: User's scenario ===")
        val result1 = useCase(category, availableAmount = Money(0.0))
        println("Without assigned money: ${result1?.asDouble() ?: 0.0}€")

        val result2 = useCase(category, availableAmount = Money(100.0))
        println("With 100€ assigned: ${result2?.asDouble() ?: 0.0}€")

        // Since the target date is in the past, both should return 0
        assertEquals(Money(0.0), result1)
        assertEquals(Money(0.0), result2)
    }

    @Test
    fun `debug user scenario with future date - 300 euros target sept 2026`() {
        // User's scenario but with future date
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(300.0),
            targetDate = LocalDate(2026, 9, 30) // Future date
        )

        println("=== DEBUG: User's scenario with future date ===")
        val result1 = useCase(category, availableAmount = Money(0.0))
        println("Without assigned money: ${result1?.asDouble() ?: 0.0}€")

        val result2 = useCase(category, availableAmount = Money(100.0))
        println("With 100€ assigned: ${result2?.asDouble() ?: 0.0}€")

        // These should be reasonable amounts
        // ~13 months from Aug 2025 to Sep 2026 = ~300/13 ≈ 23€ per month
        assertEquals(23.0, result1?.asDouble() ?: 0.0, 5.0)
        // With 100€ already saved: (300-100)/13 ≈ 15€ per month  
        assertEquals(15.0, result2?.asDouble() ?: 0.0, 5.0)
    }

    @Test
    fun `short term savings - less than a month`() {
        // User's ACTUAL scenario: August 31st target
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(300.0),
            targetDate = LocalDate(2025, 8, 31) // 15 days from Aug 16, 2025
        )

        println("=== SHORT TERM: 15 days until target ===")
        val result = useCase(category, availableAmount = Money(0.0))
        println("Days until target: ~15, Monthly needed: ${result?.asDouble() ?: 0.0}€")

        // 15 days = ~0.5 months, so less than 1 month → full amount due
        assertEquals(300.0, result?.asDouble() ?: 0.0, 1.0)
    }

    @Test
    fun `very short term savings - less than a week`() {
        // Target in 5 days
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(500.0),
            targetDate = LocalDate(2025, 8, 21) // 5 days from Aug 16, 2025
        )

        val result = useCase(category, availableAmount = Money(0.0))

        // 5 days = ~0.16 months, so less than 1 month → full amount due
        assertEquals(500.0, result?.asDouble() ?: 0.0, 1.0)
    }

    @Test
    fun `medium term savings - 2 months`() {
        // Target in 2 months
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(600.0),
            targetDate = LocalDate(2025, 10, 16) // 2 months from Aug 16, 2025
        )

        val result = useCase(category, availableAmount = Money(0.0))

        // 2 months → 600€ / 2 months = 300€ per month
        assertEquals(300.0, result?.asDouble() ?: 0.0, 50.0)
    }

    @Test
    fun `edge case - exactly 1 month`() {
        // Target in exactly 30.44 days (= 1 month)
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(400.0),
            targetDate = LocalDate(2025, 9, 15) // ~30 days from Aug 16, 2025
        )

        val result = useCase(category, availableAmount = Money(0.0))

        // Exactly around 1 month → should distribute properly, not return full amount
        // 400€ / 1 month = 400€ per month
        assertEquals(400.0, result?.asDouble() ?: 0.0, 50.0)
    }

    @Test
    fun `short term with partial savings`() {
        // 15 days target with 100€ already saved
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(300.0),
            targetDate = LocalDate(2025, 8, 31) // 15 days from Aug 16, 2025
        )

        val result = useCase(category, availableAmount = Money(100.0))

        // Still needed: 300€ - 100€ = 200€
        // Less than 1 month → full remaining amount due
        assertEquals(200.0, result?.asDouble() ?: 0.0, 1.0)
    }

    @Test
    fun `month based calculation - user scenario 600 euros for Sep 1st`() {
        // User's example: 600€ target for September 1st, 2025
        // Current: August 16th → August + September = 2 months
        // Expected: 300€ per month
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(600.0),
            targetDate = LocalDate(2025, 9, 1)
        )

        println("=== MONTH-BASED: 600€ for Sep 1st ===")
        val result = useCase(category, availableAmount = Money(0.0))
        println("600€ over August + September = ${result?.asDouble() ?: 0.0}€ per month")

        // August (current month) + September = 2 months
        // 600€ / 2 months = 300€ per month
        assertEquals(300.0, result?.asDouble() ?: 0.0, 1.0)
    }

    @Test
    fun `single month - target end of current month`() {
        // Target at end of August (current month)
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(300.0),
            targetDate = LocalDate(2025, 8, 31) // End of current month
        )

        println("=== SINGLE MONTH: August only ===")
        val result = useCase(category, availableAmount = Money(0.0))
        println("300€ for August only = ${result?.asDouble() ?: 0.0}€")

        // Only current month (August) = 1 month
        // 300€ / 1 month = 300€ this month
        assertEquals(300.0, result?.asDouble() ?: 0.0, 1.0)
    }

    @Test
    fun `cross year calculation - Dec to Feb`() {
        // Current: August 2025, Target: February 2026
        // Months: Aug, Sep, Oct, Nov, Dec, Jan, Feb = 7 months
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(700.0),
            targetDate = LocalDate(2026, 2, 15)
        )

        println("=== CROSS YEAR: August 2025 to February 2026 ===")
        val result = useCase(category, availableAmount = Money(0.0))
        println("700€ over 7 months = ${result?.asDouble() ?: 0.0}€ per month")

        // August 2025 to February 2026 = 7 months
        // 700€ / 7 months = 100€ per month
        assertEquals(100.0, result?.asDouble() ?: 0.0, 1.0)
    }

    @Test
    fun `three months - August to October`() {
        // Current: August, Target: October
        // Months: August, September, October = 3 months
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(900.0),
            targetDate = LocalDate(2025, 10, 31)
        )

        val result = useCase(category, availableAmount = Money(0.0))

        // August + September + October = 3 months
        // 900€ / 3 months = 300€ per month
        assertEquals(300.0, result?.asDouble() ?: 0.0, 1.0)
    }

    @Test
    fun `with existing savings - month based`() {
        // 600€ target for Sep 1st, but 200€ already saved
        val category = createTestCategory(
            targetType = TargetType.SAVINGS_BALANCE,
            targetAmount = Money(600.0),
            targetDate = LocalDate(2025, 9, 1)
        )

        val result = useCase(category, availableAmount = Money(200.0))

        // Still needed: 600€ - 200€ = 400€
        // Over 2 months (August + September): 400€ / 2 = 200€ per month
        assertEquals(200.0, result?.asDouble() ?: 0.0, 1.0)
    }

    private fun createTestCategory(
        targetType: TargetType = TargetType.NONE,
        targetAmount: Money? = null,
        targetDate: LocalDate? = null
    ) = Category(
        id = 1L,
        groupId = 1L,
        emoji = "💰",
        name = "Test Category",
        budgetTarget = Money(0.0),
        monthlyTargetAmount = null,
        targetMonthsRemaining = null,
        targetType = targetType,
        targetAmount = targetAmount,
        targetDate = targetDate,
        listPosition = 0,
        isInitialCategory = false,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )
}