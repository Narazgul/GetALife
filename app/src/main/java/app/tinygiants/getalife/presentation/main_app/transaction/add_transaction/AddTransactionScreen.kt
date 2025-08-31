package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.AddAccountDialog
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.AddCategoryDialog
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.DebugModeToggle
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.GuidedTransactionFlow
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared.TransactionStepContainer
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.standard.StandardTransactionForm

@Composable
fun AddTransactionScreen() {
    val viewModel: AddTransactionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showAddCategoryDialog by rememberSaveable { mutableStateOf(false) }

    TransactionStepContainer(
        transactionInput = uiState.transactionInput,
        currentStep = uiState.currentStep,
        isGuidedMode = uiState.isGuidedMode,
        showProgress = uiState.isGuidedMode
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isGuidedMode) {
                    GuidedTransactionFlow(
                        currentStep = uiState.currentStep,
                        transactionInput = uiState.transactionInput,
                        categories = uiState.categories,
                        accounts = uiState.accounts,
                        viewModel = viewModel,
                        onShowAddAccountDialog = { showAddAccountDialog = true },
                        onShowAddCategoryDialog = { showAddCategoryDialog = true }
                    )
                } else {
                    StandardTransactionForm(
                        transactionInput = uiState.transactionInput,
                        categories = uiState.categories,
                        accounts = uiState.accounts,
                        viewModel = viewModel,
                        onShowAddAccountDialog = { showAddAccountDialog = true },
                        onShowAddCategoryDialog = { showAddCategoryDialog = true }
                    )
                }
            }

            DebugModeToggle(
                isGuidedMode = uiState.isGuidedMode,
                onToggleMode = {
                    if (uiState.isGuidedMode) {
                        viewModel.switchToStandardMode()
                    } else {
                        viewModel.switchToGuidedMode()
                    }
                }
            )
        }
    }

    // Dialogs
    if (showAddAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onAccountCreated = { name, balance, type ->
                viewModel.onAccountCreated(name, balance, type)
                showAddAccountDialog = false
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onCategoryCreated = { name ->
                viewModel.onCategoryCreated(name)
                showAddCategoryDialog = false
            }
        )
    }
}