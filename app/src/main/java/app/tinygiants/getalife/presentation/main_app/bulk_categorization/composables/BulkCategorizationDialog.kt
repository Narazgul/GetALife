package app.tinygiants.getalife.presentation.main_app.bulk_categorization.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.categorization.TransactionGroup

/**
 * Dialog for categorizing a transaction group
 */
@Composable
fun BulkCategorizationDialog(
    group: TransactionGroup,
    availableCategories: List<Category>,
    availableGroups: List<Group>,
    onDismiss: () -> Unit,
    onCategorizeWithExisting: (categoryId: Long) -> Unit,
    onCategorizeWithNew: (categoryName: String, groupId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Bestehende Kategorie", "Neue Kategorie")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gruppe kategorisieren",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Schließen")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Group info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = group.groupName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${group.transactionCount} Transaktionen • ${group.totalAmount.formattedMoney}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab content
                when (selectedTab) {
                    0 -> ExistingCategoryTab(
                        categories = availableCategories,
                        onCategorySelected = onCategorizeWithExisting
                    )

                    1 -> NewCategoryTab(
                        groups = availableGroups,
                        onCreateCategory = onCategorizeWithNew
                    )
                }
            }
        }
    }
}

/**
 * Tab for selecting an existing category
 */
@Composable
private fun ExistingCategoryTab(
    categories: List<Category>,
    onCategorySelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.heightIn(max = 300.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = false,
                        onClick = { onCategorySelected(category.id) }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = category.budgetTarget.formattedMoney,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab for creating a new category
 */
@Composable
private fun NewCategoryTab(
    groups: List<Group>,
    onCreateCategory: (String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableLongStateOf(groups.firstOrNull()?.id ?: 0L) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        // Category name input
        OutlinedTextField(
            value = categoryName,
            onValueChange = { categoryName = it },
            label = { Text("Kategorie-Name") },
            placeholder = { Text("z.B. Online Shopping") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Group selection
        Box {
            OutlinedTextField(
                value = groups.find { it.id == selectedGroupId }?.name ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Gruppe") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.name) },
                        onClick = {
                            selectedGroupId = group.id
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Create button
        Button(
            onClick = {
                if (categoryName.isNotBlank() && selectedGroupId != 0L) {
                    onCreateCategory(categoryName, selectedGroupId)
                }
            },
            enabled = categoryName.isNotBlank() && selectedGroupId != 0L,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Neue Kategorie erstellen")
        }
    }
}