package app.tinygiants.getalife.presentation.account

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage

data class AccountUiState(
    val accounts: List<Account>,
    val categories: List<Category>,
    val isLoading: Boolean,
    val errorMessage: ErrorMessage?
)

sealed class UserClickEvent {
    data class AddAccount(val name: String, val balance: Money, val type: AccountType) : UserClickEvent()
    data class UpdateAccount(val account: Account) : UserClickEvent()
    data class DeleteAccount(val account: Account) : UserClickEvent()
}