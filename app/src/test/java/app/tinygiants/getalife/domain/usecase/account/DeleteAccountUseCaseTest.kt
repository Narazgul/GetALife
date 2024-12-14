package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteAccountUseCaseTest {

    private lateinit var deleteAccount: DeleteAccountUseCase
    private lateinit var accountRepositoryFake: AccountRepositoryFake
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake
    private lateinit var transactionRepositoryFake: TransactionRepositoryFake

    @BeforeEach
    fun setUp() {
        accountRepositoryFake = AccountRepositoryFake()
        categoryRepositoryFake = CategoryRepositoryFake()
        transactionRepositoryFake = TransactionRepositoryFake(
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake
        )
        deleteAccount = DeleteAccountUseCase(
            accountRepository = accountRepositoryFake,
            transactionRepository = transactionRepositoryFake
        )
    }

    @Test
    fun `Check account has been removed`(): Unit = runTest {
        accountRepositoryFake.accounts.value = accounts
        val accountToBeDeleted = accounts.first()

        deleteAccount(account = accountToBeDeleted)

        val accountLeft = accountRepositoryFake.accounts.value
        assertThat(accountLeft).hasSize(7)
        assertThat(accountLeft.first().name).isEqualTo("Checking Account")
        assertThat(accountLeft.first().listPosition).isEqualTo(1)
    }
}