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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent
import app.tinygiants.getalife.presentation.main_app.budget.composables.category.Category
import app.tinygiants.getalife.presentation.main_app.budget.composables.category.EmptyCategoryItem
import app.tinygiants.getalife.presentation.main_app.budget.composables.group.Group
import app.tinygiants.getalife.presentation.main_app.budget.fakeCategories
import app.tinygiants.getalife.presentation.main_app.shared_composables.ErrorMessage
import app.tinygiants.getalife.presentation.main_app.shared_composables.isScrollingDown
import app.tinygiants.getalife.theme.ComponentPreview
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

const val ANIMATION_TIME_1_SECOND = 1000
const val ANIMATION_TIME_300_MILLISECONDS = 300

@Composable
fun BudgetList(
    groups: Map<Group, List<Category>>,
    isLoading: Boolean,
    errorMessage: ErrorMessage?,
    onUserClickEvent: (UserClickEvent) -> Unit,
    onUserScrolling: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isLoading && errorMessage == null,
        enter = slideInVertically(),
        exit = slideOutVertically(),
        modifier = modifier
    ) {
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(spacing.xs)
        ) {
            groups.forEach { (group, items) ->

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
    categories: List<Category>,
    onUserClickEvent: (UserClickEvent) -> Unit
) {

    val firstCategoryItem = categories.firstOrNull()
    val lastCategoryItem = categories.lastOrNull()

    items(
        items = categories,
        key = { category -> category.id }
    ) { category ->
        val onUpdateEmojiClicked = { emoji: String ->
            onUserClickEvent(UserClickEvent.UpdateCategory(category = category.copy(emoji = emoji)))
        }
        val onUpdateName = { categoryName: String ->
            onUserClickEvent(UserClickEvent.UpdateCategory(category = category.copy(name = categoryName)))
        }
        val onUpdateBudgetTargetClicked = { newBudgetTarget: Money ->
            onUserClickEvent(UserClickEvent.UpdateCategory(category = category.copy(budgetTarget = newBudgetTarget)))
        }
        val onUpdateAssignedMoneyClicked = { newAssignedMoney: Money ->
            onUserClickEvent(UserClickEvent.UpdateCategory(category = category.copy(assignedMoney = newAssignedMoney)))
        }
        val onDeleteCategoryClicked = { onUserClickEvent(UserClickEvent.DeleteCategory(category = category)) }

        AnimatedVisibility(
            visible = group.isExpanded,
            enter = fadeIn(animationSpec = tween(ANIMATION_TIME_1_SECOND)) +
                    expandVertically(animationSpec = tween(ANIMATION_TIME_300_MILLISECONDS)),
            exit = shrinkVertically(animationSpec = tween(ANIMATION_TIME_300_MILLISECONDS)),
            modifier = Modifier.padding(
                top = if (category == firstCategoryItem) spacing.xxs else spacing.halfDp,
                bottom = if (category == lastCategoryItem) spacing.xxs else spacing.halfDp
            )
        ) {
            if (category.isInitialCategory) {
                EmptyCategoryItem(onUpdateNameClicked = onUpdateName)
            } else
                Column {
                    Category(
                        emoji = category.emoji,
                        categoryName = category.name,
                        budgetTarget = category.budgetTarget,
                        assignedMoney = category.assignedMoney,
                        availableMoney = category.availableMoney,
                        progress = category.progress,
                        optionalText = category.optionalText,
                        onUpdateEmojiClicked = onUpdateEmojiClicked,
                        onUpdateCategoryClicked = onUpdateName,
                        onUpdateBudgetTargetClicked = onUpdateBudgetTargetClicked,
                        onUpdateAssignedMoneyClicked = onUpdateAssignedMoneyClicked,
                        onDeleteCategoryClicked = onDeleteCategoryClicked
                    )
                    if (category != lastCategoryItem)
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

@ComponentPreview
@Composable
fun BudgetListPreview() {
    GetALifeTheme {
        Surface {
            BudgetList(
                groups = fakeCategories(),
                isLoading = false,
                errorMessage = null,
                onUserScrolling = { },
                onUserClickEvent = { }
            )
        }
    }
}

@ComponentPreview
@Composable
fun BudgetListEmptyGroupsPreview() {
    GetALifeTheme {
        Surface {
            BudgetList(
                groups = emptyMap(),
                isLoading = false,
                errorMessage = null,
                onUserScrolling = { },
                onUserClickEvent = { }
            )
        }
    }
}