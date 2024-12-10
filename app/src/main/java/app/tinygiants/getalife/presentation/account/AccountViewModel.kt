package app.tinygiants.getalife.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.usecase.account.AddAccountUseCase
import app.tinygiants.getalife.domain.usecase.account.DeleteAccountUseCase
import app.tinygiants.getalife.domain.usecase.account.GetAccountsUseCase
import app.tinygiants.getalife.domain.usecase.account.UpdateAccountUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.GetCategoriesUseCase
import app.tinygiants.getalife.presentation.UiText
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val getAccounts: GetAccountsUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val addAccount: AddAccountUseCase,
    private val updateAccount: UpdateAccountUseCase,
    private val deleteAccount: DeleteAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AccountUiState(
            accounts = emptyList(),
            categories = emptyList(),
            isLoading = true,
            errorMessage = null
        )
    )
    val uiState = _uiState.asStateFlow()

    // region Init

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            launch {
                getAccounts()
                    .catch { throwable -> displayErrorState(throwable) }
                    .collect { result ->
                        result.onSuccess { accounts -> displayAccounts(accounts) }
                        result.onFailure { throwable -> displayErrorState(throwable) }
                    }
            }
            launch {
                getCategories()
                    .catch { throwable -> displayErrorState(throwable) }
                    .collect { categories -> displayCategories(categories) }
            }
        }
    }

    // endregion

    // region User interaction

    fun onUserClickEvent(clickEvent: UserClickEvent) {
        viewModelScope.launch {
            when (clickEvent) {

                is UserClickEvent.AddAccount -> addAccount(
                    name = clickEvent.name,
                    balance = clickEvent.balance,
                    type = clickEvent.type,
                    startingBalanceName = clickEvent.startingBalanceName,
                    startingBalanceDescription = clickEvent.startingBalanceDescription
                )

                is UserClickEvent.UpdateAccount -> updateAccount(account = clickEvent.account)
                is UserClickEvent.DeleteAccount -> deleteAccount(account = clickEvent.account)
            }
        }
    }

    // endregion

    // region Private Helper functions

    private fun displayAccounts(accounts: List<Account>) {
        _uiState.update { state ->
            state.copy(
                accounts = accounts,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    private fun displayCategories(categories: List<Category>) {
        _uiState.update { state ->
            state.copy(categories = categories)
        }
    }

    private fun displayErrorState(throwable: Throwable?) {
        if (throwable != null) Firebase.crashlytics.recordException(throwable)

        _uiState.update {
            AccountUiState(
                accounts = emptyList(),
                categories = emptyList(),
                isLoading = false,
                errorMessage = ErrorMessage(
                    title = UiText.StringResource(R.string.error_title),
                    subtitle = if (throwable?.message != null) UiText.DynamicString(throwable.message ?: "")
                    else UiText.StringResource(R.string.error_subtitle)
                )
            )
        }
    }

    // endregion
}