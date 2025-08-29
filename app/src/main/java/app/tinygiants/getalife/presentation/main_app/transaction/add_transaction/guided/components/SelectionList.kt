package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.GetALifeTheme
import kotlin.time.Clock

/**
 * Generic reusable selection list component with hoisted state.
 * Can be used for Accounts, Categories, or any selectable items.
 */
@Composable
fun <T> SelectionList(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    onCreateNewClicked: () -> Unit,
    title: String,
    emptyStateMessage: String,
    createNewText: String,
    itemContent: @Composable (T, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        if (items.isEmpty()) {
            // Empty state - prominent create new option
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCreateNewClicked() }
                    .clip(RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(20.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "➕ $createNewText",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = emptyStateMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            // Items available - show list with create option at bottom
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    val isSelected = item == selectedItem
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(item) }
                            .clip(RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            itemContent(item, isSelected)
                        }
                    }
                }

                // Add new item option at the bottom
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCreateNewClicked() }
                            .clip(RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "➕",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = createNewText,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Specialized component for Account selection
 */
@Composable
fun AccountSelectionList(
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit,
    onCreateAccountClicked: () -> Unit,
    title: String = "Von welchem Konto?",
    modifier: Modifier = Modifier
) {
    SelectionList(
        items = accounts,
        selectedItem = selectedAccount,
        onItemSelected = onAccountSelected,
        onCreateNewClicked = onCreateAccountClicked,
        title = title,
        emptyStateMessage = "Es sind noch keine Konten vorhanden",
        createNewText = "Neues Konto erstellen",
        itemContent = { account, isSelected ->
            Text(
                text = account.name,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = modifier
    )
}

/**
 * Specialized component for Category selection
 */
@Composable
fun CategorySelectionList(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    onCreateCategoryClicked: () -> Unit,
    title: String = "Zu welcher Kategorie gehört das?",
    modifier: Modifier = Modifier
) {
    SelectionList(
        items = categories,
        selectedItem = selectedCategory,
        onItemSelected = onCategorySelected,
        onCreateNewClicked = onCreateCategoryClicked,
        title = title,
        emptyStateMessage = "Es sind noch keine Kategorien vorhanden",
        createNewText = "Neue Kategorie erstellen",
        itemContent = { category, isSelected ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = category.emoji,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = category.name,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        modifier = modifier
    )
}

// Preview data
private val previewAccounts = listOf(
    Account(
        id = 1,
        name = "Girokonto",
        balance = Money(1500.0),
        type = AccountType.Checking,
        listPosition = 0,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    ),
    Account(
        id = 2,
        name = "Sparkonto",
        balance = Money(5000.0),
        type = AccountType.Savings,
        listPosition = 1,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )
)

private val previewCategories = listOf(
    Category(
        id = 1,
        groupId = 1,
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
        id = 2,
        groupId = 1,
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

// Previews
@Preview(name = "Account Selection - With Items", showBackground = true)
@Composable
private fun AccountSelectionPreview() {
    GetALifeTheme {
        AccountSelectionList(
            accounts = previewAccounts,
            selectedAccount = previewAccounts[0],
            onAccountSelected = { },
            onCreateAccountClicked = { }
        )
    }
}

@Preview(name = "Category Selection - Empty State", showBackground = true)
@Composable
private fun CategorySelectionEmptyPreview() {
    GetALifeTheme {
        CategorySelectionList(
            categories = emptyList(),
            selectedCategory = null,
            onCategorySelected = { },
            onCreateCategoryClicked = { }
        )
    }
}

@Preview(name = "Category Selection - With Items", showBackground = true)
@Composable
private fun CategorySelectionPreview() {
    GetALifeTheme {
        CategorySelectionList(
            categories = previewCategories,
            selectedCategory = previewCategories[1],
            onCategorySelected = { },
            onCreateCategoryClicked = { }
        )
    }
}