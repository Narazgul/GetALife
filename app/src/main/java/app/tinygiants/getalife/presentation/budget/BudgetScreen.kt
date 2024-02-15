package app.tinygiants.getalife.presentation.budget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Header
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.budget.composables.BudgetsList
import app.tinygiants.getalife.presentation.budget.composables.ErrorMessage
import app.tinygiants.getalife.presentation.budget.composables.LoadingIndicator
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.LightAndDarkPreviews
import app.tinygiants.getalife.theme.spacing

@Composable
fun BudgetScreen() {
    val viewModel: BudgetViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BudgetScreen(
        uiState = uiState,
        onUserClickEvent = viewModel::onUserClickEvent
    )
}

@Composable
fun BudgetScreen(
    uiState: BudgetUiState,
    onUserClickEvent: (UserClickEvent) -> Unit = { }
) {
    Box(modifier = Modifier.fillMaxSize()) {
        BudgetsList(
            groups = uiState.groups,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            onUserClickEvent = onUserClickEvent,
        )
        LoadingIndicator(
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage
        )
        ErrorMessage(uiState.errorMessage)

        var headerName by rememberSaveable { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current

        Row(
            modifier = Modifier
                .padding(spacing.default)
                .align(Alignment.BottomCenter)
        ) {
            OutlinedTextField(
                value = headerName,
                onValueChange = { newText -> headerName = newText },
                label = { Text("Gruppenname eingeben") },

                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(spacing.default))
            Button(
                onClick = {
                    onUserClickEvent(UserClickEvent.AddHeader(name = headerName))
                    headerName = ""
                    keyboardController?.hide()
                },
                modifier = Modifier.align(Alignment.CenterVertically)

            ) {
                Text(text = "Save")
            }
        }
    }
}

@LightAndDarkPreviews
@Composable
fun BudgetScreenPreview(
) {
    GetALifeTheme {
        Surface {
            BudgetScreen(
                BudgetUiState(
                    groups = fakeCategories(),
                    isLoading = false,
                    errorMessage = null
                )
            )
        }
    }
}

@LightAndDarkPreviews
@Composable
fun BudgetScreenLoadingPreview() {
    GetALifeTheme {
        Surface {
            BudgetScreen(
                BudgetUiState(
                    groups = emptyMap(),
                    isLoading = true,
                    errorMessage = null
                )
            )
        }
    }
}

@LightAndDarkPreviews
@Composable
fun BudgetScreenErrorPreview() {
    GetALifeTheme {
        Surface {
            BudgetScreen(
                BudgetUiState(
                    groups = emptyMap(),
                    isLoading = true,
                    errorMessage = ErrorMessage(
                        title = "Zefix",
                        subtitle = "Ein fürchterlicher Fehler ist aufgetreten."
                    )
                )
            )
        }
    }
}

fun fakeCategories() = mapOf(
    Header(
        id = 0,
        name = "Fixed Costs",
        sumOfAvailableMoney = Money(value = 1015.00),
        isExpanded = true
    ) to listOf(
        Category(
            id = 1,
            headerId = 0,
            name = "Rent",
            budgetTarget = Money(value = 1000.00),
            availableMoney = Money(value = 1000.00),
            progress = (1000.00 / 1000.00).toFloat(),
            optionalText = ""
        ),
        Category(
            id = 2,
            headerId = 0,
            name = "Gym",
            budgetTarget = Money(value = 29.00),
            availableMoney = Money(value = 13.00),
            progress = (13 / 29.00).toFloat(),
            optionalText = "16.00 € more needed by the 30th"
        ),
        Category(
            id = 3,
            headerId = 0,
            name = "Netflix",
            budgetTarget = Money(value = 9.00),
            availableMoney = Money(value = 2.00),
            progress = (2 / 9.00).toFloat(),
            optionalText = "7.00 € mored needed by the 10th"
        )
    ),
    Header(
        id = 4,
        name = "Daily Life",
        sumOfAvailableMoney = Money(value = 210.0),
        isExpanded = true
    ) to listOf(
        Category(
            id = 5,
            headerId = 4,
            name = "Groceries",
            budgetTarget = Money(value = 100.00),
            availableMoney = Money(value = 27.00),
            progress = (27.00 / 100.00).toFloat(),
            optionalText = "73.00 € more needed by the 30th"
        ),
        Category(
            id = 6,
            headerId = 4,
            name = "Transportation",
            budgetTarget = Money(value = 50.00),
            availableMoney = Money(value = 0.00),
            progress = (0 / 50.00).toFloat(),
            optionalText = "50.00 € more needed by the 30th"
        ),
        Category(
            id = 7,
            headerId = 4,
            name = "Eating Out",
            budgetTarget = Money(value = 60.00),
            availableMoney = Money(value = 60.00),
            progress = (60 / 60.00).toFloat(),
            optionalText = ""
        )
    ),
    Header(
        id = 8,
        name = "Daily Life",
        sumOfAvailableMoney = Money(value = 0.0),
        isExpanded = true
    ) to emptyList(),
    Header(
        id = 9,
        name = "Goals",
        sumOfAvailableMoney = Money(value = 0.0),
        isExpanded = false
    ) to emptyList()
)