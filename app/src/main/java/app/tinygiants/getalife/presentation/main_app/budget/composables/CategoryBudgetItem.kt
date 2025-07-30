package app.tinygiants.getalife.presentation.main_app.budget.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.shared_composables.UiText
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import kotlin.time.Clock

@Composable
fun CategoryBudgetItem(
    categoryStatus: CategoryMonthlyStatus,
    onAmountChanged: (Money) -> Unit
) {
    var amountText by remember {
        mutableStateOf(categoryStatus.assignedAmount.asDouble().toString())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.m, vertical = spacing.xs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.m),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${categoryStatus.category.emoji} ${categoryStatus.category.name}",
                    style = MaterialTheme.typography.bodyLarge
                )

                if (categoryStatus.spentAmount.asDouble() > 0) {
                    Text(
                        text = "Spent: ${categoryStatus.spentAmount.formattedPositiveMoney}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (categoryStatus.suggestedAmount != null) {
                    Text(
                        text = "Suggested: ${categoryStatus.suggestedAmount.formattedPositiveMoney}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { newValue ->
                    amountText = newValue
                    val amount = newValue.toDoubleOrNull() ?: 0.0
                    onAmountChanged(Money(amount))
                },
                label = { Text("Budget") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.padding(start = spacing.m)
            )
        }
    }
}

@Preview
@Composable
fun CategoryBudgetItemPreview() {
    GetALifeTheme {
        val fakeStatus = CategoryMonthlyStatus(
            category = Category(
                id = 1L,
                groupId = 1L,
                emoji = "🛒",
                name = "Groceries",
                budgetTarget = Money(100.0),
                monthlyTargetAmount = null,
                targetMonthsRemaining = null,
                progress = EmptyProgress(),
                optionalText = UiText.DynamicString(""),
                listPosition = 0,
                isInitialCategory = false,
                updatedAt = Clock.System.now(),
                createdAt = Clock.System.now()
            ),
            assignedAmount = Money(100.0),
            isCarryOverEnabled = true,
            spentAmount = Money(15.0),
            availableAmount = Money(85.0),
            progress = EmptyProgress(),
            suggestedAmount = Money(120.0)
        )
        CategoryBudgetItem(
            categoryStatus = fakeStatus,
            onAmountChanged = {}
        )
    }
}