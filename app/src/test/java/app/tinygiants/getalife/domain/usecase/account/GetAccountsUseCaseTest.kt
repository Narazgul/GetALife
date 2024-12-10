package app.tinygiants.getalife.domain.usecase.account

import app.cash.turbine.test
import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.accountEntities
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class GetAccountsUseCaseTest {

    private lateinit var getAccounts: GetAccountsUseCase
    private lateinit var accountRepositoryFake: AccountRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        accountRepositoryFake = AccountRepositoryFake()

        getAccounts = GetAccountsUseCase(
            accountRepository = accountRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `Test getting accounts`(): Unit = runTest {
        accountRepositoryFake.accounts.value = accounts

        getAccounts().test {
            val receivedAccounts = awaitItem().getOrNull()
            assertThat(receivedAccounts).isNotNull()
            assertThat(receivedAccounts!!).hasSize(8)
            assertThat(receivedAccounts.first().name).isEqualTo(accountEntities.first().name)
            assertThat(receivedAccounts.last().name).isEqualTo(accountEntities.last().name)
        }
    }
}