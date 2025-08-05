package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.YearMonth

class TransactionRepositoryFake(
    private val accountRepository: AccountRepositoryFake,
    private val categoryRepository: CategoryRepositoryFake
) : TransactionRepository {

    val transactions = MutableStateFlow<List<Transaction>>(emptyList())

    override fun getTransactionsFlow() = transactions.map { list ->
        list.map { transaction ->
            transaction.copy(
                account = accountRepository.getAccount(transaction.account.id) ?: transaction.account,
                category = transaction.category?.let { categoryRepository.getCategory(it.id) ?: it }
            )
        }
    }

    override fun getTransactionsByAccountFlow(accountId: Long) = transactions.map { list ->
        list.filter { it.account.id == accountId }
            .map { transaction ->
                transaction.copy(
                    account = accountRepository.getAccount(transaction.account.id) ?: transaction.account,
                    category = transaction.category?.let { categoryRepository.getCategory(it.id) ?: it }
                )
            }
    }

    override fun getTransactionsByCategoryFlow(categoryId: Long) =
        transactions.map { list ->
            list.filter { it.category?.id == categoryId }
                .map { transaction ->
                    transaction.copy(
                        account = accountRepository.getAccount(transaction.account.id) ?: transaction.account,
                        category = transaction.category?.let { categoryRepository.getCategory(it.id) ?: it }
                    )
                }
        }

    override fun getSpentAmountByCategoryAndMonthFlow(categoryId: Long, yearMonth: YearMonth) =
        transactions.map { list ->
            val spent = list.filter { it.category?.id == categoryId }
                .sumOf { it.amount.asDouble() }
            Money(spent)
        }

    override suspend fun getTransaction(transactionId: Long): Transaction? {
        return transactions.value.find { it.id == transactionId }?.let { transaction ->
            transaction.copy(
                account = accountRepository.getAccount(transaction.account.id) ?: transaction.account,
                category = transaction.category?.let { categoryRepository.getCategory(it.id) ?: it }
            )
        }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        transactions.update { it + transaction }
        // Update account balance
        accountRepository.getAccount(transaction.account.id)?.let { account ->
            val updatedAccount = account.copy(balance = account.balance + transaction.amount)
            accountRepository.updateAccount(updatedAccount)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) =
        transactions.update { current -> current.map { if (it.id == transaction.id) transaction else it } }

    override suspend fun deleteTransaction(transaction: Transaction) =
        transactions.update { it.filterNot { entity -> entity.id == transaction.id } }

    override suspend fun getSpentAmountByCategoryAndMonth(categoryId: Long, yearMonth: YearMonth): Money {
        val spent = transactions.value
            .filter { it.category?.id == categoryId }
            .sumOf { it.amount.asDouble() }
        return Money(spent)
    }
}