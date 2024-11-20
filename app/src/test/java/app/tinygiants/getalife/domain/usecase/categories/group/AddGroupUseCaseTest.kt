package app.tinygiants.getalife.domain.usecase.categories.group

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.remote.ai.AiRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.GroupRepositoryFake
import app.tinygiants.getalife.domain.usecase.categories.category.AddCategoryUseCase
import app.tinygiants.getalife.domain.usecase.emoji.AddEmojiToCategoryNameUseCase
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AddGroupUseCaseTest {

    private lateinit var addGroup: AddGroupUseCase
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake
    private lateinit var groupRepositoryFake: GroupRepositoryFake
    private lateinit var addEmojiToCategory: AddEmojiToCategoryNameUseCase
    private lateinit var aiRepositoryFake: AiRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        categoryRepositoryFake = CategoryRepositoryFake()
        groupRepositoryFake = GroupRepositoryFake()
        aiRepositoryFake = AiRepositoryFake()
        addEmojiToCategory = AddEmojiToCategoryNameUseCase(
            aiRepository = aiRepositoryFake,
            repository = categoryRepositoryFake
        )
        val addCategory = AddCategoryUseCase(
            repository = categoryRepositoryFake,
            addEmoji = addEmojiToCategory,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )

        addGroup = AddGroupUseCase(
            repository = groupRepositoryFake,
            addCategory = addCategory,
            ioDispatcher = testDispatcherExtension.testDispatcher,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `Test group gets created`(): Unit = runTest {
        addGroup(groupName = "Fix costs")

        val groups = groupRepositoryFake.groupFlow.value

        assertThat(groups).hasSize(1)
        assertThat(groups.first().name).isEqualTo("Fix costs")
    }

    @Test
    fun `Test second group got correct listPosition`(): Unit = runTest {
        addGroup(groupName = "Fixed costs")
        addGroup(groupName = "Dreams")

        val groups = groupRepositoryFake.groupFlow.value

        assertThat(groups).hasSize(2)
        assertThat(groups[0].name).isEqualTo("Fixed costs")
        assertThat(groups[1].name).isEqualTo("Dreams")
        assertThat(groups[1].listPosition).isEqualTo(1)
    }

    @Test
    fun `Test empty category gets created with group`(): Unit = runTest {
        addGroup(groupName = "Fixed costs")

        val createdGroup = groupRepositoryFake.groupFlow.value.first()
        val categories = categoryRepositoryFake.categories.value

        assertThat(categories).hasSize(1)
        assertThat(categories.first().name).isEqualTo("")
        assertThat(categories.first().availableMoney).isEqualTo(0.0)
        assertThat(categories.first().groupId).isEqualTo(createdGroup.id)
        assertThat(categories.first().assignedMoney).isEqualTo(0.0)
        assertThat(categories.first().listPosition).isEqualTo(0)
    }
}