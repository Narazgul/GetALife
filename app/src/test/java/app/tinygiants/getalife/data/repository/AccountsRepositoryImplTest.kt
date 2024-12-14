package app.tinygiants.getalife.data.repository

import app.cash.turbine.test
import app.tinygiants.getalife.data.local.dao.AccountDaoFake
import app.tinygiants.getalife.data.local.datagenerator.accountEntities
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.checkingAccount
import app.tinygiants.getalife.data.local.datagenerator.savingsAccount
import app.tinygiants.getalife.data.local.datagenerator.secondCheckingAccount
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AccountsRepositoryImplTest {

    private lateinit var repository: AccountsRepositoryImpl
    private lateinit var fakeDao: AccountDaoFake

    @BeforeEach
    fun setUp() {
        fakeDao = AccountDaoFake()
        repository = AccountsRepositoryImpl(fakeDao)
    }

    @Test
    fun `Test AccountsFlow`(): Unit = runTest {
        repository.getAccountsFlow().test {
            val initialEmission = awaitItem()
            assertThat(initialEmission).isNotNull()
            assertThat(initialEmission).isEmpty()

            fakeDao.addAccount(cashAccount())
            val emission1 = awaitItem()
            assertThat(emission1).hasSize(1)
            assertThat(emission1.first().name).isEqualTo(cashAccount().name)

            val updatedAccount = cashAccount().copy(name = "Cash")
            fakeDao.updateAccount(updatedAccount)
            val emission2 = awaitItem()
            assertThat(emission2.first().name).isEqualTo("Cash")

            fakeDao.deleteAccount(cashAccount())
            val emission3 = awaitItem()
            assertThat(emission3).isEmpty()

            fakeDao.accounts.value = accountEntities
            val finalEmission = awaitItem()

            assertThat(finalEmission).hasSize(8)
        }
    }

    @Test
    fun `Get account`(): Unit = runTest {
        fakeDao.accounts.value = accountEntities

        val account = repository.getAccount(checkingAccount().id)

        assertThat(account).isNotNull()
        assertThat(account?.name).isEqualTo(checkingAccount().name)
    }

    @Test
    fun `Add account to list`(): Unit = runTest {
        val accounts = fakeDao.accounts.value
        assertThat(accounts).isEmpty()

        repository.addAccount(cashAccount().toDomain())

        val firstItem = fakeDao.accounts.value.first()
        assertThat(fakeDao.accounts.value).hasSize(1)
        assertThat(firstItem.name).isEqualTo(cashAccount().name)

        repository.addAccount(checkingAccount().toDomain())

        val secondItem = fakeDao.accounts.value[1]
        assertThat(fakeDao.accounts.value).hasSize(2)
        assertThat(secondItem.name).isEqualTo(checkingAccount().name)
    }

    @Test
    fun `Update account in list`(): Unit = runTest {
        fakeDao.accounts.value = accountEntities
        val firstItem = fakeDao.accounts.value.first()

        assertThat(firstItem.name).isEqualTo(cashAccount().name)

        val updatedEntity = cashAccount().copy(name = "Cash")
        repository.updateAccount(updatedEntity.toDomain())

        val updatedFirstItem = fakeDao.accounts.value.first()
        assertThat(updatedFirstItem.name).isEqualTo("Cash")
    }

    @Test
    fun `Delete account from list`(): Unit = runTest {
        fakeDao.accounts.value = accountEntities

        repository.deleteAccount(cashAccount().toDomain())

        val accountsAfterFirstDeletion = fakeDao.accounts.value
        assertThat(accountsAfterFirstDeletion).hasSize(7)
        assertThat(accountsAfterFirstDeletion.first().name).isEqualTo(checkingAccount().name)
        assertThat(accountsAfterFirstDeletion[1].name).isEqualTo(secondCheckingAccount().name)

        repository.deleteAccount(savingsAccount().toDomain())

        val accountsAfterSecondDeletion = fakeDao.accounts.value
        assertThat(accountsAfterSecondDeletion).hasSize(6)
        assertThat(accountsAfterSecondDeletion.first().name).isEqualTo(checkingAccount().name)
        assertThat(accountsAfterSecondDeletion[1].name).isEqualTo(secondCheckingAccount().name)
    }
}