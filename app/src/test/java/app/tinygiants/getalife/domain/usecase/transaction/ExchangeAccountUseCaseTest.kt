package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.aldiGroceriesJanuary
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.checkingAccount
import app.tinygiants.getalife.data.local.datagenerator.techCorpSalaryJanuary
import app.tinygiants.getalife.data.local.datagenerator.toAccount
import app.tinygiants.getalife.data.local.datagenerator.toTransaction
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.TransactionRepositoryFake
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ExchangeAccountUseCaseTest {

    private lateinit var exchangeAccount: ExchangeAccountUseCase
    private lateinit var accountRepositoryFake: AccountRepositoryFake
    private lateinit var transactionRepositoryFake: TransactionRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        accountRepositoryFake = AccountRepositoryFake()
        transactionRepositoryFake = TransactionRepositoryFake()

        exchangeAccount = ExchangeAccountUseCase(
            accountRepository = accountRepositoryFake,
            transactionRepository = transactionRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher)
    }

    @Test
    fun `Change Account for Inflow Transaction`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value = listOf(cashAccount(), checkingAccount())
        transactionRepositoryFake.transactions.value = listOf(techCorpSalaryJanuary())

        val oldAccount = checkingAccount().toAccount()
        val transaction = techCorpSalaryJanuary().toTransaction(account = cashAccount().toAccount())

        exchangeAccount(transaction = transaction, oldAccount = oldAccount)

        val updatedOldAccount = accountRepositoryFake.accountsFlow.value.last()
        assertThat(updatedOldAccount.balance).isEqualTo(-799.5)

        val updatedNewAccount = accountRepositoryFake.accountsFlow.value.first()
        assertThat(updatedNewAccount.balance).isEqualTo(2500.0)

        val updatedTransaction = transactionRepositoryFake.transactions.value.first()
        assertThat(updatedTransaction.accountId).isEqualTo(cashAccount().id)
    }

    @Test
    fun `Change Account for Outflow Transaction`(): Unit = runTest {
        accountRepositoryFake.accountsFlow.value = listOf(cashAccount(), checkingAccount())
        transactionRepositoryFake.transactions.value = listOf(aldiGroceriesJanuary())

        val oldAccount = cashAccount().toAccount()
        val transaction = aldiGroceriesJanuary().toTransaction(account = checkingAccount().toAccount())

        exchangeAccount(transaction = transaction, oldAccount = oldAccount)

        val updatedOldAccount = accountRepositoryFake.accountsFlow.value.first()
        assertThat(updatedOldAccount.balance).isEqualTo(550.0)

        val updatedNewAccount = accountRepositoryFake.accountsFlow.value.last()
        assertThat(updatedNewAccount.balance).isEqualTo(1150.5)

        val updatedTransaction = transactionRepositoryFake.transactions.value.first()
        assertThat(updatedTransaction.accountId).isEqualTo(checkingAccount().id)
    }
}