package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tinygiants.getalife.presentation.budget.Category
import app.tinygiants.getalife.presentation.budget.ErrorMessage
import app.tinygiants.getalife.presentation.budget.Header
import app.tinygiants.getalife.presentation.budget.exampleMap
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.LightAndDarkPreviews
import app.tinygiants.getalife.theme.spacing

const val ANIMATION_TIME_1_SECOND = 1000
const val ANIMATION_TIME_300_MILLISECONDS = 300

@Composable
fun BudgetsList(
    categories: Map<Header, List<Category>>,
    isLoading: Boolean,
    errorMessage: ErrorMessage?
) {
    AnimatedVisibility(
        visible = !isLoading && errorMessage == null,
        enter = slideInVertically(),
        exit = slideOutVertically()
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(spacing.halfDp),
            contentPadding = PaddingValues(spacing.default)
        ) {
            categories.forEach { (header, items) ->

                stickyHeader(header = header)
                items(isHeaderExpanded = header.isExpanded, items = items)

            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.stickyHeader(header: Header) {
    this.stickyHeader(key = header.id) {
        CategoryHeader(
            name = header.name,
            sumOfAvailableMoney = header.sumOfAvailableMoney,
            isExpanded = header.isExpanded,
            onHeaderClicked = header.toggleExpanded
        )
    }
}

private fun LazyListScope.items(isHeaderExpanded: Boolean, items: List<Category>) {

    val firstCategoryItem = items.firstOrNull()
    val lastCategoryItem = items.lastOrNull()

    this.items(items = items, key = { item -> item.id }) { item ->
        AnimatedVisibility(
            visible = isHeaderExpanded,
            enter = fadeIn(animationSpec = tween(ANIMATION_TIME_1_SECOND)) +
                    expandVertically(animationSpec = tween(ANIMATION_TIME_300_MILLISECONDS)),
            exit = shrinkVertically(animationSpec = tween(ANIMATION_TIME_300_MILLISECONDS)),
            modifier = Modifier.padding(
                top = if (item == firstCategoryItem) spacing.tiny else spacing.halfDp,
                bottom = if (item == lastCategoryItem) spacing.tiny else spacing.halfDp
            )
        ) {
            if (items.isEmpty()) AddCategoryItem { 1 }
            else Category(
                name = item.name,
                budgetTarget = item.budgetTarget,
                availableMoney = item.availableMoney,
                progress = item.progress,
                optionalText = item.optionalText
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
                categories = exampleMap(),
                isLoading = false,
                errorMessage = null
            )
        }
    }
}