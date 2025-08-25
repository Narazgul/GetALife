package app.tinygiants.getalife.presentation.main_app.transaction.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.categorization.CategoryMatch
import app.tinygiants.getalife.domain.model.categorization.CategorizationResult
import app.tinygiants.getalife.domain.model.categorization.NewCategorySuggestion

/**
 * Bottom sheet for displaying smart categorization suggestions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCategorizationBottomSheet(
    categorizationResult: CategorizationResult,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onCategorySelected: (Long) -> Unit,
    onNewCategoryCreated: (NewCategorySuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible && categorizationResult.hasAnyMatch) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                SmartCategorizationHeader(onDismiss = onDismiss)

                Spacer(modifier = Modifier.height(16.dp))

                // Existing category match
                categorizationResult.existingCategoryMatch?.let { match ->
                    CategoryMatchCard(
                        match = match,
                        onAccept = { onCategorySelected(match.categoryId) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // New category suggestion
                categorizationResult.newCategorySuggestion?.let { suggestion ->
                    NewCategoryCreationCard(
                        suggestion = suggestion,
                        onCreate = { onNewCategoryCreated(suggestion) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Skip option
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Überspringen")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SmartCategorizationHeader(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Smart Kategorisierung",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Schließen"
            )
        }
    }
}

/**
 * Card displaying an existing category match suggestion
 */
@Composable
fun CategoryMatchCard(
    match: CategoryMatch,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (match.isConfident)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = match.categoryEmoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = match.categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        ConfidenceIndicator(confidence = match.confidence)
                    }
                }
            }

            if (match.reasoning.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = match.reasoning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FilledTonalButton(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (match.isConfident) "Kategorie übernehmen" else "Trotzdem verwenden"
                )
            }
        }
    }
}

/**
 * Card for suggesting a new category creation
 */
@Composable
fun NewCategoryCreationCard(
    suggestion: NewCategorySuggestion,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                ) {
                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = suggestion.emoji,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Neue Kategorie vorgeschlagen",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "${suggestion.categoryName} (${suggestion.groupName})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (suggestion.reasoning.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = suggestion.reasoning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FilledTonalButton(
                onClick = onCreate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kategorie erstellen")
            }
        }
    }
}

/**
 * Visual indicator showing confidence level
 */
@Composable
fun ConfidenceIndicator(
    confidence: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vertrauen: ${(confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { confidence },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = when {
                confidence >= 0.8f -> MaterialTheme.colorScheme.primary
                confidence >= 0.5f -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.error
            }
        )
    }
}