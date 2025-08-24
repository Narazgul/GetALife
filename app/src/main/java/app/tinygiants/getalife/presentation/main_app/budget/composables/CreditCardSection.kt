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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import app.tinygiants.getalife.theme.spacing
import kotlin.time.Clock

sealed class CoverageStatus {
    data class Underfunded(val shortfall: Money) : CoverageStatus()
    object ExactlyCovered : CoverageStatus()
    data class Overfunded(val surplus: Money) : CoverageStatus()
    data class NoDebt(val available: Money) : CoverageStatus()
}

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

    // Calculate total effective available amount (considering debt coverage)
    val totalEffectiveAvailable = creditCardCategories.sumOf { monthlyStatus ->
        val linkedAccountId = monthlyStatus.category.linkedAccountId
        val accountBalance = if (linkedAccountId != null) {
            creditCardAccountBalances[linkedAccountId] ?: Money(0.0)
        } else {
            Money(0.0)
        }

        // Use the same logic as in CreditCardItem for consistency
        val manuallyAssignedAmount = monthlyStatus.assignedAmount
        val creditCardDebt = if (accountBalance.asDouble() < 0) {
            Money(kotlin.math.abs(accountBalance.asDouble()))
        } else {
            Money(0.0)
        }

        // Calculate effective available amount (assigned - debt)
        val effectiveAvailable = when {
            creditCardDebt.asDouble() == 0.0 -> manuallyAssignedAmount.asDouble()
            manuallyAssignedAmount.asDouble() < creditCardDebt.asDouble() -> {
                // Negative value indicates shortfall
                -(creditCardDebt.asDouble() - manuallyAssignedAmount.asDouble())
            }

            else -> {
                // Positive value indicates surplus 
                manuallyAssignedAmount.asDouble() - creditCardDebt.asDouble()
            }
        }

        effectiveAvailable
    }

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
                    text = Money(totalEffectiveAvailable).formattedMoney,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (!isExpanded) {
                        if (totalEffectiveAvailable < 0) {
                            MaterialTheme.colorScheme.error
                        } else if (totalEffectiveAvailable > 0) {
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

@Composable
private fun CreditCardItem(
    monthlyStatus: CategoryMonthlyStatus,
    accountBalance: Money,
    onUserClickEvent: (UserClickEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAssignMoneyBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showGeneralEditBottomSheet by rememberSaveable { mutableStateOf(false) }

    // Credit card debt coverage calculation
    // Use assignedAmount (manually assigned money) instead of availableAmount 
    // (which includes invisible inflow from credit card spending)
    val manuallyAssignedAmount = monthlyStatus.assignedAmount
    val creditCardDebt = if (accountBalance.asDouble() < 0) {
        Money(kotlin.math.abs(accountBalance.asDouble())) // Convert negative balance to positive debt amount
    } else {
        Money(0.0) // No debt
    }

    // Calculate coverage status based on manually assigned money only
    val debtCoverage = when {
        creditCardDebt.asDouble() == 0.0 -> {
            // No debt - show assigned amount normally
            if (manuallyAssignedAmount.asDouble() > 0) {
                CoverageStatus.Overfunded(manuallyAssignedAmount)
            } else {
                CoverageStatus.NoDebt(manuallyAssignedAmount)
            }
        }

        manuallyAssignedAmount.asDouble() < creditCardDebt.asDouble() -> {
            // Not enough money manually assigned to cover debt
            val shortfall = creditCardDebt - manuallyAssignedAmount
            CoverageStatus.Underfunded(shortfall)
        }

        manuallyAssignedAmount.asDouble() == creditCardDebt.asDouble() -> {
            // Exactly covered by manual assignment
            CoverageStatus.ExactlyCovered
        }

        else -> {
            // More than enough money manually assigned
            val surplus = manuallyAssignedAmount - creditCardDebt
            CoverageStatus.Overfunded(surplus)
        }
    }

    // Text colors based on coverage status (instead of card gradient)
    val amountColor = when (debtCoverage) {
        is CoverageStatus.Underfunded -> Color(0xFFFFC107) // Yellow/Orange for needs more money
        CoverageStatus.ExactlyCovered -> Color(0xFF2196F3) // Blue for exactly covered
        is CoverageStatus.Overfunded -> Color(0xFF4CAF50) // Green for overfunded
        is CoverageStatus.NoDebt -> if (debtCoverage.available.asDouble() > 0) {
            Color(0xFF4CAF50) // Green for available money
        } else {
            Color.White.copy(alpha = 0.7f) // Gray for no money
        }
    }

    // Neutral gradient with subtle depth for card-like appearance
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surface
        )
    )

    Card(
        modifier = modifier
            .aspectRatio(1.6f) // More credit card-like ratio
            .combinedClickable(
                onClick = { showAssignMoneyBottomSheet = true },
                onLongClick = { showGeneralEditBottomSheet = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp) // More rounded corners
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    brush = cardGradient,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(spacing.m)
        ) {
            // Credit card layout
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section - Card name and type indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = monthlyStatus.category.name.replace(" Payment", ""),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Card type indicator (could be replaced with brand logos later)
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "💳",
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.s))

                // Middle section - Assigned amount with better styling
                Column {
                    Text(
                        text = "ZUGEWIESEN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = manuallyAssignedAmount.formattedMoney,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(spacing.xs))

                // Bottom section - Status with improved hierarchy
                Column {
                    val (label, amount) = when (debtCoverage) {
                        is CoverageStatus.Underfunded -> "BENÖTIGT" to debtCoverage.shortfall.formattedMoney
                        CoverageStatus.ExactlyCovered -> "STATUS" to "Gedeckt"
                        is CoverageStatus.Overfunded -> "VERFÜGBAR" to debtCoverage.surplus.formattedMoney
                        is CoverageStatus.NoDebt -> if (debtCoverage.available.asDouble() > 0) {
                            "VERFÜGBAR" to debtCoverage.available.formattedMoney
                        } else {
                            "STATUS" to "Schuldenfrei"
                        }
                    }

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = amount,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                }
            }

            // Subtle highlight effect on the right edge for visual interest
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(2.dp)
                    .fillMaxHeight(0.6f)
                    .background(
                        amountColor.copy(alpha = 0.3f),
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }

    // Bottom Sheets
    if (showGeneralEditBottomSheet) {
        EditCategoryBottomSheet(
            categoryName = monthlyStatus.category.name,
            budgetTarget = monthlyStatus.category.budgetTarget,
            monthlyTarget = monthlyStatus.category.monthlyTargetAmount,
            targetType = monthlyStatus.category.targetType,
            targetAmount = monthlyStatus.category.targetAmount,
            targetDate = monthlyStatus.category.targetDate,
            onUpdateCategoryName = { name ->
                onUserClickEvent(UserClickEvent.UpdateCategory(category = monthlyStatus.category.copy(name = name)))
            },
            onBudgetTargetChanged = { target ->
                onUserClickEvent(UserClickEvent.UpdateCategory(category = monthlyStatus.category.copy(budgetTarget = target)))
            },
            onMonthlyTargetChanged = { amount ->
                onUserClickEvent(
                    UserClickEvent.UpdateCategory(
                        category = monthlyStatus.category.copy(monthlyTargetAmount = amount)
                    )
                )
            },
            onTargetTypeChanged = { type ->
                onUserClickEvent(
                    UserClickEvent.UpdateCategory(
                        category = monthlyStatus.category.copy(targetType = type)
                    )
                )
            },
            onTargetAmountChanged = { amount ->
                onUserClickEvent(
                    UserClickEvent.UpdateCategory(
                        category = monthlyStatus.category.copy(targetAmount = amount)
                    )
                )
            },
            onTargetDateChanged = { date ->
                onUserClickEvent(
                    UserClickEvent.UpdateCategory(
                        category = monthlyStatus.category.copy(targetDate = date)
                    )
                )
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
                        suggestedAmount = null,
                        targetContribution = null // Credit card payments don't have target contributions
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
                        suggestedAmount = null,
                        targetContribution = null // Credit card payments don't have target contributions
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
                        suggestedAmount = null,
                        targetContribution = null // Credit card payments don't have target contributions
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