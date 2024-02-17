package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Header
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.budget.ErrorMessage
import app.tinygiants.getalife.presentation.budget.UserClickEvent
import app.tinygiants.getalife.presentation.budget.fakeCategories
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.LightAndDarkPreviews
import app.tinygiants.getalife.theme.spacing

const val ANIMATION_TIME_1_SECOND = 1000
const val ANIMATION_TIME_300_MILLISECONDS = 300

@Composable
fun BudgetsList(
    groups: Map<Header, List<Category>>,
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
        val onCreateCategoryClicked = { categoryName: String ->
            val updateCategory = category.copy(name = categoryName)
            onUserClickEvent(UserClickEvent.ReplaceEmptyCategory(updateCategory))
        }
        val onUpdateNameClicked = { updatedCategoryName: String ->
            val updatedCategory = category.copy(name = updatedCategoryName)
            onUserClickEvent(UserClickEvent.UpdateCategory(category = updatedCategory))
        }
        val onUpdateBudgetTargetClicked = { newBudgetTarget: Money ->
            val updateCategory = category.copy(budgetTarget = newBudgetTarget)
            onUserClickEvent(UserClickEvent.UpdateCategory(category = updateCategory))
        }
        val onUpdateAvailableMoneyClicked = { newAvailableMoney: Money ->
            val updatedCategory = category.copy(availableMoney = newAvailableMoney)
            onUserClickEvent(UserClickEvent.UpdateCategory(category = updatedCategory))
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
            if (category.isEmptyCategory) {
                EmptyCategoryItem(
                    onReplaceEmptyClicked = onCreateCategoryClicked
                )
            } else
                Column {
                    Category(
                        name = category.name,
                        budgetTarget = category.budgetTarget,
                        availableMoney = category.availableMoney,
                        progress = category.progress,
                        optionalText = category.optionalText,
                        onUpdateCategoryClicked = onUpdateNameClicked,
                        onUpdateBudgetTargetClicked = onUpdateBudgetTargetClicked,
                        onUpdateAvailableMoneyClicked = onUpdateAvailableMoneyClicked,
                        onDeleteCategoryClicked = onDeleteCategoryClicked
                    )
                    if (category != lastCategoryItem) Spacer(
                        modifier = Modifier
                            .height(spacing.tiny)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
                    )
                }
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