package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.BudgetRepositoryFake
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class UpdateAssignableMoneyUseCaseTest {

    private lateinit var updateAssignableMoney: UpdateAssignableMoneyUseCase
    private lateinit var budgetRepositoryFake: BudgetRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        budgetRepositoryFake = BudgetRepositoryFake()

        updateAssignableMoney = UpdateAssignableMoneyUseCase(
            repository = budgetRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `Zero assignable Money remains zero when not changing the value`(): Unit = runTest {
        updateAssignableMoney(Money(value = 0.0))

        val updatedBudget = budgetRepositoryFake.budgetsFlow.value.first()
        assertThat(updatedBudget.readyToAssign).isEqualTo(0.0)
    }

    @Test
    fun `Test raising the assignable Money after value was zero`(): Unit = runTest {
        updateAssignableMoney(Money(value = 10.0))

        val updatedBudget = budgetRepositoryFake.budgetsFlow.value.first()
        assertThat(updatedBudget.readyToAssign).isEqualTo(10.0)
    }

    @Test
    fun `Test lowering the assignable Money after value was zero`(): Unit = runTest {
        updateAssignableMoney(Money(value = -10.0))

        val updatedBudget = budgetRepositoryFake.budgetsFlow.value.first()
        assertThat(updatedBudget.readyToAssign).isEqualTo(-10.0)
    }

}