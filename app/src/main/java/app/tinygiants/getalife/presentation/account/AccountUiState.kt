package app.tinygiants.getalife.presentation.account

data class AccountUiState(
    val title: String = "",
    val subtitle: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)