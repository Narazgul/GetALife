package app.tinygiants.getalife.domain.usecase.categories

import app.tinygiants.getalife.MainCoroutineExtension
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.GroupRepositoryFake
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class GetCategoriesInGroupsUseCaseTest {

    private lateinit var useCase: GetCategoriesInGroupsUseCase
    private lateinit var groupsRepositoryFake: GroupRepositoryFake
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val mainCoroutineExtension: MainCoroutineExtension = MainCoroutineExtension()
    }

    @BeforeEach
    fun setUp() {
        groupsRepositoryFake = GroupRepositoryFake()
        categoryRepositoryFake = CategoryRepositoryFake()

        useCase = GetCategoriesInGroupsUseCase(
            groupsRepository = groupsRepositoryFake,
            categoryRepository = categoryRepositoryFake,
            defaultDispatcher = mainCoroutineExtension.testDispatcher
        )
    }

    @Test
    fun `No target set no money assigned nothing spent - `(): Unit = runTest {

    }
}