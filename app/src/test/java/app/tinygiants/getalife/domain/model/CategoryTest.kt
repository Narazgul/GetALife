package app.tinygiants.getalife.domain.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test
import kotlin.time.Clock

class CategoryTest {

    @Test
    fun `behaviorType returns Normal when linkedAccountId is null`() {
        val category = Category(
            id = 1L,
            groupId = 1L,
            emoji = "🛒",
            name = "Groceries",
            budgetTarget = Money(100.0),
            monthlyTargetAmount = null,
            targetMonthsRemaining = null,
            listPosition = 0,
            isInitialCategory = false,
            linkedAccountId = null,
            updatedAt = Clock.System.now(),
            createdAt = Clock.System.now()
        )

        assertThat(category.behaviorType).isEqualTo(CategoryBehaviorType.Normal)
    }

    @Test
    fun `behaviorType returns CreditCardPayment when linkedAccountId is not null`() {
        val category = Category(
            id = 2L,
            groupId = 1L,
            emoji = "💳",
            name = "Credit Card Payment",
            budgetTarget = Money(0.0),
            monthlyTargetAmount = null,
            targetMonthsRemaining = null,
            listPosition = 0,
            isInitialCategory = false,
            linkedAccountId = 5L, // Linked to credit card account
            updatedAt = Clock.System.now(),
            createdAt = Clock.System.now()
        )

        assertThat(category.behaviorType).isEqualTo(CategoryBehaviorType.CreditCardPayment)
    }
}