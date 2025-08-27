package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.standard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransactionUiState
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransactionViewModel
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.SmartCategorizationUiState
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.waveAnimationBackground
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.onSuccess
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * Standard transaction input screen using AddTransactionItem.
 *
 * Used for experienced users who don't need step-by-step guidance.
 * Provides full-featured transaction form with smart categorization and animated background.
 * Performance optimized - receives ViewModel as parameter to avoid multiple injections.
 */
@Composable
fun StandardTransactionScreen(
    viewModel: AddTransactionViewModel,
    uiState: AddTransactionUiState
) {
    // Collect additional states needed for standard mode
    val partnerSuggestions by viewModel.partners.collectAsStateWithLifecycle()
    val smartCategorizationState by viewModel.smartCategorizationState.collectAsStateWithLifecycle()
    val transactionPartner by viewModel.transactionPartner.collectAsStateWithLifecycle()

    // UI state management
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Background color management for wave animation
    val neutralBackground = MaterialTheme.colorScheme.primary.toArgb()
    val inflowBackground = onSuccess.toArgb()
    val outflowBackground = MaterialTheme.colorScheme.errorContainer.toArgb()
    val transferBackground = MaterialTheme.colorScheme.tertiary.toArgb()
    var waveColor by remember { mutableIntStateOf(neutralBackground) }

    // Snackbar helper
    val transactionSavedString = stringResource(id = R.string.transaction_saved)
    val showTransactionAddedSnackbar = {
        scope.launch { snackbarHostState.showSnackbar(transactionSavedString) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .padding(innerPadding)
        ) {
            // Animated wave background
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .height(450.dp)
                    .waveAnimationBackground(color = waveColor)
            )

            // Main transaction form
            AddTransactionItem(
                categories = uiState.categories,
                accounts = uiState.accounts,
                partnerSuggestions = partnerSuggestions,
                selectedCategory = uiState.selectedCategory,
                transactionPartner = transactionPartner,
                smartCategorizationUiState = smartCategorizationState,
                onTransactionPartnerChanged = viewModel::updateTransactionPartner,
                onTransactionDirectionClicked = { direction ->
                    // Update wave color based on transaction direction
                    waveColor = when (direction) {
                        TransactionDirection.Inflow -> inflowBackground
                        TransactionDirection.Outflow -> outflowBackground
                        TransactionDirection.Unknown -> transferBackground
                        else -> neutralBackground
                    }
                },
                onAddTransactionClicked = { amount, account, category, direction, description, partner, dateOfTransaction, recurrenceFrequency ->
                    viewModel.onSaveTransactionClicked(
                        amount = amount,
                        direction = direction,
                        accountId = account.id,
                        description = description,
                        transactionPartner = partner,
                        category = category,
                        dateOfTransaction = dateOfTransaction,
                        recurrenceFrequency = recurrenceFrequency
                    )
                    showTransactionAddedSnackbar()
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(spacing.m)
            )

            // Smart categorization bottom sheet
            smartCategorizationState.categorizationResult?.let { result ->
                SmartCategorizationBottomSheet(
                    categorizationResult = result,
                    isVisible = smartCategorizationState.showBottomSheet,
                    onDismiss = viewModel::dismissCategorizationBottomSheet,
                    onCategorySelected = viewModel::onCategorySuggestionAccepted,
                    onNewCategoryCreated = viewModel::onCreateCategoryFromSuggestion
                )
            }
        }
    }
}

// ================================
// Preview Composables
// ================================

private val previewAccounts = listOf(
    Account(
        id = 1L,
        name = "Girokonto",
        type = AccountType.Checking,
        balance = Money(1250.75),
        listPosition = 0,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now()
    ),
    Account(
        id = 2L,
        name = "Sparkonto",
        type = AccountType.Savings,
        balance = Money(5000.0),
        listPosition = 1,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now()
    )
)

private val previewCategories = listOf(
    Category(
        id = 1L,
        groupId = 1L,
        emoji = "🍕",
        name = "Lebensmittel",
        budgetTarget = Money(400.0),
        monthlyTargetAmount = null,
        targetMonthsRemaining = null,
        listPosition = 1,
        isInitialCategory = false,
        linkedAccountId = null,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    ),
    Category(
        id = 2L,
        groupId = 1L,
        emoji = "🏠",
        name = "Miete",
        budgetTarget = Money(800.0),
        monthlyTargetAmount = null,
        targetMonthsRemaining = null,
        listPosition = 2,
        isInitialCategory = false,
        linkedAccountId = null,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )
)

@Preview(name = "Standard Transaction Form", showBackground = true)
@Composable
private fun StandardTransactionScreenPreview() {
    GetALifeTheme {
        AddTransactionItem(
            categories = previewCategories,
            accounts = previewAccounts,
            partnerSuggestions = listOf("Netflix", "Edeka", "Amazon", "Spotify"),
            selectedCategory = previewCategories[0],
            transactionPartner = "",
            smartCategorizationUiState = SmartCategorizationUiState(),
            onTransactionPartnerChanged = { },
            onTransactionDirectionClicked = { },
            onAddTransactionClicked = { _, _, _, _, _, _, _, _ -> }
        )
    }
}