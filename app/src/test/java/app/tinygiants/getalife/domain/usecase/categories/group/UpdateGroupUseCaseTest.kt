package app.tinygiants.getalife.domain.usecase.categories.group

import app.tinygiants.getalife.MainCoroutineExtension
import app.tinygiants.getalife.data.local.datagenerator.fixedCosts
import app.tinygiants.getalife.data.local.datagenerator.groups
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.GroupRepositoryFake
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class UpdateGroupUseCaseTest {

    private lateinit var updateGroup: UpdateGroupUseCase
    private lateinit var groupRepositoryFake: GroupRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val mainCoroutineExtension: MainCoroutineExtension = MainCoroutineExtension()
    }

    @BeforeEach
    fun setUp() {
        groupRepositoryFake = GroupRepositoryFake()

        updateGroup = UpdateGroupUseCase(
            repository = groupRepositoryFake,
            defaultDispatcher = mainCoroutineExtension.testDispatcher
        )

        groupRepositoryFake.groupFlow.value = groups
    }

    @Test
    fun `Test update group name`(): Unit = runTest {
        val groupToBeUpdated = fixedCosts().run {
            Group(
                id = id,
                name = "new Groupname",
                sumOfAvailableMoney = Money(0.0),
                listPosition = listPosition,
                isExpanded = isExpanded
            )
        }

        updateGroup(groupToBeUpdated)

        val groups = groupRepositoryFake.groupFlow.value

        assertThat(groups).hasSize(4)
        assertThat(groups.first().name).isEqualTo("new Groupname")
        assertThat(groups.first().listPosition).isEqualTo(fixedCosts().listPosition)
    }

    @Test
    fun `Test update list position`(): Unit = runTest {
        val groupToBeUpdated = fixedCosts().run {
            Group(
                id = id,
                name = name,
                sumOfAvailableMoney = Money(0.0),
                listPosition = 5,
                isExpanded = isExpanded
            )
        }

        updateGroup(groupToBeUpdated)

        val groups = groupRepositoryFake.groupFlow.value

        assertThat(groups).hasSize(4)
        assertThat(groups.first().name).isEqualTo(fixedCosts().name)
        assertThat(groups.first().listPosition).isEqualTo(5)
    }

    @Test
    fun `Test change isExpanded state`(): Unit = runTest {
        assertThat(groups.first().isExpanded).isFalse()

        val groupToBeUpdated = fixedCosts().run {
            Group(
                id = id,
                name = name,
                sumOfAvailableMoney = Money(0.0),
                listPosition = 5,
                isExpanded = true
            )
        }

        updateGroup(groupToBeUpdated)

        val groups = groupRepositoryFake.groupFlow.value

        assertThat(groups).hasSize(4)
        assertThat(groups.first().name).isEqualTo(fixedCosts().name)
        assertThat(groups.first().isExpanded).isTrue()
    }
}