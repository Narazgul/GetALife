package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.GroupRepositoryFake
import app.tinygiants.getalife.domain.usecase.budget.GetBudgetUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.CalculateCategoryProgressUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension

class GetBudgetUseCaseTest {

    private lateinit var useCase: GetBudgetUseCase
    private lateinit var calculateProgressUseCase: CalculateCategoryProgressUseCase
    private lateinit var groupsRepositoryFake: GroupRepositoryFake
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        groupsRepositoryFake = GroupRepositoryFake()
        categoryRepositoryFake = CategoryRepositoryFake()
        calculateProgressUseCase = CalculateCategoryProgressUseCase(
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )

        useCase = GetBudgetUseCase(
            groupsRepository = groupsRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            calculateProgress = calculateProgressUseCase,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    // TODO write Tests
}