package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TransactionRepositoryFake(
    private val accountRepository: AccountRepositoryFake,
    private val categoryRepository: CategoryRepositoryFake
) : TransactionRepository {

    val transactions = MutableStateFlow<List<Transaction>>(emptyList())

    override fun getTransactionsFlow() = transactions.map { list ->
            list.map { transaction ->
                transaction.copy(
                    account = accountRepository.getAccount(transaction.account.id),
                    category = transaction.category?.let { categoryRepository.getCategory(it.id) }
                )
            }
        }

    override fun getTransactionsByAccountFlow(accountId: Long) = transactions.map { list ->
            list.filter { it.account.id == accountId }
                .map { transaction ->
                    transaction.copy(
                        account = accountRepository.getAccount(transaction.account.id),
                        category = transaction.category?.let { categoryRepository.getCategory(it.id) }
                    )
                }
        }

    override fun getTransactionsByCategoryFlow(categoryId: Long) =
        transactions.map { list ->
            list.filter { it.category?.id == categoryId }
                .map { transaction ->
                    transaction.copy(
                        account = accountRepository.getAccount(transaction.account.id),
                        category = transaction.category?.let { categoryRepository.getCategory(it.id) }
                    )
                }
        }

    override suspend fun getTransaction(transactionId: Long): Transaction? {
        return transactions.value.find { it.id == transactionId }?.let { transaction ->
            transaction.copy(
                account = accountRepository.getAccount(transaction.account.id),
                category = transaction.category?.let { categoryRepository.getCategory(it.id) }
            )
        }
    }

    override suspend fun addTransaction(transaction: Transaction) =
        transactions.update { it + transaction }

    override suspend fun updateTransaction(transaction: Transaction) =
        transactions.update { current -> current.map { if (it.id == transaction.id) transaction else it } }

    override suspend fun deleteTransaction(transaction: Transaction) =
        transactions.update { it.filterNot { entity -> entity.id == transaction.id } }
}