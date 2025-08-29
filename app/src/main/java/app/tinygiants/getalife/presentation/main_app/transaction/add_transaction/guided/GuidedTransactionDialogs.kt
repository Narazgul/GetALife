package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransactionUiState
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransactionViewModel
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.GuidedTransactionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.AddAccountDialog
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.AddCategoryDialog
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.InflowAccountStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.OutflowAccountStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.OutflowCategoryStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.TransferFromAccountStep

/**
 * Enhanced step orchestrator with integrated dialog support.
 * Handles dialog state management for account and category creation.
 */
@Composable
fun GuidedTransactionStepWithDialogs(
    step: GuidedTransactionStep,
    uiState: AddTransactionUiState,
    viewModel: AddTransactionViewModel,
    modifier: Modifier = Modifier
) {
    // Dialog state management
    var showAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var amountText by rememberSaveable { mutableStateOf(uiState.selectedAmount?.asDouble()?.toString() ?: "") }

    when (step) {
        // Steps that need account creation dialogs
        GuidedTransactionStep.Account -> {
            when (uiState.selectedDirection) {
                app.tinygiants.getalife.domain.model.TransactionDirection.Inflow -> {
                    InflowAccountStep(
                        accounts = uiState.accounts,
                        selectedAccount = uiState.selectedAccount,
                        onAccountSelected = viewModel::onGuidedAccountSelected,
                        onCreateAccountClicked = { showAccountDialog = true },
                        modifier = modifier
                    )
                }

                app.tinygiants.getalife.domain.model.TransactionDirection.Outflow -> {
                    OutflowAccountStep(
                        accounts = uiState.accounts,
                        selectedAccount = uiState.selectedAccount,
                        onAccountSelected = viewModel::onGuidedAccountSelected,
                        onCreateAccountClicked = { showAccountDialog = true },
                        modifier = modifier
                    )
                }

                app.tinygiants.getalife.domain.model.TransactionDirection.Unknown -> { // Transfer
                    TransferFromAccountStep(
                        accounts = uiState.accounts,
                        selectedAccount = uiState.selectedAccount,
                        onAccountSelected = viewModel::onGuidedAccountSelected,
                        onCreateAccountClicked = { showAccountDialog = true },
                        modifier = modifier
                    )
                }

                else -> {
                    InflowAccountStep(
                        accounts = uiState.accounts,
                        selectedAccount = uiState.selectedAccount,
                        onAccountSelected = viewModel::onGuidedAccountSelected,
                        onCreateAccountClicked = { showAccountDialog = true },
                        modifier = modifier
                    )
                }
            }
        }

        // Step that needs category creation dialog
        GuidedTransactionStep.Category -> {
            OutflowCategoryStep(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::onGuidedCategorySelected,
                onCreateCategoryClicked = { showCategoryDialog = true },
                modifier = modifier
            )
        }

        // All other steps use the regular orchestrator
        else -> {
            app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.GuidedTransactionStep(
                step = step,
                uiState = uiState,
                viewModel = viewModel,
                modifier = modifier
            )
        }
    }

    // Account Creation Dialog
    if (showAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAccountDialog = false },
            onAccountCreated = { name, balance, accountType ->
                // Use the new ViewModel function for account creation
                viewModel.onAccountCreated(name, balance, accountType)
                showAccountDialog = false
            }
        )
    }

    // Category Creation Dialog
    if (showCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showCategoryDialog = false },
            onCategoryCreated = { name ->
                // Use the new ViewModel function for category creation
                viewModel.onCategoryCreated(name)
                showCategoryDialog = false
            }
        )
    }
}