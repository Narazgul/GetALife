package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.FIXED_COST_GROUP
import app.tinygiants.getalife.data.local.datagenerator.transportCategoryEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.ProgressColor
import app.tinygiants.getalife.domain.model.UserHint
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Instant

class CalculateCategoryProgressUseCaseTest {

    private lateinit var calculateCategoryProgress: CalculateCategoryProgressUseCase
    private lateinit var entity: CategoryEntity

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        calculateCategoryProgress = CalculateCategoryProgressUseCase(
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )

        entity = CategoryEntity(
            id = 1L,
            groupId = FIXED_COST_GROUP,
            emoji = "🏠",
            name = "Rent",
            budgetTarget = 0.0,
            monthlyTargetAmount = null,
            targetMonthsRemaining = null,
            listPosition = 0,
            isInitialCategory = false,
            updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
            createdAt = Instant.parse("2024-01-01T12:00:00Z")
        )
    }

    private fun createCategoryMonthlyStatus(
        category: CategoryEntity,
        assignedAmount: Double,
        availableAmount: Double
    ): CategoryMonthlyStatus {
        val spentAmount = assignedAmount - availableAmount
        return CategoryMonthlyStatus(
            category = category.toDomain(),
            assignedAmount = Money(assignedAmount),
            isCarryOverEnabled = true,
            spentAmount = Money(spentAmount),
            availableAmount = Money(availableAmount),
            progress = app.tinygiants.getalife.domain.model.EmptyProgress(),
            suggestedAmount = null
        )
    }

    // region No Target Set

    @Test
    fun `Get empty values for untouched Category`(): Unit = runTest {
        val category = transportCategoryEntity().toDomain()
        val categoryMonthlyStatus = CategoryMonthlyStatus(
            category = category,
            assignedAmount = EmptyMoney(),
            isCarryOverEnabled = false,
            spentAmount = EmptyMoney(),
            availableAmount = EmptyMoney(),
            progress = app.tinygiants.getalife.domain.model.EmptyProgress(),
            suggestedAmount = null
        )

        val progress = calculateCategoryProgress(categoryMonthlyStatus)

        assertThat(progress.bar1).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Grey)
        assertThat(progress.userHint).isEqualTo(UserHint.NoHint)
    }

    @Test
    fun `No target some assigned & nothing spent`(): Unit = runTest {
        val status = createCategoryMonthlyStatus(entity, assignedAmount = 100.0, availableAmount = 100.0)

        val progress = calculateCategoryProgress(status)

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.userHint).isEqualTo(UserHint.FullyFunded)
    }

    @Test
    fun `No target, some assigned, little spent`(): Unit = runTest {
        val status = createCategoryMonthlyStatus(entity, assignedAmount = 100.0, availableAmount = 80.0)

        val progress = calculateCategoryProgress(status)

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo(0.2f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.userHint).isEqualTo(UserHint.Spent(amount = "$20.00"))
    }

    @Test
    fun `No target, some assigned, fully spent`(): Unit = runTest {
        val status = createCategoryMonthlyStatus(entity, assignedAmount = 100.0, availableAmount = 0.0)

        val progress = calculateCategoryProgress(status)

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.bar1Lite).isEqualTo(1f)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.userHint).isEqualTo(UserHint.AllSpent)
    }

    @Test
    fun `No target, nothing assigned, overspent`(): Unit = runTest {
        val category = entity.toDomain()
        val categoryMonthlyStatus = CategoryMonthlyStatus(
            category = category,
            assignedAmount = EmptyMoney(),
            isCarryOverEnabled = false,
            spentAmount = Money(20.0),
            availableAmount = Money(-20.0),
            progress = app.tinygiants.getalife.domain.model.EmptyProgress(),
            suggestedAmount = null
        )

        val progress = calculateCategoryProgress(categoryMonthlyStatus)

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Red)
        assertThat(progress.userHint).isEqualTo(UserHint.AssignMoreOrRemoveSpending(amount = "$20.00"))
    }

    @Test
    fun `No target, something assigned, overspent`(): Unit = runTest {
        val status = createCategoryMonthlyStatus(entity, assignedAmount = 100.0, availableAmount = -20.0)

        val progress = calculateCategoryProgress(status)

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Red)
        assertThat(progress.userHint).isEqualTo(UserHint.SpentMoreThanAvailable(amount = "$20.00"))
    }

    // endregion

    // region Simplified tests focusing on core functionality

    @Test
    fun `Basic spending within budget`(): Unit = runTest {
        val status = createCategoryMonthlyStatus(entity, assignedAmount = 200.0, availableAmount = 150.0)

        val progress = calculateCategoryProgress(status)

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo(0.25f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.userHint).isEqualTo(UserHint.Spent(amount = "$50.00"))
    }

    @Test
    fun `Overspending scenario`(): Unit = runTest {
        val status = createCategoryMonthlyStatus(entity, assignedAmount = 100.0, availableAmount = -50.0)

        val progress = calculateCategoryProgress(status)

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Red)
        assertThat(progress.userHint).isEqualTo(UserHint.SpentMoreThanAvailable(amount = "$50.00"))
    }

    // endregion
}