package app.tinygiants.getalife.data.repository

import app.cash.turbine.test
import app.tinygiants.getalife.data.local.dao.TransactionDaoFake
import app.tinygiants.getalife.data.local.datagenerator.accounts
import app.tinygiants.getalife.data.local.datagenerator.aldiGroceriesJanuary
import app.tinygiants.getalife.data.local.datagenerator.aldiGroceriesMarch
import app.tinygiants.getalife.data.local.datagenerator.cashAccount
import app.tinygiants.getalife.data.local.datagenerator.categories
import app.tinygiants.getalife.data.local.datagenerator.checkingAccount
import app.tinygiants.getalife.data.local.datagenerator.eonElectricityMarch
import app.tinygiants.getalife.data.local.datagenerator.groceriesCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.landlordRentJanuary
import app.tinygiants.getalife.data.local.datagenerator.landlordRentMarch
import app.tinygiants.getalife.data.local.datagenerator.rentCategoryEntity
import app.tinygiants.getalife.data.local.datagenerator.techCorpSalaryJanuary
import app.tinygiants.getalife.data.local.datagenerator.transactionEntities
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepositoryFake
import app.tinygiants.getalife.domain.repository.CategoryRepositoryFake
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TransactionRepositoryImplTest {

    private lateinit var repository: TransactionRepositoryImpl
    private lateinit var transactionDaoFake: TransactionDaoFake
    private lateinit var accountRepositoryFake: AccountRepositoryFake
    private lateinit var categoryRepositoryFake: CategoryRepositoryFake

    @BeforeEach
    fun setUp() {
        transactionDaoFake = TransactionDaoFake()
        accountRepositoryFake = AccountRepositoryFake()
        categoryRepositoryFake = CategoryRepositoryFake()

        repository = TransactionRepositoryImpl(
            transactionDao = transactionDaoFake,
            accountRepository = accountRepositoryFake,
            categoryRepository = categoryRepositoryFake
        )
    }

    @Test
    fun `Test Transactions Flow`(): Unit = runTest {
        accountRepositoryFake.accounts.value = accounts
        categoryRepositoryFake.categories.value = categories

        repository.getTransactionsFlow().test {
            val initialEmission = awaitItem()
            assertThat(initialEmission).isNotNull()
            assertThat(initialEmission).isEmpty()

            transactionDaoFake.addTransaction(techCorpSalaryJanuary())
            val emission1 = awaitItem()
            assertThat(emission1).hasSize(1)
            assertThat(emission1.first().id).isEqualTo(techCorpSalaryJanuary().id)

            val updatedGroup = techCorpSalaryJanuary().copy(transactionPartner = "Employer")
            transactionDaoFake.updateTransaction(updatedGroup)
            val emission2 = awaitItem()
            assertThat(emission2.first().transactionPartner).isEqualTo("Employer")

            transactionDaoFake.deleteTransaction(techCorpSalaryJanuary())
            val emission3 = awaitItem()
            assertThat(emission3).isEmpty()

            transactionDaoFake.transactions.value = transactionEntities
            val finalEmission = awaitItem()

            assertThat(finalEmission).hasSize(21)
        }
    }

    @Test
    fun `Get Transactions for Account`(): Unit = runTest {
        accountRepositoryFake.accounts.value = listOf(cashAccount().toDomain(), checkingAccount().toDomain())
        categoryRepositoryFake.categories.value = categories
        transactionDaoFake.transactions.value = transactionEntities

        repository.getTransactionsByAccountFlow(accountId = 1).test {
            val emission = awaitItem()
            assertThat(emission).hasSize(18)
            assertThat(emission.first()).isEqualTo(
                aldiGroceriesJanuary().toDomain(
                    cashAccount().toDomain(),
                    groceriesCategoryEntity().toDomain()
                )
            )
        }

        repository.getTransactionsByAccountFlow(accountId = 2).test {
            val emission = awaitItem()
            assertThat(emission).hasSize(2)
            assertThat(emission.first()).isEqualTo(
                techCorpSalaryJanuary().toDomain(
                    checkingAccount().toDomain(),
                    null
                )
            )
        }
    }

    @Test
    fun `Get Transactions for Category`(): Unit = runTest {
        transactionDaoFake.transactions.value = transactionEntities
        categoryRepositoryFake.categories.value = categories
        accountRepositoryFake.accounts.value = accounts

        repository.getTransactionsByCategoryFlow(categoryId = 1L).test {
            val emission = awaitItem()
            assertThat(emission).hasSize(3)
            assertThat(emission.first()).isEqualTo(
                landlordRentJanuary().toDomain(
                    cashAccount().toDomain(), rentCategoryEntity().toDomain()
                )
            )
            assertThat(emission.last()).isEqualTo(
                landlordRentMarch().toDomain(
                    cashAccount().toDomain(), rentCategoryEntity().toDomain()
                )
            )
        }
    }

    @Test
    fun `Add transaction`(): Unit = runTest {
        val account = checkingAccount().toDomain()
        repository.addTransaction(transaction = techCorpSalaryJanuary().toDomain(account, null))

        val transactions = transactionDaoFake.transactions.value
        assertThat(transactions).hasSize(1)
        assertThat(transactions.first()).isEqualTo(techCorpSalaryJanuary())
    }

    @Test
    fun `Update transaction`(): Unit = runTest {
        transactionDaoFake.transactions.value = transactionEntities
        val account = cashAccount().toDomain()
        val category = rentCategoryEntity().toDomain()

        val tobeUpdatedTransaction =
            techCorpSalaryJanuary().copy(transactionDirection = TransactionDirection.Outflow, transactionPartner = "Seven")
        repository.updateTransaction(tobeUpdatedTransaction.toDomain(account, category))

        val updatedTransaction = transactionDaoFake.transactions.value.find { it.id == techCorpSalaryJanuary().id }
        assertThat(updatedTransaction).isNotNull()
        assertThat(updatedTransaction?.transactionPartner).isEqualTo("Seven")
    }

    @Test
    fun `Delete Transaction`(): Unit = runTest {
        transactionDaoFake.transactions.value = transactionEntities
        val account = cashAccount().toDomain()
        val category = rentCategoryEntity().toDomain()

        repository.deleteTransaction(aldiGroceriesJanuary().toDomain(account, category))

        val transactionsAfterFirstDeletion = transactionDaoFake.transactions.value
        assertThat(transactionsAfterFirstDeletion).hasSize(20)
        assertThat(transactionsAfterFirstDeletion.find { it.id == aldiGroceriesJanuary().id }).isNull()
        assertThat(transactionsAfterFirstDeletion.first().transactionPartner).isEqualTo(techCorpSalaryJanuary().transactionPartner)
        assertThat(transactionsAfterFirstDeletion[1].transactionPartner).isEqualTo(landlordRentJanuary().transactionPartner)

        repository.deleteTransaction(eonElectricityMarch().toDomain(account, category))

        val transactionsAfterSecondDeletion = transactionDaoFake.transactions.value
        assertThat(transactionsAfterSecondDeletion).hasSize(19)
        assertThat(transactionsAfterSecondDeletion.find { it.id == eonElectricityMarch().id }).isNull()
        assertThat(transactionsAfterSecondDeletion[14].id).isEqualTo(landlordRentMarch().id)
        assertThat(transactionsAfterSecondDeletion[15].id).isEqualTo(aldiGroceriesMarch().id)
    }
}