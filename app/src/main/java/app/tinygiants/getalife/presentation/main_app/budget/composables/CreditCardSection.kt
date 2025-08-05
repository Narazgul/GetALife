package app.tinygiants.getalife.presentation.main_app.budget.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent
import app.tinygiants.getalife.presentation.main_app.budget.composables.category.AssignableMoneyBottomSheet
import app.tinygiants.getalife.presentation.main_app.budget.composables.category.EditCategoryBottomSheet
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.onWarning
import app.tinygiants.getalife.theme.spacing
import kotlin.time.Clock

@Composable
fun CreditCardSection(
    creditCardCategories: List<CategoryMonthlyStatus>,
    creditCardAccountBalances: Map<Long, Money>,
    onUserClickEvent: (UserClickEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (creditCardCategories.isEmpty()) return

    var isExpanded by rememberSaveable { mutableStateOf(true) }

    // Create account balance lookup for credit card categories
    // Map from categoryId to account balance using linkedAccountId
    val accountBalanceLookup = creditCardCategories.associate { monthlyStatus ->
        val linkedAccountId = monthlyStatus.category.linkedAccountId
        val accountBalance = if (linkedAccountId != null) {
            creditCardAccountBalances[linkedAccountId] ?: Money(0.0)
        } else {
            Money(0.0)
        }
        monthlyStatus.category.id to accountBalance
    }

    val totalAvailable = accountBalanceLookup.values.sumOf { it.asDouble() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.m, vertical = spacing.s)
    ) {
        // Clickable Header - styled like normal group headers but with white background
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .background(
                    color = MaterialTheme.colorScheme.surface, // White background
                    shape = RoundedCornerShape(size = spacing.s)
                )
                .padding(horizontal = spacing.l, vertical = spacing.m)
        ) {
            // Expand/Collapse Icon
            androidx.compose.material3.Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.credit_cards),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = spacing.m)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Right side - Available money info (always present to maintain consistent header size)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.available),
                    color = if (!isExpanded) MaterialTheme.colorScheme.onSurface else Color.Transparent
                )

                Text(
                    text = Money(totalAvailable).formattedMoney,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (!isExpanded) {
                        if (totalAvailable < 0) {
                            MaterialTheme.colorScheme.error
                        } else if (totalAvailable > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    } else {
                        Color.Transparent
                    }
                )
            }
        }

        // Credit Cards Grid - Only show when expanded
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(1000)) + expandVertically(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300))
        ) {
            Column {
                Spacer(modifier = Modifier.height(spacing.m))

                // Credit Cards Grid with dynamic width distribution
                val chunkedCategories = creditCardCategories.chunked(4)

                chunkedCategories.forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.s)
                    ) {
                        // Calculate weight based on number of cards in this row
                        val cardsInRow = rowCategories.size
                        val weightPerCard = when (cardsInRow) {
                            1 -> 0.5f  // 1 card takes half width
                            2 -> 0.5f  // 2 cards take half width each  
                            3 -> 1f / 3f // 3 cards take one third each
                            4 -> 0.25f // 4 cards take quarter width each
                            else -> 0.25f
                        }

                        // Add leading spacer for centering when less than 4 cards
                        if (cardsInRow < 4) {
                            val leadingWeight = when (cardsInRow) {
                                1 -> 0.25f  // Center 1 card
                                2 -> 0f     // No centering needed for 2 cards
                                3 -> 0f     // No centering needed for 3 cards
                                else -> 0f
                            }
                            if (leadingWeight > 0) {
                                Spacer(modifier = Modifier.weight(leadingWeight))
                            }
                        }

                        rowCategories.forEach { monthlyStatus ->
                            CreditCardItem(
                                monthlyStatus = monthlyStatus,
                                accountBalance = accountBalanceLookup[monthlyStatus.category.id] ?: Money(0.0),
                                onUserClickEvent = onUserClickEvent,
                                modifier = Modifier.weight(weightPerCard)
                            )
                        }

                        // Add trailing spacer for centering when less than 4 cards
                        if (cardsInRow < 4) {
                            val trailingWeight = when (cardsInRow) {
                                1 -> 0.25f  // Center 1 card
                                2 -> 0f     // No centering needed for 2 cards
                                3 -> 0f     // No centering needed for 3 cards
                                else -> 0f
                            }
                            if (trailingWeight > 0) {
                                Spacer(modifier = Modifier.weight(trailingWeight))
                            }
                        }
                    }

                    // Add spacing between rows
                    if (rowCategories != chunkedCategories.last()) {
                        Spacer(modifier = Modifier.height(spacing.s))
                    }
                }
            }
        }
    }
}

/**
 * Temporary function to create mock account balances based on linkedAccountId.
 * In a real implementation, this would be replaced with actual account data from the repository.
 */
//private fun createMockAccountBalanceLookup(creditCardCategories: List<CategoryMonthlyStatus>): Map<Long, Money> {
//    return creditCardCategories.associate { monthlyStatus ->
//        val linkedAccountId = monthlyStatus.category.linkedAccountId
//        val mockBalance = when {
//            linkedAccountId == null -> Money(0.0)
//            linkedAccountId % 3 == 0L -> Money(250.00) // Positive balance (credit)
//            linkedAccountId % 3 == 1L -> Money(-125.50) // Negative balance (debt)  
//            else -> Money(0.0) // Zero balance
//        }
//        monthlyStatus.category.id to mockBalance
//    }
//}

