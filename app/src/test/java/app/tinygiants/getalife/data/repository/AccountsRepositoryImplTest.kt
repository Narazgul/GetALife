package app.tinygiants.getalife.data.repository

import app.cash.turbine.test
import app.tinygiants.getalife.data.local.dao.AccountDaoFake
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.cashAccountEntity
import app.tinygiants.getalife.data.local.datagenerator.checkingAccountEntity
import app.tinygiants.getalife.data.local.datagenerator.creditCardAccountEntity
import app.tinygiants.getalife.data.local.datagenerator.savingsAccountEntity
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
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

            fakeDao.addAccount(cashAccountEntity())
            val emission1 = awaitItem()
            assertThat(emission1).hasSize(1)
            assertThat(emission1.first().name).isEqualTo(cashAccountEntity().name)

            val updatedAccount = cashAccountEntity().copy(name = "Cash")
            fakeDao.updateAccount(updatedAccount)
            val emission2 = awaitItem()
            assertThat(emission2.first().name).isEqualTo("Cash")

            fakeDao.deleteAccount(cashAccountEntity())
            val emission3 = awaitItem()
            assertThat(emission3).isEmpty()

            fakeDao.accounts.value = accounts
            val finalEmission = awaitItem()

            assertThat(finalEmission).hasSize(7)
        }
    }

    @Test
    fun `Accounts list is empty`(): Unit = runTest {
        val accounts = repository.getAccounts()

        assertThat(accounts).isEmpty()
        assertThat(accounts).isNotNull()
    }

    @Test
    fun `Get accounts list`(): Unit = runTest {
        fakeDao.accounts.value = accounts

        val accounts = repository.getAccounts()

        assertThat(accounts).isNotEmpty()
        assertThat(accounts).hasSize(7)
    }

    @Test
    fun `Get account`(): Unit = runTest {
        fakeDao.accounts.value = accounts

        val account = repository.getAccount(checkingAccountEntity().id)

        assertThat(account).isNotNull()
        assertThat(account.name).isEqualTo(checkingAccountEntity().name)
    }

    @Test
    fun `Add account to list`(): Unit = runTest {
        val accounts = repository.getAccounts()
        assertThat(accounts).isEmpty()

        repository.addAccount(cashAccountEntity())

        val firstItem = fakeDao.accounts.value.first()
        assertThat(fakeDao.accounts.value).hasSize(1)
        assertThat(firstItem.name).isEqualTo(cashAccountEntity().name)

        repository.addAccount(checkingAccountEntity())

        val secondItem = fakeDao.accounts.value[1]
        assertThat(fakeDao.accounts.value).hasSize(2)
        assertThat(secondItem.name).isEqualTo(checkingAccountEntity().name)
    }

    @Test
    fun `Update account in list`(): Unit = runTest {
        fakeDao.accounts.value = accounts
        val firstItem = fakeDao.accounts.value.first()

        assertThat(firstItem.name).isEqualTo(cashAccountEntity().name)

        val updatedEntity = cashAccountEntity().copy(name = "Cash")
        repository.updateAccount(updatedEntity)

        val updatedFirstItem = fakeDao.accounts.value.first()
        assertThat(updatedFirstItem.name).isEqualTo("Cash")
    }

    @Test
    fun `Delete account from list`(): Unit = runTest {
        fakeDao.accounts.value = accounts

        repository.deleteAccount(cashAccountEntity())

        val accountsAfterFirstDeletion = fakeDao.accounts.value
        assertThat(accountsAfterFirstDeletion).hasSize(6)
        assertThat(accountsAfterFirstDeletion.first().name).isEqualTo(checkingAccountEntity().name)
        assertThat(accountsAfterFirstDeletion[1].name).isEqualTo(savingsAccountEntity().name)

        repository.deleteAccount(savingsAccountEntity())

        val accountsAfterSecondDeletion = fakeDao.accounts.value
        assertThat(accountsAfterSecondDeletion).hasSize(5)
        assertThat(accountsAfterSecondDeletion.first().name).isEqualTo(checkingAccountEntity().name)
        assertThat(accountsAfterSecondDeletion[1].name).isEqualTo(creditCardAccountEntity().name)
    }
}