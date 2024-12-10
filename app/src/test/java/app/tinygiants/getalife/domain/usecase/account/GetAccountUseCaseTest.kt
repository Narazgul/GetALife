package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAccountUseCaseTest {

    private lateinit var getAccount: GetAccountUseCase
    private lateinit var accountRepositoryFake: AccountRepositoryFake

    @BeforeEach
    fun setUp() {
        accountRepositoryFake = AccountRepositoryFake()
        getAccount = GetAccountUseCase(repository = accountRepositoryFake)
    }

    @Test
    fun `Get Account for accountId 1`(): Unit = runTest {
        accountRepositoryFake.accounts.value = accounts

        val account = getAccount(accountId = 1)

        val firstAccount = accounts.first()
        assertThat(account.name).isEqualTo(firstAccount.name)
        assertThat(account.balance).isEqualTo(firstAccount.balance)
        assertThat(account.listPosition).isEqualTo(firstAccount.listPosition)
    }

    @Test
    fun `Get Account for accountId 8`(): Unit = runTest {
        accountRepositoryFake.accounts.value = accounts

        val account = getAccount(accountId = 8)

        val lastAccount = accounts.last()
        assertThat(account.name).isEqualTo(lastAccount.name)
        assertThat(account.balance).isEqualTo(lastAccount.balance)
        assertThat(account.listPosition).isEqualTo(lastAccount.listPosition)
    }
}