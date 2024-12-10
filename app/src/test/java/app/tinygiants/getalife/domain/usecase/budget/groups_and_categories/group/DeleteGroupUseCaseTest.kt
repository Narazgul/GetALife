package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.dailyLife
import app.tinygiants.getalife.data.local.datagenerator.fixedCosts
import app.tinygiants.getalife.data.local.datagenerator.groups
import app.tinygiants.getalife.data.local.datagenerator.savings
import app.tinygiants.getalife.domain.repository.GroupRepositoryFake
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class DeleteGroupUseCaseTest {

    private lateinit var deleteGroup: DeleteGroupUseCase
    private lateinit var groupRepositoryFake: GroupRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        groupRepositoryFake = GroupRepositoryFake()

        deleteGroup = DeleteGroupUseCase(repository = groupRepositoryFake)

        groupRepositoryFake.groupFlow.value = groups
    }

    @Test
    fun `Test deleting Group`(): Unit = runTest {
        val groupToBeDeleted = fixedCosts().toDomain()

        deleteGroup(group = groupToBeDeleted)

        val groupsLeft = groupRepositoryFake.groupFlow.value

        assertThat(groupsLeft).hasSize(3)
        assertThat(groupsLeft.first()).isEqualTo(dailyLife().toDomain())
        assertThat(groupsLeft.last()).isEqualTo(savings().toDomain())
    }
}