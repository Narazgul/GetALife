package app.tinygiants.getalife.domain.usecase.account

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.account.DeleteAccountStatus.AccountHasTransactionsException
import app.tinygiants.getalife.domain.usecase.account.DeleteAccountStatus.SuccessfullyDeleted
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed class DeleteAccountStatus {
    data object SuccessfullyDeleted : DeleteAccountStatus()
    data class AccountHasTransactionsException(override val message: String) : Exception(message)
}

class DeleteAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
){

    suspend operator fun invoke(account: Account): Result<DeleteAccountStatus> {

        val transactions = transactionRepository.getTransactionsByAccountFlow(accountId = account.id).first()
        if (transactions.isNotEmpty())
            return Result.failure(AccountHasTransactionsException(message = "Account still has transactions"))

        accountRepository.deleteAccount(account = account)
        return Result.success(SuccessfullyDeleted)
    }
}