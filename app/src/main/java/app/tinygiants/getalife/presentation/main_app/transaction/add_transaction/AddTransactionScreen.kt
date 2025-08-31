package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.AddAccountDialog
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.AddCategoryDialog
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.DebugModeToggle
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.waveAnimationBackground
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.GuidedTransactionFlow
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.GuidedTransactionProgress
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.standard.StandardTransactionForm
import app.tinygiants.getalife.theme.onSuccess
import app.tinygiants.getalife.theme.spacing

@Composable
fun AddTransactionScreen() {
    val viewModel: AddTransactionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showAddCategoryDialog by rememberSaveable { mutableStateOf(false) }

    // Wave background color based on transaction direction
    val neutralBackground = MaterialTheme.colorScheme.primary.toArgb()
    val inflowBackground = onSuccess.toArgb()
    val outflowBackground = MaterialTheme.colorScheme.errorContainer.toArgb()
    val transferBackground = MaterialTheme.colorScheme.primary.toArgb()

    val waveColor = when (uiState.transactionInput.direction) {
        app.tinygiants.getalife.domain.model.TransactionDirection.Inflow -> inflowBackground
        app.tinygiants.getalife.domain.model.TransactionDirection.Outflow -> outflowBackground
        app.tinygiants.getalife.domain.model.TransactionDirection.AccountTransfer -> transferBackground
        else -> neutralBackground
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            // Animated Wave Background
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(450.dp)
                    .waveAnimationBackground(color = waveColor)
            )

            // Main content container
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.m)
                    .padding(WindowInsets.statusBars.asPaddingValues())
            ) {
                // Progress indicator for guided mode
                if (uiState.isGuidedMode) {
                    GuidedTransactionProgress(
                        currentStep = uiState.currentStep,
                        transactionInput = uiState.transactionInput,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.s)
                    )
                }

                // Main content area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(
                            top = if (uiState.isGuidedMode) spacing.l else spacing.m,
                            bottom = spacing.m
                        ),
                    contentAlignment = if (uiState.isGuidedMode) Alignment.Center else Alignment.TopCenter
                ) {
                    if (uiState.isGuidedMode) {
                        GuidedTransactionFlow(
                            currentStep = uiState.currentStep,
                            transactionInput = uiState.transactionInput,
                            categories = uiState.categories,
                            accounts = uiState.accounts,
                            viewModel = viewModel,
                            onShowAddAccountDialog = { showAddAccountDialog = true },
                            onShowAddCategoryDialog = { showAddCategoryDialog = true },
                            currentStepTitle = uiState.currentStepTitle
                        )
                    } else {
                        StandardTransactionForm(
                            transactionInput = uiState.transactionInput,
                            categories = uiState.categories,
                            accounts = uiState.accounts,
                            viewModel = viewModel,
                            onShowAddAccountDialog = { showAddAccountDialog = true },
                            onShowAddCategoryDialog = { showAddCategoryDialog = true },
                            currentStepTitle = uiState.currentStepTitle
                        )
                    }
                }

                // Debug Mode Toggle
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