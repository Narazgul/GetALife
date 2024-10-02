package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.AccountsDao
import app.tinygiants.getalife.data.local.dao.AccountsDaoFake
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AccountsRepositoryImplTest {

    private lateinit var repository: AccountsRepositoryImpl
    private lateinit var dao: AccountsDao

    @BeforeEach
    fun setUp() {
        dao = AccountsDaoFake()
        repository = AccountsRepositoryImpl(dao)
    }

    @Test
    fun `Test getting accounts`(): Unit = runBlocking {
        val accounts = repository.getAccounts()

        assertThat(accounts.isNotEmpty()).isTrue()
        assertThat(accounts.size).isEqualTo(10)
        assertThat(accounts[0].id).isEqualTo(1)
    }
}