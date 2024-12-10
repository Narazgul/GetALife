package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.data.local.datagenerator.aldiGroceriesJanuary
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.checkingAccount
import app.tinygiants.getalife.data.local.datagenerator.groceriesCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.techCorpSalaryJanuary
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
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
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake
    private lateinit var transactionRepositoryFake: TransactionRepositoryFake

    companion object {
        @JvmField
        @RegisterExtension
        val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()
    }

    @BeforeEach
    fun setUp() {
        accountRepositoryFake = AccountRepositoryFake()
        categoryRepositoryFake = CategoryRepositoryFake()
        transactionRepositoryFake = TransactionRepositoryFake(accountRepositoryFake, categoryRepositoryFake)

        exchangeAccount = ExchangeAccountUseCase(
            accountRepository = accountRepositoryFake,
            transactionRepository = transactionRepositoryFake,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `Change Account for Inflow Transaction`(): Unit = runTest {
        accountRepositoryFake.accounts.value = listOf(
            cashAccount().toDomain(),
            checkingAccount().toDomain()
        )
        transactionRepositoryFake.transactions.value = listOf(
            techCorpSalaryJanuary().toDomain(
                account = checkingAccount().toDomain(),
                category = null
            )
        )

        val oldAccount = checkingAccount().toDomain()
        val newAccount = cashAccount().toDomain()
        val transaction = techCorpSalaryJanuary().toDomain(
            account = newAccount,
            category = null
        )

        exchangeAccount(transaction = transaction, oldAccount = oldAccount)

        val updatedOldAccount = accountRepositoryFake.accounts.value.last()
        assertThat(updatedOldAccount.balance).isEqualTo(Money(-799.5))

        val updatedNewAccount = accountRepositoryFake.accounts.value.first()
        assertThat(updatedNewAccount.balance).isEqualTo(Money(2500.0))

        val updatedTransaction = transactionRepositoryFake.transactions.value.first()
        assertThat(updatedTransaction.account.id).isEqualTo(cashAccount().id)
    }

    @Test
    fun `Change Account for Outflow Transaction`(): Unit = runTest {
        accountRepositoryFake.accounts.value = listOf(
            cashAccount().toDomain(),
            checkingAccount().toDomain()
        )
        transactionRepositoryFake.transactions.value = listOf(
            aldiGroceriesJanuary().toDomain(
                account = cashAccount().toDomain(),
                category = groceriesCategoryEntity().toDomain()
            )
        )

        val oldAccount = cashAccount().toDomain()
        val newAccount = checkingAccount().toDomain()
        val toBeUpdatedTransaction = aldiGroceriesJanuary().toDomain(
            account = newAccount,
            category = groceriesCategoryEntity().toDomain()
        )

        exchangeAccount(transaction = toBeUpdatedTransaction, oldAccount = oldAccount)

        val updatedOldAccount = accountRepositoryFake.accounts.value.first()
        assertThat(updatedOldAccount.balance).isEqualTo(Money(550.0))

        val updatedNewAccount = accountRepositoryFake.accounts.value.last()
        assertThat(updatedNewAccount.balance).isEqualTo(Money(1150.5))

        val updatedTransaction = transactionRepositoryFake.transactions.value.first()
        assertThat(updatedTransaction.account.id).isEqualTo(checkingAccount().id)
    }
}