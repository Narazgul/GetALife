package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isNotEqualTo
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
        accountRepositoryFake.accountsFlow.value = accounts
    }

    @Test
    fun `Update account name`(): Unit = runTest {
        val accountToBeUpdated = accounts.first().run {
            Account(
                id = id,
                name = "new name",
                balance = Money(value = balance),
                type = type,
                listPosition = listPosition,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }

        updateAccount(account = accountToBeUpdated)

        val updatedAccount = accountRepositoryFake.accountsFlow.value.first()
        assertThat(updatedAccount.name).isEqualTo("new name")
        assertThat(updatedAccount.balance).isEqualTo(accounts.first().balance)
        assertThat(updatedAccount.listPosition).isEqualTo(accounts.first().listPosition)
        assertThat(updatedAccount.updatedAt).isGreaterThan(accounts.first().updatedAt)
        assertThat(updatedAccount.createdAt).isEqualTo(accounts.first().createdAt)
    }

    @Test
    fun `Update account balance`(): Unit = runTest {
        val accountToBeUpdated = accounts.first().run {
            Account(
                id = id,
                name = name,
                balance = Money(value = 1000.00),
                type = type,
                listPosition = listPosition,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }

        updateAccount(account = accountToBeUpdated)

        val updatedAccount = accountRepositoryFake.accountsFlow.value.first()
        assertThat(updatedAccount.name).isEqualTo(accounts.first().name)
        assertThat(updatedAccount.balance).isEqualTo(1000.00)
        assertThat(updatedAccount.listPosition).isEqualTo(accounts.first().listPosition)
        assertThat(updatedAccount.updatedAt).isNotEqualTo(accounts.first().updatedAt)
        assertThat(updatedAccount.createdAt).isEqualTo(accounts.first().createdAt)
    }

    @Test
    fun `Update account list position`(): Unit = runTest {
        val accountToBeUpdated = accounts.first().run {
            Account(
                id = id,
                name = name,
                balance = Money(value = balance),
                type = type,
                listPosition = 10,
                updatedAt = updatedAt,
                createdAt = createdAt
            )
        }

        updateAccount(account = accountToBeUpdated)

        val updatedAccount = accountRepositoryFake.accountsFlow.value.first()
        assertThat(updatedAccount.name).isEqualTo(accounts.first().name)
        assertThat(updatedAccount.balance).isEqualTo(accounts.first().balance)
        assertThat(updatedAccount.listPosition).isEqualTo(10)
        assertThat(updatedAccount.updatedAt).isNotEqualTo(accounts.first().updatedAt)
        assertThat(updatedAccount.createdAt).isEqualTo(accounts.first().createdAt)
    }
}