package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.CompletedStepsChecklist
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.GuidedTransactionScreen
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.TransactionCompletedStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.standard.AddTransactionItem
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.standard.StandardTransactionScreen
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.waveAnimationBackground
import app.tinygiants.getalife.theme.GetALifeTheme
import java.time.LocalDate
import kotlin.time.Clock

/**
 * Main entry point for the Add Transaction feature.
 * 
 * Routes between Guided Mode (for new users) and Standard Mode (for experienced users).
 * Performance optimized with single ViewModel injection that gets passed down to components.
 * 
 * Architecture:
 * - AddTransactionScreen() -> Entry point & ViewModel injection
 * - GuidedTransactionScreen() -> Step-by-step onboarding flow (in GuidedTransactionScreen.kt)
 * - StandardTransactionScreen() -> Full-featured transaction form (in StandardTransactionScreen.kt)
 */
@Composable
fun AddTransactionScreen() {
    // Single ViewModel injection point - gets passed down to avoid multiple instances
    val viewModel: AddTransactionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Route to appropriate UI based on user preference and onboarding status
    if (uiState.isGuidedMode) {
        GuidedTransactionScreen(
            viewModel = viewModel,
            uiState = uiState
        )
    } else {
        StandardTransactionScreen(
            viewModel = viewModel, 
            uiState = uiState
        )
    }
}

// ================================
// Preview Composables
// ================================

/**
 * Preview data for testing both modes
 */
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
    ),
    Account(
        id = 3L,
        name = "Kreditkarte",
        type = AccountType.CreditCard,
        balance = Money(-245.30),
        listPosition = 2,
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
    ),
    Category(
        id = 3L,
        groupId = 2L,
        emoji = "🚗",
        name = "Transport",
        budgetTarget = Money(200.0),
        monthlyTargetAmount = null,
        targetMonthsRemaining = null,
        listPosition = 3,
        isInitialCategory = false,
        linkedAccountId = null,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )
)

@Preview(name = "AddTransactionScreen - Guided Mode (Completion)", showBackground = true)
@Composable
private fun AddTransactionScreenGuidedPreview() {
    GetALifeTheme {
        val mockUiState = AddTransactionUiState(
            categories = previewCategories,
            accounts = previewAccounts,
            isGuidedMode = true,
            guidedStep = GuidedTransactionStep.Done, // Show completion screen
            selectedDirection = TransactionDirection.Outflow,
            selectedAmount = Money(25.50),
            selectedAccount = previewAccounts[0],
            selectedCategory = previewCategories[0],
            selectedPartner = "Edeka",
            selectedDate = LocalDate.now(),
            selectedDescription = "Wocheneinkauf"
        )

        // Simulate the complete GuidedTransactionScreen structure with progress bar
        val progress = 1.0f // 100% complete for Done step

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Animated Wave Background (like in real guided screen)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .height(350.dp)
                    .waveAnimationBackground(color = MaterialTheme.colorScheme.primary.toArgb())
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Progress Header like in real GuidedTransactionScreen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Progress bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Motivational text
                    Text(
                        text = "Stark! Du hast alle Schritte abgeschlossen 🎉",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Completed steps checklist
                CompletedStepsChecklist(
                    uiState = mockUiState,
                    onStepClicked = { }
                )

                // Completion step
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    TransactionCompletedStep(
                        onSwitchToStandardMode = { }
                    )
                }
            }
        }
    }
}

@Preview(name = "AddTransactionScreen - Standard Mode (Full Screen)", showBackground = true)
@Composable
private fun AddTransactionScreenStandardPreview() {
    GetALifeTheme {
        val mockUiState = AddTransactionUiState(
            categories = previewCategories,
            accounts = previewAccounts,
            isGuidedMode = false,
            selectedDirection = TransactionDirection.Outflow,
            selectedAmount = Money(15.99),
            selectedAccount = previewAccounts[0],
            selectedCategory = previewCategories[0],
            selectedPartner = "Netflix",
            selectedDate = LocalDate.now(),
            selectedDescription = "Streaming Abo"
        )

        // Show the complete standard transaction screen with animated wave background
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Animated wave background (like in StandardTransactionScreen)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .height(450.dp)
                    .waveAnimationBackground(color = MaterialTheme.colorScheme.primary.toArgb())
            )

            // Transaction form (centered like in real screen)
            AddTransactionItem(
                categories = previewCategories,
                accounts = previewAccounts,
                partnerSuggestions = listOf("Netflix", "Edeka", "Amazon", "Spotify"),
                selectedCategory = mockUiState.selectedCategory,
                transactionPartner = mockUiState.selectedPartner,
                smartCategorizationUiState = SmartCategorizationUiState(),
                onTransactionPartnerChanged = { },
                onTransactionDirectionClicked = { },
                onAddTransactionClicked = { _, _, _, _, _, _, _, _ -> },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }
    }
}
