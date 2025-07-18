package app.tinygiants.getalife.domain.usecase.transaction

import app.cash.turbine.test
import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.transactions
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import assertk.assertThat
import assertk.assertions.hasSize
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class GetTransactionsForAccountUseCaseTest {

    private lateinit var getTransactionsForAccount: GetTransactionsForAccountUseCase
    private lateinit var transactionsRepositoryFake: TransactionRepositoryFake
    private lateinit var accountsRepositoryFake: AccountRepositoryFake
    private lateinit var categoriesRepositoryFake: CategoryRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        accountsRepositoryFake = AccountRepositoryFake()
        categoriesRepositoryFake = CategoryRepositoryFake()
        transactionsRepositoryFake = TransactionRepositoryFake(accountsRepositoryFake, categoriesRepositoryFake)

        getTransactionsForAccount = GetTransactionsForAccountUseCase(
            transactions = transactionsRepositoryFake,
            accounts = accountsRepositoryFake,
            categories = categoriesRepositoryFake,
        )

        transactionsRepositoryFake.transactions.value = transactions
        accountsRepositoryFake.accounts.value = accounts
        categoriesRepositoryFake.categories.value = categories
    }

    @Test
    fun `Retrieve transactions for Cash Account with id 1`(): Unit = runTest {
        getTransactionsForAccount(accountId = 1L).test {
            val transactions = awaitItem().getOrThrow()
            assertThat(transactions).hasSize(18)
        }
    }

    @Test
    fun `Retrieve transactions for Checking Account with id 2`(): Unit = runTest {
        getTransactionsForAccount(accountId = 2L).test {
            val transactions = awaitItem().getOrThrow()
            assertThat(transactions).hasSize(2)
        }
    }
}