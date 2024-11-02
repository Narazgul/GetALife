package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteAccountUseCaseTest {

    private lateinit var deleteAccount: DeleteAccountUseCase
    private lateinit var accountRepositoryFake: AccountRepositoryFake

    @BeforeEach
    fun setUp() {
        accountRepositoryFake = AccountRepositoryFake()
        deleteAccount = DeleteAccountUseCase(repository = accountRepositoryFake)
    }

    @Test
    fun `Check account has been removed`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value = accounts
        val accountToBeDeleted = accounts.first().run {
            Account(
                id = id,
                name = name,
                balance = Money(value = balance),
                type = type,
                listPosition = listPosition,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }

        deleteAccount(account = accountToBeDeleted)

        val accountLeft = accountRepositoryFake.accountsFlow.value
        assertThat(accountLeft).hasSize(7)
        assertThat(accountLeft.first().name).isEqualTo("Checking Account")
        assertThat(accountLeft.first().listPosition).isEqualTo(1)
    }
}