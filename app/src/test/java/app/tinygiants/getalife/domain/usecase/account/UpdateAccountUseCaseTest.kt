package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateAccountUseCaseTest {

    private lateinit var updateAccount: UpdateAccountUseCase
    private lateinit var accountRepositoryFake: AccountRepositoryFake

    @BeforeEach
    fun setUp() {
        accountRepositoryFake = AccountRepositoryFake()

        updateAccount = UpdateAccountUseCase(repository = accountRepositoryFake)
        accountRepositoryFake.accounts.value = accounts
    }

    @Test
    fun `Update account name`(): Unit = runTest {
        val accountToBeUpdated = accounts.first().copy(name = "new name")

        updateAccount(account = accountToBeUpdated)

        val updatedAccount = accountRepositoryFake.accounts.value.first()
        val cashAccount = accounts.first()
        assertThat(updatedAccount.name).isEqualTo("new name")
        assertThat(updatedAccount.balance).isEqualTo(cashAccount.balance)
        assertThat(updatedAccount.listPosition).isEqualTo(cashAccount.listPosition)
    }

    @Test
    fun `Update account balance`(): Unit = runTest {
        val accountToBeUpdated = accounts.first().copy(balance = Money(1000.00))

        updateAccount(account = accountToBeUpdated)

        val updatedAccount = accountRepositoryFake.accounts.value.first()
        val cashAccount = accounts.first()
        assertThat(updatedAccount.name).isEqualTo(cashAccount.name)
        assertThat(updatedAccount.balance).isEqualTo(Money(1000.00))
        assertThat(updatedAccount.listPosition).isEqualTo(cashAccount.listPosition)
    }

    @Test
    fun `Update account list position`(): Unit = runTest {
        val accountToBeUpdated = accounts.first().copy(listPosition = 10)

        updateAccount(account = accountToBeUpdated)

        val updatedAccount = accountRepositoryFake.accounts.value.first()
        val cashAccount = accounts.first()
        assertThat(updatedAccount.name).isEqualTo(cashAccount.name)
        assertThat(updatedAccount.balance).isEqualTo(cashAccount.balance)
        assertThat(updatedAccount.listPosition).isEqualTo(10)
    }
}