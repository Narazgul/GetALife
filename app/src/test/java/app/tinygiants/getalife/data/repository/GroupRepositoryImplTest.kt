package app.tinygiants.getalife.data.repository

import app.cash.turbine.test
import app.tinygiants.getalife.data.local.dao.GroupDaoFake
import app.tinygiants.getalife.data.local.datagenerator.dailyLife
import app.tinygiants.getalife.data.local.datagenerator.dreams
import app.tinygiants.getalife.data.local.datagenerator.fixedCosts
import app.tinygiants.getalife.data.local.datagenerator.groupEntities
import app.tinygiants.getalife.data.local.datagenerator.savings
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GroupRepositoryImplTest {

    private lateinit var fakeDao: GroupDaoFake
    private lateinit var repository: GroupRepositoryImpl

    @BeforeEach
    fun setUp() {
        fakeDao = GroupDaoFake()
        repository = GroupRepositoryImpl(fakeDao)
    }

    @Test
    fun `Get Groups Flow`(): Unit = runTest {
        repository.getGroupsFlow().test {
            val initialEmission = awaitItem()
            assertThat(initialEmission).isNotNull()
            assertThat(initialEmission).isEmpty()

            fakeDao.addGroup(fixedCosts())
            val emission1 = awaitItem()
            assertThat(emission1).hasSize(1)
            assertThat(emission1.first().name).isEqualTo(fixedCosts().name)

            val updatedGroup = fixedCosts().copy(name = "updated fixed costs")
            fakeDao.updateGroup(updatedGroup)
            val emission2 = awaitItem()
            assertThat(emission2.first().name).isEqualTo("updated fixed costs")

            fakeDao.deleteGroup(fixedCosts())
            val emission3 = awaitItem()
            assertThat(emission3).isEmpty()

            fakeDao.groups.value = groupEntities
            val finalEmission = awaitItem()

            assertThat(finalEmission).hasSize(4)
        }
    }

    @Test
    fun `Add group to list`(): Unit = runTest {
        val groups = fakeDao.groups.value
        assertThat(groups).isEmpty()

        repository.addGroup(fixedCosts().toDomain())

        val firstItem = fakeDao.groups.value.first()
        assertThat(fakeDao.groups.value).hasSize(1)
        assertThat(firstItem.name).isEqualTo(fixedCosts().name)

        repository.addGroup(dailyLife().toDomain())

        val secondItem = fakeDao.groups.value[1]
        assertThat(fakeDao.groups.value).hasSize(2)
        assertThat(secondItem.name).isEqualTo(dailyLife().name)
    }

    @Test
    fun `Update group in list`(): Unit = runTest {
        fakeDao.groups.value = groupEntities
        val firstItem = fakeDao.groups.value.first()

        assertThat(firstItem.name).isEqualTo(fixedCosts().name)

        val updatedEntity = fixedCosts().copy(name = "Fixed costs")
        repository.updateGroup(updatedEntity.toDomain())

        val updatedFirstItem = fakeDao.groups.value.first()
        assertThat(updatedFirstItem.name).isEqualTo("Fixed costs")
    }

    @Test
    fun `Delete group from list`(): Unit = runTest {
        fakeDao.groups.value = groupEntities

        repository.deleteGroup(fixedCosts().toDomain())

        val groupsAfterFirstDeletion = fakeDao.groups.value
        assertThat(groupsAfterFirstDeletion).hasSize(3)
        assertThat(groupsAfterFirstDeletion.find { it.name == fixedCosts().name }).isNull()
        assertThat(groupsAfterFirstDeletion.first().name).isEqualTo(dailyLife().name)
        assertThat(groupsAfterFirstDeletion[1].name).isEqualTo(dreams().name)

        repository.deleteGroup(dailyLife().toDomain())

        val groupsAfterSecondDeletion = fakeDao.groups.value
        assertThat(groupsAfterSecondDeletion).hasSize(2)
        assertThat(groupsAfterSecondDeletion.first().name).isEqualTo(dreams().name)
        assertThat(groupsAfterSecondDeletion[1].name).isEqualTo(savings().name)
    }
}