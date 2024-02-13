package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tinygiants.getalife.presentation.budget.ErrorMessage
import app.tinygiants.getalife.presentation.budget.Money
import app.tinygiants.getalife.presentation.budget.UiCategory
import app.tinygiants.getalife.presentation.budget.UiHeader
import app.tinygiants.getalife.presentation.budget.UserClickEvent
import app.tinygiants.getalife.presentation.budget.fakeCategories
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.LightAndDarkPreviews
import app.tinygiants.getalife.theme.spacing

const val ANIMATION_TIME_1_SECOND = 1000
const val ANIMATION_TIME_300_MILLISECONDS = 300

@Composable
fun BudgetsList(
    groups: Map<UiHeader, List<UiCategory>>,
    isLoading: Boolean,
    errorMessage: ErrorMessage?,
    onUserClickEvent: (UserClickEvent) -> Unit
) {
    AnimatedVisibility(
        visible = !isLoading && errorMessage == null,
        enter = slideInVertically(),
        exit = slideOutVertically()
    ) {
        LazyColumn(contentPadding = PaddingValues(spacing.extraSmall)) {
            groups.forEach { (header, items) ->

                stickyHeader(
                    header = header,
                    onUserClickEvent = onUserClickEvent
                )

                items(
                    isHeaderExpanded = header.isExpanded,
                    uiCategories = items,
                    onUserClickEvent = onUserClickEvent
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.stickyHeader(
    header: UiHeader,
    onUserClickEvent: (UserClickEvent) -> Unit
) {
    val onHeaderClicked = { onUserClickEvent(UserClickEvent.ToggleCategoryGroupExpandedState(header = header.copy(isExpanded = !header.isExpanded))) }
    val onUpdateHeaderNameClicked =
        { updatedCategoryName: String -> onUserClickEvent(UserClickEvent.UpdateHeaderName(header = header.copy(name = updatedCategoryName))) }
    val onDeleteHeaderClicked = { onUserClickEvent(UserClickEvent.DeleteHeader(header = header)) }
    val onAddCategoryClicked =
        { categoryName: String -> onUserClickEvent(UserClickEvent.AddCategory(headerId = header.id, categoryName = categoryName)) }

    this.stickyHeader(key = header.id) {
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
    isHeaderExpanded: Boolean,
    uiCategories: List<UiCategory>,
    onUserClickEvent: (UserClickEvent) -> Unit
) {

    val firstCategoryItem = uiCategories.firstOrNull()
    val lastCategoryItem = uiCategories.lastOrNull()

    this.items(
        items = uiCategories,
        key = { uiCategory -> uiCategory.id }
    ) { uiCategory ->
        val onUpdateNameClicked = { updatedCategoryName: String ->
            val updatedCategory = uiCategory.copy(name = updatedCategoryName)
            onUserClickEvent(UserClickEvent.UpdateCategory(category = updatedCategory))
        }
        val onUpdateBudgetTargetClicked = { newBudgetTarget: Money ->
            val updateCategory = uiCategory.copy(budgetTarget = newBudgetTarget)
            onUserClickEvent(UserClickEvent.UpdateCategory(category = updateCategory))
        }
        val onUpdateAvailableMoneyClicked = { newAvailableMoney: Money ->
            val updatedCategory = uiCategory.copy(availableMoney = newAvailableMoney)
            onUserClickEvent(UserClickEvent.UpdateCategory(category = updatedCategory))
        }
        val onDeleteCategoryClicked = { onUserClickEvent(UserClickEvent.DeleteCategory(category = uiCategory)) }

        AnimatedVisibility(
            visible = isHeaderExpanded,
            enter = fadeIn(animationSpec = tween(ANIMATION_TIME_1_SECOND)) +
                    expandVertically(animationSpec = tween(ANIMATION_TIME_300_MILLISECONDS)),
            exit = shrinkVertically(animationSpec = tween(ANIMATION_TIME_300_MILLISECONDS)),
            modifier = Modifier.padding(
                top = if (uiCategory == firstCategoryItem) spacing.tiny else spacing.halfDp,
                bottom = if (uiCategory == lastCategoryItem) spacing.tiny else spacing.halfDp
            )
        ) {
            Category(
                name = uiCategory.name,
                budgetTarget = uiCategory.budgetTarget,
                availableMoney = uiCategory.availableMoney,
                progress = uiCategory.progress,
                optionalText = uiCategory.optionalText,
                onUpdateCategoryClicked = onUpdateNameClicked,
                onUpdateBudgetTargetClicked = onUpdateBudgetTargetClicked,
                onUpdateAvailableMoneyClicked = onUpdateAvailableMoneyClicked,
                onDeleteCategoryClicked = onDeleteCategoryClicked
            )
        }
    }
}

@LightAndDarkPreviews
@Composable
fun BudgetListPreview() {
    GetALifeTheme {
        Surface {
            BudgetsList(
                groups = fakeCategories(),
                isLoading = false,
                errorMessage = null,
                onUserClickEvent = { }
            )
        }
    }
}