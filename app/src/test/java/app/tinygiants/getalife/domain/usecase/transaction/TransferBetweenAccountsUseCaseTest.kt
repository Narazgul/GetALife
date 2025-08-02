package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.TestDispatcherExtension
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock

class TransferBetweenAccountsUseCaseTest {

    @get:Rule
    val testDispatcherExtension: TestDispatcherExtension = TestDispatcherExtension()

    private lateinit var accountRepository: AccountRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var transferBetweenAccounts: TransferBetweenAccountsUseCase

    private val fromAccount = Account(
        id = 1L,
        name = "Checking Account",
        balance = Money(1000.0),
        type = AccountType.Checking,
        listPosition = 0,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )

    private val toAccount = Account(
        id = 2L,
        name = "Savings Account",
        balance = Money(500.0),
        type = AccountType.Savings,
        listPosition = 1,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )

    @Before
    fun setup() {
        accountRepository = mockk()
        transactionRepository = mockk()
        transferBetweenAccounts = TransferBetweenAccountsUseCase(
            accountRepository = accountRepository,
            transactionRepository = transactionRepository,
            defaultDispatcher = testDispatcherExtension.testDispatcher
        )
    }

    @Test
    fun `should transfer money between accounts correctly`() = runTest {
        // Given
        val transferAmount = Money(200.0)
        coEvery { accountRepository.updateAccount(any()) } returns Unit
        coEvery { transactionRepository.addTransaction(any()) } returns Unit

        // When
        transferBetweenAccounts(
            fromAccount = fromAccount,
            toAccount = toAccount,
            amount = transferAmount,
            description = "Test Transfer"
        )

        // Then
        coVerify {
            accountRepository.updateAccount(
                match { it.id == fromAccount.id && it.balance == Money(800.0) }
            )
        }
        coVerify {
            accountRepository.updateAccount(
                match { it.id == toAccount.id && it.balance == Money(700.0) }
            )
        }
        coVerify {
            transactionRepository.addTransaction(
                match {
                    it.amount == transferAmount &&
                            it.transactionDirection == TransactionDirection.AccountTransfer &&
                            it.account.id == fromAccount.id
                }
            )
        }
    }

    @Test
    fun `should not transfer if same account`() = runTest {
        // Given
        val transferAmount = Money(200.0)

        // When
        transferBetweenAccounts(
            fromAccount = fromAccount,
            toAccount = fromAccount,
            amount = transferAmount
        )

        // Then
        coVerify(exactly = 0) { accountRepository.updateAccount(any()) }
        coVerify(exactly = 0) { transactionRepository.addTransaction(any()) }
    }

    @Test
    fun `should not transfer if amount is zero or negative`() = runTest {
        // Given
        val zeroAmount = Money(0.0)
        val negativeAmount = Money(-100.0)

        // When
        transferBetweenAccounts(fromAccount, toAccount, zeroAmount)
        transferBetweenAccounts(fromAccount, toAccount, negativeAmount)

        // Then
        coVerify(exactly = 0) { accountRepository.updateAccount(any()) }
        coVerify(exactly = 0) { transactionRepository.addTransaction(any()) }
    }
}