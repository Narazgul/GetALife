package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.FIXED_COST_GROUP
import app.tinygiants.getalife.data.local.datagenerator.transportCategoryEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.domain.model.Progress
import app.tinygiants.getalife.domain.model.ProgressColor
import app.tinygiants.getalife.domain.model.UserHint
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

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
            assignedMoney = 0.0,
            availableMoney = 0.0,
            listPosition = 0,
            isInitialCategory = false,
            updatedAt = Instant.parse("2024-01-01T12:00:00Z"),
            createdAt = Instant.parse("2024-01-01T12:00:00Z")
        )
    }

    // region No Target Set

    @Test
    fun `Get empty values for untouched Category`(): Unit = runTest {
        val progress = calculateCategoryProgress(category = transportCategoryEntity().toDomain())

        assertThat(progress.bar1).isEqualTo(0f)
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.NoHint)
    }

    @Test
    fun `No target some assigned & nothing spent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 0.0, assignedMoney = 100.0, availableMoney = 100.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.NoHint)
    }

    @Test
    fun `No target, some assigned, little spent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 0.0, assignedMoney = 100.0, availableMoney = 80.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo(0.2f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.Spent(amount = "20,00 €"))
    }

    @Test
    fun `No target, some assigned, fully spent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 0.0, assignedMoney = 100.0, availableMoney = 0.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo(0f)
        assertThat(progress.bar1Lite).isEqualTo(1f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.AllSpent)
    }

    @Test
    fun `No target, nothing assigned, overspent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 0.0, assignedMoney = 0.0, availableMoney = -20.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Red)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.AssignMoreOrRemoveSpending(amount = "20,00 €"))
    }

    @Test
    fun `No target, something assigned, overspent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 0.0, assignedMoney = 100.0, availableMoney = -20.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo((100.0/120.0).toFloat())
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Red)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.SpentMoreThanAvailable(amount = "20,00 €"))
    }

    // endregion

    // region Target Set

    @Test
    fun `Set Target, nothing assigned, nothing spent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 0.0, availableMoney = 0.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Grey)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.MoreNeedForBudgetTarget(amount = "100,00 €"))
    }

    @Test
    fun `Set target, something assigned, nothing spent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 30.0, availableMoney = 30.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo(0.3f)
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo(0.3f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Yellow)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.MoreNeedForBudgetTarget(amount = "70,00 €"))
    }

    @Test
    fun `Set target, fully assigned, nothing spent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 100.0, availableMoney = 100.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.FullyFunded)

    }

    @Test
    fun `Set target, over budget assigned, nothing spent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 120.0, availableMoney = 120.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo((100.0/120.0).toFloat())
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo((100.0/120.0).toFloat())
        assertThat(progress.bar2Lite).isEqualTo((100.0/120.0).toFloat())
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Primary)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isTrue()
        assertThat(progress.userHint).isEqualTo(UserHint.ExtraMoney(amount = "20,00 €"))
    }

    @Test
    fun `Set target, something assigned, little spent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 60.0, availableMoney = 40.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo((60.0 / 100.0).toFloat())
        assertThat(progress.bar1Lite).isEqualTo(((60.0 - 40.0) / 100.0).toFloat())
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Yellow)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.YellowLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.MoreNeedForBudgetTarget(amount = "40,00 €"))
    }

    @Test
    fun `Set target, something assigned, all spent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 60.0, availableMoney = 0.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo((60.0 / 100.0).toFloat())
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.YellowLite)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.MoreNeedForBudgetTarget(amount = "40,00 €"))
    }

    @Test
    fun `Set target, fully assigned, little spent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 100.0, availableMoney = 70.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo(((100.0 - 70.0) / 100).toFloat())
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.NoHint)
    }

    @Test
    fun `Set target, fully assigned, all spent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 100.0, availableMoney = 0.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.NoHint)
    }

    @Test
    fun `Set target, over budget assigned, little spent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 120.0, availableMoney = 80.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar1Lite).isEqualTo(((120.0 - 80.0) / 120.0).toFloat())
        assertThat(progress.bar2).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar2Lite).isEqualTo((100 / 120.0).toFloat())
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Primary)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isTrue()
        assertThat(progress.userHint).isEqualTo(UserHint.ExtraMoney(amount = "20,00 €"))
    }

    @Test
    fun `Set target, over budget assigned, all budget spent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 120.0, availableMoney = 20.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar1Lite).isEqualTo(((120.0 - 20.0) / 120.0).toFloat())
        assertThat(progress.bar2).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar2Lite).isEqualTo((100 / 120.0).toFloat())
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Primary)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isTrue()
        assertThat(progress.userHint).isEqualTo(UserHint.ExtraMoney(amount = "20,00 €"))
    }

    @Test
    fun `Set Target, over budget assigned, little spent over budget`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 120.0, availableMoney = 10.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar1Lite).isEqualTo(((120.0 - 20.0) / 120.0).toFloat())
        assertThat(progress.bar2).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar2Lite).isEqualTo((110 / 120.0).toFloat())
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.PrimaryLite)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Primary)
        assertThat(progress.showColorOnSecondBar).isTrue()
        assertThat(progress.userHint).isEqualTo(UserHint.NoHint)
    }

    @Test
    fun `Set target, over budget assigned, all spent over budget`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 120.0, availableMoney = 0.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar1Lite).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar2).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar2Lite).isEqualTo((100 / 120.0).toFloat())
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Green)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.PrimaryLite)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isTrue()
        assertThat(progress.userHint).isEqualTo(UserHint.NoHint)
    }

    @Test
    fun `Set target, nothing assigned, overspent`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 0.0, availableMoney = -20.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo(1f)
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Red)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.AssignMoreOrRemoveSpending(amount = "20,00 €"))
    }

    @Test
    fun `Set target, something assigned, overspent`(): Unit = runTest {
        Progress(
            bar1 = ((40.0 + 20.0) / 100.0).toFloat(),
            bar1Lite = (40.0 / 100.0).toFloat(),
            bar1Color = ProgressColor.Red,
            bar1LiteColor = ProgressColor.YellowLite,
            userHint = UserHint.AssignMoreOrRemoveSpending(amount = "20,-€")
        )
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 40.0, availableMoney = -20.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo(((40.0 + 20.0) / 100.0).toFloat())
        assertThat(progress.bar1Lite).isEqualTo((40.0 / 100.0).toFloat())
        assertThat(progress.bar2).isEqualTo(0f)
        assertThat(progress.bar2Lite).isEqualTo(0f)
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Red)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.YellowLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isFalse()
        assertThat(progress.userHint).isEqualTo(UserHint.AssignMoreOrRemoveSpending(amount = "20,00 €"))
    }

    @Test
    fun `Set target, something assigned, overspent beyond budget`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 40.0, availableMoney = -80.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar1Lite).isEqualTo((40.0 / 120.0).toFloat())
        assertThat(progress.bar2).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar2Lite).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.Red)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.YellowLite)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Red)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isTrue()
        assertThat(progress.userHint).isEqualTo(UserHint.AssignMoreOrRemoveSpending(amount = "80,00 €"))
    }

    @Test
    fun `Set target, all assigned, overspent beyond budget`(): Unit = runTest {
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 100.0, availableMoney = -20.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar2Lite).isEqualTo((100.0 / 120.0).toFloat())
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.Red)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.showColorOnSecondBar).isTrue()
        assertThat(progress.userHint).isEqualTo(UserHint.AssignMoreOrRemoveSpending(amount = "20,00 €"))
    }

    @Test
    fun `Set target, assigned beyond target, overspent beyond budget`(): Unit = runTest {
        Progress(
            bar1 = (100.0 / 140.0).toFloat(),
            bar2 = (100.0 / 140.0).toFloat(),
            bar2Lite = (120.0 / 140.0).toFloat(),
            bar1Color = ProgressColor.GreenLite,
            bar2Color = ProgressColor.PrimaryLite,
            bar2LiteColor = ProgressColor.Red,
            showColorOnSecondBar = true,
            userHint = UserHint.AssignMoreOrRemoveSpending(amount = "20,-€")
        )
        entity = entity.copy(budgetTarget = 100.0, assignedMoney = 120.0, availableMoney = -20.0)

        val progress = calculateCategoryProgress(category = entity.toDomain())

        assertThat(progress.bar1).isEqualTo((100.0 / 140.0).toFloat())
        assertThat(progress.bar1Lite).isEqualTo(0f)
        assertThat(progress.bar2).isEqualTo((100.0 / 140.0).toFloat())
        assertThat(progress.bar2Lite).isEqualTo((120.0 / 140.0).toFloat())
        assertThat(progress.bar1Color).isEqualTo(ProgressColor.GreenLite)
        assertThat(progress.bar1LiteColor).isEqualTo(ProgressColor.Unknown)
        assertThat(progress.bar2Color).isEqualTo(ProgressColor.PrimaryLite)
        assertThat(progress.bar2LiteColor).isEqualTo(ProgressColor.Red)
        assertThat(progress.showColorOnSecondBar).isTrue()
        assertThat(progress.userHint).isEqualTo(UserHint.AssignMoreOrRemoveSpending(amount = "20,00 €"))
    }

    // endregion
}