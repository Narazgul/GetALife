package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Clock

/**
 * UseCase for making credit card payments following YNAB logic.
 * When paying a credit card, money is moved from the payment category to reduce the debt.
 */
class MakeCreditCardPaymentUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        creditCardAccount: Account,
        paymentCategory: Category,
        paymentAmount: Money,
        fromAccountId: Long
    ) = withContext(defaultDispatcher) {
        val fromAccount = accountRepository.getAccount(fromAccountId) ?: return@withContext

        // Create payment transaction that reduces credit card debt
        val paymentTransaction = createPaymentTransaction(
            creditCardAccount = creditCardAccount,
            paymentCategory = paymentCategory,
            paymentAmount = paymentAmount
        )

        // Update credit card balance (reduce debt)
        updateAccount(creditCardAccount, Money(-paymentAmount.asDouble()))

        // Update source account balance (reduce available money)
        updateAccount(fromAccount, Money(-paymentAmount.asDouble()))

        transactionRepository.addTransaction(paymentTransaction)
    }

    private suspend fun createPaymentTransaction(
        creditCardAccount: Account,
        paymentCategory: Category,
        paymentAmount: Money
    ): Transaction {
        val now = Clock.System.now()
        val transactionId = abs(Random.nextLong())

        return Transaction(
            id = transactionId,
            amount = Money(paymentAmount.asDouble()),
            account = creditCardAccount,
            category = paymentCategory,
            transactionPartner = "Credit Card Payment",
            transactionDirection = TransactionDirection.CreditCardPayment,
            description = "Credit card payment",
            dateOfTransaction = now,
            updatedAt = now,
            createdAt = now
        )
    }

    private suspend fun updateAccount(account: Account, amount: Money) {
        val updatedAccount = account.copy(
            balance = account.balance + amount,
            updatedAt = Clock.System.now()
        )
        accountRepository.updateAccount(account = updatedAccount)
    }
}