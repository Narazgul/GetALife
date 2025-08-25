package app.tinygiants.getalife.presentation.main_app.bulk_categorization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.domain.model.categorization.TransactionGroup
import app.tinygiants.getalife.presentation.main_app.bulk_categorization.composables.BulkCategorizationDialog
import app.tinygiants.getalife.presentation.main_app.bulk_categorization.composables.TransactionGroupCard
import app.tinygiants.getalife.presentation.shared_composables.LoadingIndicator

/**
 * Bulk Categorization Screen for mass categorization of similar transactions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkCategorizationScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BulkCategorizationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bulk Kategorisierung",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Success/Error Messages
            uiState.successMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { viewModel.refresh() }
                        ) {
                            Text("Wiederholen")
                        }
                    }
                }
            }

            // Main Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                }

                !uiState.hasData -> {
                    EmptyState()
                }

                else -> {
                    TransactionGroupsList(
                        groups = uiState.transactionGroups,
                        isProcessing = uiState.isProcessing,
                        onGroupClicked = viewModel::showCategorizationOptions
                    )
                }
            }
        }

        // Categorization Dialog
        if (uiState.showCategorizationDialog && uiState.selectedGroup != null) {
            BulkCategorizationDialog(
                group = uiState.selectedGroup!!,
                availableCategories = uiState.availableCategories,
                availableGroups = uiState.availableGroups,
                onDismiss = viewModel::hideCategorizationDialog,
                onCategorizeWithExisting = { categoryId ->
                    viewModel.categorizeGroupWithExistingCategory(
                        uiState.selectedGroup!!,
                        categoryId
                    )
                    viewModel.hideCategorizationDialog()
                },
                onCategorizeWithNew = { categoryName, groupId ->
                    viewModel.categorizeGroupWithNewCategory(
                        uiState.selectedGroup!!,
                        categoryName,
                        groupId
                    )
                    viewModel.hideCategorizationDialog()
                }
            )
        }
    }
}

/**
 * List of transaction groups
 */
@Composable
private fun TransactionGroupsList(
    groups: List<TransactionGroup>,
    isProcessing: Boolean,
    onGroupClicked: (TransactionGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🧠 Smart Gruppierung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${groups.size} ähnliche Transaktionsgruppen gefunden. Klicken Sie eine Gruppe an, um alle Transaktionen gleichzeitig zu kategorisieren.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Transaction Groups
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = groups,
                key = { it.id }
            ) { group ->
                TransactionGroupCard(
                    group = group,
                    isProcessing = isProcessing,
                    onClick = { onGroupClicked(group) }
                )
            }
        }
    }
}

/**
 * Empty state when no transaction groups are found
 */
@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Alles kategorisiert!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Es wurden keine ähnlichen unkategorisierten Transaktionen gefunden. Alle Ihre Transaktionen sind bereits kategorisiert oder es gibt keine Gruppen mit mindestens 2 ähnlichen Transaktionen.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💡 Tipp",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Fügen Sie neue Transaktionen hinzu oder überprüfen Sie die Smart-Kategorisierung bei der Transaktionseingabe für automatische Vorschläge.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}