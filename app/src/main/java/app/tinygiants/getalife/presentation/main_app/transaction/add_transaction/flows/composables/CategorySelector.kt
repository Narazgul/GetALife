package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.flows.composables

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.GetALifeTheme
import kotlin.time.Clock

/**
 * Specialized category selection component for guided transaction flows.
 * Handles empty state, category creation, and displays category icons with budget info.
 *
 * Performance optimized with stable callbacks to prevent unnecessary recompositions.
 */
@Composable
fun CategorySelector(
    title: String,
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    onCreateCategoryClicked: () -> Unit,
    modifier: Modifier = Modifier,
    showBudgetInfo: Boolean = true
) {
    // Stable callback for create category - prevents recomposition of EmptyCategoryState
    val stableCreateCategoryCallback = remember { onCreateCategoryClicked }

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

        if (categories.isEmpty()) {
            // Empty state - show prominent "create category" option
            EmptyCategoryState(
                onCreateClicked = stableCreateCategoryCallback
            )
        } else {
            // Categories available - show list with "add new" at bottom
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = categories,
                    key = { category -> category.id }
                ) { category ->
                    // Create stable callback per category to prevent unnecessary recompositions
                    val stableCategoryCallback = remember(category.id, onCategorySelected) {
                        { onCategorySelected(category) }
                    }

                    CategoryCard(
                        category = category,
                        isSelected = category == selectedCategory,
                        showBudgetInfo = showBudgetInfo,
                        onClick = stableCategoryCallback
                    )
                }

                item(key = "add_new_category") {
                    AddNewCategoryCard(
                        onClick = stableCreateCategoryCallback
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCategoryState(
    onCreateClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCreateClicked() }
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
                    text = "🏷️ Neue Kategorie erstellen",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Es sind noch keine Kategorien vorhanden",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    isSelected: Boolean,
    showBudgetInfo: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category icon/emoji
                    Text(
                        text = if (category.emoji.isNotEmpty()) category.emoji else "🏷️",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 12.dp)
                    )

                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )

                        if (showBudgetInfo && category.budgetTarget.asDouble() > 0) {
                            Text(
                                text = "Budget: ${category.budgetTarget.formattedMoney}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddNewCategoryCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                    text = "🏷️",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Neue Kategorie erstellen",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Preview data
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
        emoji = "🚗",
        name = "Transport",
        budgetTarget = Money(200.0),
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
        emoji = "🎮",
        name = "Unterhaltung",
        budgetTarget = Money(150.0),
        monthlyTargetAmount = null,
        targetMonthsRemaining = null,
        listPosition = 3,
        isInitialCategory = false,
        linkedAccountId = null,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    ),
    Category(
        id = 4L,
        groupId = 2L,
        emoji = "",
        name = "Sonstiges",
        budgetTarget = Money(0.0),
        monthlyTargetAmount = null,
        targetMonthsRemaining = null,
        listPosition = 4,
        isInitialCategory = false,
        linkedAccountId = null,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )
)

// Previews
@Preview(name = "Category Selector - With Categories", showBackground = true)
@Composable
private fun CategorySelectorPreview() {
    GetALifeTheme {
        CategorySelector(
            title = "Zu welcher Kategorie gehört diese Ausgabe?",
            categories = previewCategories,
            selectedCategory = previewCategories[0],
            onCategorySelected = { },
            onCreateCategoryClicked = { }
        )
    }
}

@Preview(name = "Category Selector - Empty State", showBackground = true)
@Composable
private fun CategorySelectorEmptyPreview() {
    GetALifeTheme {
        CategorySelector(
            title = "Zu welcher Kategorie gehört das?",
            categories = emptyList(),
            selectedCategory = null,
            onCategorySelected = { },
            onCreateCategoryClicked = { }
        )
    }
}

@Preview(name = "Category Selector - No Budget Info", showBackground = true)
@Composable
private fun CategorySelectorNoBudgetPreview() {
    GetALifeTheme {
        CategorySelector(
            title = "Kategorie wählen",
            categories = previewCategories,
            selectedCategory = previewCategories[2],
            onCategorySelected = { },
            onCreateCategoryClicked = { },
            showBudgetInfo = false
        )
    }
}