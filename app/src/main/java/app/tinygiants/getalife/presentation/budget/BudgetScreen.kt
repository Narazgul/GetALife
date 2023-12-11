package app.tinygiants.getalife.presentation.budget

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.presentation.budget.composables.BudgetsList
import app.tinygiants.getalife.presentation.budget.composables.ErrorMessage
import app.tinygiants.getalife.presentation.budget.composables.LoadingIndicator
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

@Composable
fun BudgetScreen() {
    val viewModel: BudgetViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BudgetScreen(uiState)
}

@Composable
fun BudgetScreen(uiState: BudgetUiState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.default)
    ) {
        BudgetsList(
            categories = uiState.categories,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage
        )
        LoadingIndicator(
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage
        )
        ErrorMessage(uiState.errorMessage)
    }
}

@Preview(name = "Light", widthDp = 400)
@Preview(name = "Dark", widthDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BudgetScreenPreview() {
    GetALifeTheme {
        Surface {
            BudgetScreen(
                BudgetUiState(categories = exampleMap())
            )
        }
    }
}

@Preview(name = "Light", widthDp = 400)
@Preview(name = "Dark", widthDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BudgetScreenLoadingPreview() {
    GetALifeTheme {
        Surface {
            BudgetScreen(
                BudgetUiState(
                    categories = emptyMap(),
                    isLoading = true
                )
            )
        }
    }
}

@Preview(name = "Light", widthDp = 400)
@Preview(name = "Dark", widthDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BudgetScreenErrorPreview() {
    GetALifeTheme {
        Surface {
            BudgetScreen(
                BudgetUiState(
                    categories = emptyMap(),
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

fun exampleMap() = mapOf(
    Header(id = 0, name = "Fixed Costs", sumOfAvailableMoney = Money(1015.00), isExpanded = true) {}
            to listOf(
        Category(
            id = 11,
            name = "Rent",
            budgetTarget = Money(1000.00),
            availableMoney = Money(1000.00),
            progress = (1000.00 / 1000.00).toFloat(),
            optionalText = ""
        ),
        Category(
            id = 12,
            name = "Gym",
            budgetTarget = Money(29.00),
            availableMoney = Money(13.00),
            progress = (13 / 29.00).toFloat(),
            optionalText = "16.00 € more needed by the 30th"
        ),
        Category(
            id = 13,
            name = "Netflix",
            budgetTarget = Money(9.00),
            availableMoney = Money(2.00),
            progress = (2 / 9.00).toFloat(),
            optionalText = "7.00 € mored needed by the 10th"
        )
    ),
    Header(id = 1, name = "Daily Life", sumOfAvailableMoney = Money(210.0), isExpanded = true) {}
            to listOf(
        Category(
            id = 21,
            name = "Groceries",
            budgetTarget = Money(100.00),
            availableMoney = Money(27.00),
            progress = (27.00 / 100.00).toFloat(),
            optionalText = "73.00 € more needed by the 30th"
        ),
        Category(
            id = 22,
            name = "Transportation",
            budgetTarget = Money(50.00),
            availableMoney = Money(0.00),
            progress = (0 / 50.00).toFloat(),
            optionalText = "50.00 € more needed by the 30th"
        ),
        Category(
            id = 23,
            name = "Eating Out",
            budgetTarget = Money(60.00),
            availableMoney = Money(60.00),
            progress = (60 / 60.00).toFloat(),
            optionalText = ""
        )
    ),
    Header(id = 2, name = "Daily Life", sumOfAvailableMoney = Money(0.0), isExpanded = true) {}
            to emptyList(),
    Header(id = 3, name = "Goals", sumOfAvailableMoney = Money(0.0), isExpanded = false) {}
            to emptyList()
)