package app.tinygiants.getalife.data.local.dao

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.TransactionDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlin.random.Random

class AccountsDaoFake : AccountsDao {

    private var accounts = (1..10).map { currentIndex ->
        AccountEntity(
            id = currentIndex.toLong(),
            name = "Account $currentIndex",
            balance = Random.nextDouble(from = -100.00, until = 100.00),
            type = AccountType.entries.toTypedArray().random(),
            listPosition = currentIndex - 1
        )
    }

    private var transactions = (1..10).map { currentIndex ->
        TransactionEntity(
            id = currentIndex.toLong(),
            accountId = Random.nextLong(from = 1, until = 10),
            categoryId = null,
            amount = Random.nextDouble(until = 1000.00),
            transactionPartner = "TransactionPartner $currentIndex",
            transactionDirection = TransactionDirection.entries.toTypedArray().random(),
            description = "",
            timestamp = Clock.System.now().plus(currentIndex, DateTimeUnit.MINUTE)
        )
    }

    override fun getAccounts(): Flow<List<AccountEntity>> = flow { emit(accounts) }

    override suspend fun getAccounts(): List<AccountEntity> = accounts

    override fun getAccountsWithTransactionsFlow(): Flow<List<AccountWithTransactionsEntity>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAccountsWithTransactions(): List<AccountWithTransactionsEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getAccount(accountId: Long): AccountEntity = accounts[accountId.toInt()]

    override suspend fun addAccount(accountEntity: AccountEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun updateAccount(accountEntity: AccountEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAccount(accountEntity: AccountEntity) {
        TODO("Not yet implemented")
    }

}