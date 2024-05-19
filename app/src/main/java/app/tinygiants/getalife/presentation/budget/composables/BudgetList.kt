package app.tinygiants.getalife.presentation.budget.composables

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tinygiants.getalife.domain.model.BudgetPurpose
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Header
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.budget.UserClickEvent
import app.tinygiants.getalife.presentation.budget.fakeCategories
import app.tinygiants.getalife.presentation.composables.ErrorMessage
import app.tinygiants.getalife.theme.ComponentPreview
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

const val ANIMATION_TIME_1_SECOND = 1000
const val ANIMATION_TIME_300_MILLISECONDS = 300

@Composable
fun BudgetList(
    groups: Map<Header, List<Category>>,
    isLoading: Boolean,
    errorMessage: ErrorMessage?,
    onUserClickEvent: (UserClickEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isLoading && errorMessage == null,
        enter = slideInVertically(),
        exit = slideOutVertically(),
        modifier = modifier
    ) {

        LazyColumn(contentPadding = PaddingValues(spacing.extraSmall)) {
            groups.forEach { (header, items) ->

                stickyHeader(
                    header = header,
                    onUserClickEvent = onUserClickEvent
                )

                items(
                    header = header,
                    categories = items,
                    onUserClickEvent = onUserClickEvent
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.stickyHeader(
    header: Header,
    onUserClickEvent: (UserClickEvent) -> Unit
) {
    val onHeaderClicked = { onUserClickEvent(UserClickEvent.UpdateHeader(header = header.copy(isExpanded = !header.isExpanded))) }

    val onUpdateHeaderNameClicked =
        { updatedCategoryName: String -> onUserClickEvent(UserClickEvent.UpdateHeader(header = header.copy(name = updatedCategoryName))) }

    val onDeleteHeaderClicked = { onUserClickEvent(UserClickEvent.DeleteHeader(header = header)) }

    val onAddCategoryClicked =
        { categoryName: String -> onUserClickEvent(UserClickEvent.AddCategory(headerId = header.id, categoryName = categoryName)) }

    stickyHeader(key = header.id) {
        CategoryHeader(
            name = header.name,
            sumOfAvailableMoney = header.sumOfAvailableMoney,
            isExpanded = header.isExpanded,
            onHeaderClicked = onHeaderClicked,
            onUpdateHeaderNameClicked = onUpdateHeaderNameClicked,
            onDeleteHeaderClicked = onDeleteHeaderClicked,
            onAddCategoryClicked = onAddCategoryClicked
        )
        Spacer(modifier = Modifier.height(spacing.tiny))
    }
}

private fun LazyListScope.items(
    header: Header,
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
        val onUpdateBudgetPurposeClicked = { newBudgetPurpose: BudgetPurpose ->
            onUserClickEvent(UserClickEvent.UpdateCategory(category = category.copy(budgetPurpose = newBudgetPurpose)))
        }
        val onUpdateAssignedMoneyClicked = { newAssignedMoney: Money ->
            onUserClickEvent(UserClickEvent.UpdateCategory(category = category.copy(assignedMoney = newAssignedMoney)))
        }
        val onDeleteCategoryClicked = { onUserClickEvent(UserClickEvent.DeleteCategory(category = category)) }

        AnimatedVisibility(
            visible = header.isExpanded,
            enter = fadeIn(animationSpec = tween(ANIMATION_TIME_1_SECOND)) +
                    expandVertically(animationSpec = tween(ANIMATION_TIME_300_MILLISECONDS)),
            exit = shrinkVertically(animationSpec = tween(ANIMATION_TIME_300_MILLISECONDS)),
            modifier = Modifier.padding(
                top = if (category == firstCategoryItem) spacing.tiny else spacing.halfDp,
                bottom = if (category == lastCategoryItem) spacing.tiny else spacing.halfDp
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
                        budgetPurpose = category.budgetPurpose,
                        assignedMoney = category.assignedMoney,
                        availableMoney = category.availableMoney,
                        progress = category.progress,
                        optionalText = category.optionalText,
                        onUpdateEmojiClicked = onUpdateEmojiClicked,
                        onUpdateCategoryClicked = onUpdateName,
                        onUpdateBudgetTargetClicked = onUpdateBudgetTargetClicked,
                        onUpdateBudgetPurposeClicked = onUpdateBudgetPurposeClicked,
                        onUpdateAssignedMoneyClicked = onUpdateAssignedMoneyClicked,
                        onDeleteCategoryClicked = onDeleteCategoryClicked
                    )
                    if (category != lastCategoryItem)
                        Spacer(
                            modifier = Modifier
                                .height(spacing.tiny)
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
                onUserClickEvent = { }
            )
        }
    }
}