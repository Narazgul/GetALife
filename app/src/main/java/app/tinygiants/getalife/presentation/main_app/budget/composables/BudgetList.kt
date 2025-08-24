package app.tinygiants.getalife.presentation.main_app.budget.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.CategoryBehaviorType
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent
import app.tinygiants.getalife.presentation.main_app.budget.composables.category.Category
import app.tinygiants.getalife.presentation.main_app.budget.composables.category.EmptyCategoryItem
import app.tinygiants.getalife.presentation.main_app.budget.composables.group.Group
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage
import app.tinygiants.getalife.presentation.shared_composables.isScrollingDown
import app.tinygiants.getalife.theme.spacing

const val ANIMATION_TIME_1_SECOND = 1000
const val ANIMATION_TIME_300_MILLISECONDS = 300

@Composable
fun BudgetList(
    groups: Map<Group, List<CategoryMonthlyStatus>>,
    creditCardAccountBalances: Map<Long, Money>,
    isLoading: Boolean,
    errorMessage: ErrorMessage?,
    onUserScrolling: (Boolean) -> Unit,
    onUserClickEvent: (UserClickEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isLoading && errorMessage == null,
        enter = slideInVertically(),
        exit = slideOutVertically(),
        modifier = modifier
    ) {
        val listState = rememberLazyListState()

        // Separate credit card groups from regular groups
        val creditCardsGroupName = stringResource(R.string.credit_cards)
        val (creditCardGroups, regularGroups) = groups.toList().partition { (group, _) ->
            group.name == creditCardsGroupName || group.name == "Kreditkarten" ||
                    group.name == "Credit Card Payments" || group.name == "บัตรเครดิต"
        }

        // Extract all credit card categories
        val creditCardCategories = creditCardGroups.flatMap { (_, categories) ->
            categories.filter { it.category.behaviorType == CategoryBehaviorType.CreditCardPayment }
        }

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(spacing.xs)
        ) {
            // Show credit card section if there are credit card categories
            if (creditCardCategories.isNotEmpty()) {
                item(key = "credit_cards_section") {
                    CreditCardSection(
                        creditCardCategories = creditCardCategories,
                        creditCardAccountBalances = creditCardAccountBalances,
                        onUserClickEvent = onUserClickEvent
                    )
                }

                // Add extra spacing between credit cards and regular groups
                item(key = "credit_cards_spacer") {
                    Spacer(modifier = Modifier.height(spacing.m))
                }
            }

            // Show regular groups
            regularGroups.forEach { (group, items) ->
                stickyGroups(
                    group = group,
                    onUserClickEvent = onUserClickEvent
                )

                categories(
                    group = group,
                    categories = items,
                    onUserClickEvent = onUserClickEvent
                )
            }
        }

        onUserScrolling(listState.isScrollingDown())
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.stickyGroups(
    group: Group,
    onUserClickEvent: (UserClickEvent) -> Unit
) {
    val onGroupClicked =
        { onUserClickEvent(UserClickEvent.UpdateGroup(group = group.copy(isExpanded = !group.isExpanded))) }

    val onUpdateGroupNameClicked =
        { updatedCategoryName: String -> onUserClickEvent(UserClickEvent.UpdateGroup(group = group.copy(name = updatedCategoryName))) }

    val onDeleteGroupClicked = { onUserClickEvent(UserClickEvent.DeleteGroup(group = group)) }

    val onAddCategoryClicked =
        { categoryName: String ->
            onUserClickEvent(
                UserClickEvent.AddCategory(
                    groupId = group.id,
                    categoryName = categoryName
                )
            )
        }

    stickyHeader(key = group.id) {
        Group(
            groupName = group.name,
            sumOfAvailableMoney = group.sumOfAvailableMoney,
            isExpanded = group.isExpanded,
            onGroupClicked = onGroupClicked,
            onUpdateGroupNameClicked = onUpdateGroupNameClicked,
            onDeleteGroupClicked = onDeleteGroupClicked,
            onAddCategoryClicked = onAddCategoryClicked
        )
        Spacer(modifier = Modifier.height(spacing.xxs))
    }
}

private fun LazyListScope.categories(
    group: Group,
    categories: List<CategoryMonthlyStatus>,
    onUserClickEvent: (UserClickEvent) -> Unit
) {

    val firstCategoryItem = categories.firstOrNull()
    val lastCategoryItem = categories.lastOrNull()

    items(
        items = categories,
        key = { category -> category.category.id }
    ) { monthlyStatus ->
        val onUpdateEmojiClicked = { emoji: String ->
            onUserClickEvent(UserClickEvent.UpdateCategory(category = monthlyStatus.category.copy(emoji = emoji)))
        }
        val onUpdateName = { categoryName: String ->
            onUserClickEvent(UserClickEvent.UpdateCategory(category = monthlyStatus.category.copy(name = categoryName)))
        }
        val onUpdateBudgetTargetClicked = { newBudgetTarget: Money ->
            onUserClickEvent(UserClickEvent.UpdateCategory(category = monthlyStatus.category.copy(budgetTarget = newBudgetTarget)))
        }
        val onUpdateAssignedMoneyClicked = { newAssignedMoney: Money ->
            onUserClickEvent(
                UserClickEvent.UpdateCategoryAssignment(
                    categoryId = monthlyStatus.category.id,
                    newAmount = newAssignedMoney
                )
            )
        }
        val onDeleteCategoryClicked = { onUserClickEvent(UserClickEvent.DeleteCategory(category = monthlyStatus.category)) }

        AnimatedVisibility(
            visible = group.isExpanded,
            enter = fadeIn(animationSpec = tween(ANIMATION_TIME_1_SECOND)) +
                    expandVertically(animationSpec = tween(ANIMATION_TIME_300_MILLISECONDS)),
            exit = shrinkVertically(animationSpec = tween(ANIMATION_TIME_300_MILLISECONDS)),
            modifier = Modifier.padding(
                top = if (monthlyStatus == firstCategoryItem) spacing.xxs else spacing.halfDp,
                bottom = if (monthlyStatus == lastCategoryItem) spacing.xxs else spacing.halfDp
            )
        ) {
            if (monthlyStatus.category.isInitialCategory) {
                EmptyCategoryItem(onUpdateNameClicked = onUpdateName)
            } else
                Column {
                    Category(
                        emoji = monthlyStatus.category.emoji,
                        categoryName = monthlyStatus.category.name,
                        budgetTarget = monthlyStatus.category.budgetTarget,
                        assignedMoney = monthlyStatus.assignedAmount,
                        availableMoney = monthlyStatus.availableAmount,
                        progress = monthlyStatus.progress,
                        monthlyTarget = monthlyStatus.category.monthlyTargetAmount,
                        targetType = monthlyStatus.category.targetType,
                        targetAmount = monthlyStatus.category.targetAmount,
                        targetDate = monthlyStatus.category.targetDate,
                        targetContribution = monthlyStatus.targetContribution,
                        onUpdateEmojiClicked = onUpdateEmojiClicked,
                        onUpdateCategoryClicked = onUpdateName,
                        onUpdateBudgetTargetClicked = onUpdateBudgetTargetClicked,
                        onUpdateAssignedMoneyClicked = onUpdateAssignedMoneyClicked,
                        onMonthlyTargetChanged = { amount ->
                            val updatedCategory = monthlyStatus.category.copy(monthlyTargetAmount = amount)
                            onUserClickEvent(UserClickEvent.UpdateCategory(updatedCategory))
                        },
                        onTargetTypeChanged = { type ->
                            val updatedCategory = monthlyStatus.category.copy(targetType = type)
                            onUserClickEvent(UserClickEvent.UpdateCategory(updatedCategory))
                        },
                        onTargetAmountChanged = { amount ->
                            val updatedCategory = monthlyStatus.category.copy(targetAmount = amount)
                            onUserClickEvent(UserClickEvent.UpdateCategory(updatedCategory))
                        },
                        onTargetDateChanged = { date ->
                            val updatedCategory = monthlyStatus.category.copy(targetDate = date)
                            onUserClickEvent(UserClickEvent.UpdateCategory(updatedCategory))
                        },
                        onDeleteCategoryClicked = onDeleteCategoryClicked
                    )
                    if (monthlyStatus != lastCategoryItem)
                        Spacer(
                            modifier = Modifier
                                .height(spacing.xxs)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
                        )
                }
        }
    }
}

// Temporarily disabled previews due to data structure change
// @ComponentPreview
// @Composable
// fun BudgetListPreview() {
//     GetALifeTheme {
//         Surface {
//             BudgetList(
//                 groups = fakeCategories(),
//                 isLoading = false,
//                 errorMessage = null,
//                 onUserScrolling = { },
//                 onUserClickEvent = { }
//             )
//         }
//     }
// }

// @ComponentPreview
// @Composable
// fun BudgetListEmptyGroupsPreview() {
//     GetALifeTheme {
//         Surface {
//             BudgetList(
//                 groups = emptyMap(),
//                 isLoading = false,
//                 errorMessage = null,
//                 onUserScrolling = { },
//                 onUserClickEvent = { }
//             )
//         }
//     }
// }