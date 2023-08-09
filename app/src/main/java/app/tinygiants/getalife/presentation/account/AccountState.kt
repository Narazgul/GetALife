package app.tinygiants.getalife.presentation.account

data class AccountState(
    val title: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)