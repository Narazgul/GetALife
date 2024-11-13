package app.tinygiants.getalife.domain.usecase.categories.category

import app.tinygiants.getalife.data.local.datagenerator.FIXED_COST_GROUP
import app.tinygiants.getalife.data.local.datagenerator.transportCategoryEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.domain.model.BudgetPurpose
import app.tinygiants.getalife.domain.model.ProgressColor
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CalculateCategoryProgressUseCaseTest {

    private lateinit var calculateCategoryProgress: CalculateCategoryProgressUseCase

    @BeforeEach
    fun setUp() {

        calculateCategoryProgress = CalculateCategoryProgressUseCase()
    }

    // region No Target Set

    @Test
    fun `Get empty values for untouched Category`(): Unit = runTest {
        val progress = calculateCategoryProgress(categoryEntity = transportCategoryEntity())

        assertThat(progress.bar1).isEqualTo(0f)
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2VisibilityState).isFalse()
        assertThat(progress.optionalText).isEqualTo("")
    }

    @Test
    fun `No target some assigned & nothing spent`(): Unit = runTest {
        val entity = CategoryEntity(
            id = 1L,
            groupId = FIXED_COST_GROUP,
            emoji = "🏠",
            name = "Miete",
            budgetTarget = 0.0,
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = 100.0,
            availableMoney = 100.0,
            listPosition = 0,
            isInitialCategory = false,
            updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
            createdAt = Instant.parse("2024-01-01T12:00:00Z")
        )

        val progress = calculateCategoryProgress(categoryEntity = entity)

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2VisibilityState).isFalse()
        assertThat(progress.optionalText).isEqualTo("")
    }

    @Test
    fun `No target, some assigned, little spent`(): Unit = runTest {
        val entity = CategoryEntity(
            id = 1L,
            groupId = FIXED_COST_GROUP,
            emoji = "🏠",
            name = "Miete",
            budgetTarget = 0.0,
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = 100.0,
            availableMoney = 80.0,
            listPosition = 0,
            isInitialCategory = false,
            updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
            createdAt = Instant.parse("2024-01-01T12:00:00Z")
        )

        val progress = calculateCategoryProgress(categoryEntity = entity)

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo(0.2f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2VisibilityState).isFalse()
        assertThat(progress.optionalText).isEqualTo("TODO 20.0€ spent")
    }

    @Test
    fun `No target, some assigned, fully spent`(): Unit = runTest {
        val entity = CategoryEntity(
            id = 1L,
            groupId = FIXED_COST_GROUP,
            emoji = "🏠",
            name = "Miete",
            budgetTarget = 0.0,
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = 100.0,
            availableMoney = 0.0,
            listPosition = 0,
            isInitialCategory = false,
            updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
            createdAt = Instant.parse("2024-01-01T12:00:00Z")
        )

        val progress = calculateCategoryProgress(categoryEntity = entity)

        assertThat(progress.bar1).isEqualTo(0f)
        assertThat(progress.bar1Lite).isEqualTo(1f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2VisibilityState).isFalse()
        assertThat(progress.optionalText).isEqualTo("TODO all spent")
    }

    @Test
    fun `No target, nothing assigned, overspent`(): Unit = runTest {
        val entity = CategoryEntity(
            id = 1L,
            groupId = FIXED_COST_GROUP,
            emoji = "🏠",
            name = "Miete",
            budgetTarget = 0.0,
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = 0.0,
            availableMoney = -20.0,
            listPosition = 0,
            isInitialCategory = false,
            updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
            createdAt = Instant.parse("2024-01-01T12:00:00Z")
        )

        val progress = calculateCategoryProgress(categoryEntity = entity)

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Red)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2VisibilityState).isFalse()
        assertThat(progress.optionalText).isEqualTo("TODO Assign money to category or remove spending!")
    }

    @Test
    fun `No target, something assigned, overspent`(): Unit = runTest {
        val entity = CategoryEntity(
            id = 1L,
            groupId = FIXED_COST_GROUP,
            emoji = "🏠",
            name = "Miete",
            budgetTarget = 0.0,
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = 100.0,
            availableMoney = -20.0,
            listPosition = 0,
            isInitialCategory = false,
            updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
            createdAt = Instant.parse("2024-01-01T12:00:00Z")
        )

        val progress = calculateCategoryProgress(categoryEntity = entity)

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo((100.0/120.0).toFloat())
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Red)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2VisibilityState).isFalse()
        assertThat(progress.optionalText).isEqualTo("TODO Spent 20.0€ more than available")
    }

    // endregion

    // region Target Set

    @Test
    fun `Target set, nothing assigned, nothing spent`(): Unit = runTest {

    }

    // endregion
}