@Composable
private fun CreditCardItem(
    monthlyStatus: CategoryMonthlyStatus,
    accountBalance: Money,
    onUserClickEvent: (UserClickEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAssignMoneyBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showGeneralEditBottomSheet by rememberSaveable { mutableStateOf(false) }

    // Gradient colors for credit card look
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
    )

    Card(
        modifier = modifier
            .aspectRatio(1.8f) // Wider aspect ratio to reduce height
            .combinedClickable(
                onClick = { showAssignMoneyBottomSheet = true },
                onLongClick = { showGeneralEditBottomSheet = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    brush = cardGradient,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(spacing.s)
        ) {
            // Compact Credit Card Layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Card Name at top left
                Text(
                    text = monthlyStatus.category.name.replace(" Payment", ""),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )

                // Empty space in the middle - handled by Arrangement.SpaceBetween

                // Bottom left: Available Amount
                Column {
                    // Dynamic label based on account balance
                    Text(
                        text = if (accountBalance.asDouble() < 0) {
                            "Benötigt"
                        } else {
                            "Available"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 9.sp
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    // Amount with background color for negative values
                    val amountText = if (accountBalance.asDouble() < 0) {
                        Money(kotlin.math.abs(accountBalance.asDouble())).formattedMoney
                    } else {
                        accountBalance.formattedMoney
                    }

                    if (accountBalance.asDouble() < 0) {
                        // Yellow background for negative amounts (debt) - using theme color
                        Box(
                            modifier = Modifier
                                .background(
                                    color = onWarning,
                                    shape = RoundedCornerShape(3.dp)
                                )
                                .padding(horizontal = 3.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = amountText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 10.sp
                            )
                        }
                    } else {
                        // Green text for positive, gray for zero
                        Text(
                            text = amountText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (accountBalance.asDouble() > 0) {
                                Color.Green.copy(alpha = 0.9f)
                            } else {
                                Color.White.copy(alpha = 0.7f) // Gray for zero
                            }
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheets
    if (showGeneralEditBottomSheet) {
        EditCategoryBottomSheet(
            categoryName = monthlyStatus.category.name,
            budgetTarget = monthlyStatus.category.budgetTarget,
            onUpdateCategoryName = { name ->
                onUserClickEvent(UserClickEvent.UpdateCategory(category = monthlyStatus.category.copy(name = name)))
            },
            onBudgetTargetChanged = { target ->
                onUserClickEvent(UserClickEvent.UpdateCategory(category = monthlyStatus.category.copy(budgetTarget = target)))
            },
            onDeleteCategoryClicked = {
                onUserClickEvent(UserClickEvent.DeleteCategory(category = monthlyStatus.category))
            },
            onDismissRequest = { showGeneralEditBottomSheet = false }
        )
    }

    if (showAssignMoneyBottomSheet) {
        AssignableMoneyBottomSheet(
            assignedMoney = monthlyStatus.assignedAmount,
            onAssignedMoneyChanged = { money ->
                onUserClickEvent(
                    UserClickEvent.UpdateCategoryAssignment(
                        categoryId = monthlyStatus.category.id,
                        newAmount = money
                    )
                )
            },
            onDismissRequest = { showAssignMoneyBottomSheet = false }
        )
    }
}

@Preview
@Composable
private fun CreditCardSectionPreview() {
    GetALifeTheme {
        Surface {
            CreditCardSection(
                creditCardCategories = listOf(
                    CategoryMonthlyStatus(
                        category = Category(
                            id = 1,
                            groupId = 1,
                            emoji = "💳",
                            name = "Visa Card Payment",
                            budgetTarget = EmptyMoney(),
                            monthlyTargetAmount = null,
                            targetMonthsRemaining = null,
                            listPosition = 0,
                            isInitialCategory = false,
                            linkedAccountId = 1,
                            updatedAt = Clock.System.now(),
                            createdAt = Clock.System.now()
                        ),
                        assignedAmount = Money(200.0),
                        isCarryOverEnabled = true,
                        spentAmount = Money(150.0),
                        availableAmount = Money(50.0),
                        progress = EmptyProgress(),
                        suggestedAmount = null
                    ),
                    CategoryMonthlyStatus(
                        category = Category(
                            id = 2,
                            groupId = 1,
                            emoji = "💳",
                            name = "Mastercard Payment",
                            budgetTarget = EmptyMoney(),
                            monthlyTargetAmount = null,
                            targetMonthsRemaining = null,
                            listPosition = 1,
                            isInitialCategory = false,
                            linkedAccountId = 2,
                            updatedAt = Clock.System.now(),
                            createdAt = Clock.System.now()
                        ),
                        assignedAmount = Money(100.0),
                        isCarryOverEnabled = true,
                        spentAmount = Money(75.0),
                        availableAmount = Money(-25.0),
                        progress = EmptyProgress(),
                        suggestedAmount = null
                    ),
                    CategoryMonthlyStatus(
                        category = Category(
                            id = 3,
                            groupId = 1,
                            emoji = "💳",
                            name = "American Express Payment",
                            budgetTarget = EmptyMoney(),
                            monthlyTargetAmount = null,
                            targetMonthsRemaining = null,
                            listPosition = 2,
                            isInitialCategory = false,
                            linkedAccountId = 3,
                            updatedAt = Clock.System.now(),
                            createdAt = Clock.System.now()
                        ),
                        assignedAmount = Money(300.0),
                        isCarryOverEnabled = true,
                        spentAmount = Money(250.0),
                        availableAmount = Money(150.0),
                        progress = EmptyProgress(),
                        suggestedAmount = null
                    )
                ),
                creditCardAccountBalances = mapOf(
                    1L to Money(250.0),
                    2L to Money(-125.50),
                    3L to Money(0.0)
                ),
                onUserClickEvent = { }
            )
        }
    }
}