package app.tinygiants.getalife.data.repository

import aldiGroceriesJanuary
import aldiGroceriesMarch
import app.cash.turbine.test
import app.tinygiants.getalife.data.local.dao.TransactionDaoFake
import app.tinygiants.getalife.domain.model.TransactionDirection
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import eonElectricityMarch
import kotlinx.coroutines.test.runTest
import landlordRentJanuary
import landlordRentMarch
import lidlGroceriesMarch
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import techCorpSalaryJanuary
import transactionEntities

class TransactionRepositoryImplTest {

    private lateinit var fakeDao: TransactionDaoFake
    private lateinit var repository: TransactionRepositoryImpl

    @BeforeEach
    fun setUp() {
        fakeDao = TransactionDaoFake()
        repository = TransactionRepositoryImpl(fakeDao)
    }

    @Test
    fun `Test Transactions Flow`(): Unit = runTest {
        repository.getTransactions().test {
            val initialEmission = awaitItem()
            assertThat(initialEmission).isNotNull()
            assertThat(initialEmission).isEmpty()

            fakeDao.addTransaction(techCorpSalaryJanuary())
            val emission1 = awaitItem()
            assertThat(emission1).hasSize(1)
            assertThat(emission1.first().id).isEqualTo(techCorpSalaryJanuary().id)

            val updatedGroup = techCorpSalaryJanuary().copy(transactionPartner = "Arbeitgeber")
            fakeDao.updateTransaction(updatedGroup)
            val emission2 = awaitItem()
            assertThat(emission2.first().transactionPartner).isEqualTo("Arbeitgeber")

            fakeDao.deleteTransaction(techCorpSalaryJanuary())
            val emission3 = awaitItem()
            assertThat(emission3).isEmpty()

            fakeDao.transactions.value = transactionEntities()
            val finalEmission = awaitItem()

            assertThat(finalEmission).hasSize(21)
        }
    }

    @Test
    fun `Get Transactions for Account`(): Unit = runTest {
        fakeDao.transactions.value = transactionEntities()

        repository.getTransactionsByAccount(accountId = 1).test {
            val emission = awaitItem()
            assertThat(emission).hasSize(18)
            assertThat(emission.first()).isEqualTo(aldiGroceriesJanuary())
        }

        repository.getTransactionsByAccount(accountId = 2).test {
            val emission = awaitItem()
            assertThat(emission).hasSize(3)
            assertThat(emission.first()).isEqualTo(techCorpSalaryJanuary())
        }
    }

    @Test
    fun `Get Transactions for Category`(): Unit = runTest {
        fakeDao.transactions.value = transactionEntities()

        repository.getTransactionsByCategory(categoryId = 1L).test {
            val emission = awaitItem()
            assertThat(emission).hasSize(6)
            assertThat(emission.first()).isEqualTo(aldiGroceriesJanuary())
            assertThat(emission.last()).isEqualTo(lidlGroceriesMarch())
        }
    }

    @Test
    fun `Add transaction`(): Unit = runTest {
        repository.addTransaction(techCorpSalaryJanuary())

        val transactions = fakeDao.transactions.value
        assertThat(transactions).hasSize(1)
        assertThat(transactions.first()).isEqualTo(techCorpSalaryJanuary())
    }

    @Test
    fun `Update transaction`(): Unit = runTest {
        fakeDao.transactions.value = transactionEntities()

        val tobeUpdatedTransaction =
            techCorpSalaryJanuary().copy(transactionDirection = TransactionDirection.Outflow, transactionPartner = "Testpartner")
        repository.updateTransaction(tobeUpdatedTransaction)

        val updatedTransaction = fakeDao.transactions.value.find { it.id == techCorpSalaryJanuary().id }
        assertThat(updatedTransaction).isNotNull()
        assertThat(updatedTransaction?.transactionPartner).isEqualTo("Testpartner")
    }

    @Test
    fun `Delete Transaction`(): Unit = runTest{
        fakeDao.transactions.value = transactionEntities()

        repository.deleteTransaction(aldiGroceriesJanuary())

        val transactionsAfterFirstDeletion = fakeDao.transactions.value
        assertThat(transactionsAfterFirstDeletion).hasSize(20)
        assertThat(transactionsAfterFirstDeletion.find { it.id == aldiGroceriesJanuary().id }).isNull()
        assertThat(transactionsAfterFirstDeletion.first().transactionPartner).isEqualTo(techCorpSalaryJanuary().transactionPartner)
        assertThat(transactionsAfterFirstDeletion[1].transactionPartner).isEqualTo(landlordRentJanuary().transactionPartner)

        repository.deleteTransaction(eonElectricityMarch())

        val transactionsAfterSecondDeletion = fakeDao.transactions.value
        assertThat(transactionsAfterSecondDeletion).hasSize(19)
        assertThat(transactionsAfterSecondDeletion.find { it.id == eonElectricityMarch().id }).isNull()
        assertThat(transactionsAfterSecondDeletion[14].id).isEqualTo(landlordRentMarch().id)
        assertThat(transactionsAfterSecondDeletion[15].id).isEqualTo(aldiGroceriesMarch().id)
    }
}