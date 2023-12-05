package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.presentation.budget.Category
import app.tinygiants.getalife.presentation.budget.ErrorMessage
import app.tinygiants.getalife.presentation.budget.Header

const val ANIMATION_TIME_300 = 300

@OptIn(ExperimentalFoundationApi::class)
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
            verticalArrangement = Arrangement.spacedBy(0.5.dp)
        ) {
            categories.forEach { (header, items) ->
                stickyHeader(key = header.id) {
                    CategoryHeader(
                        name = header.name,
                        sumOfAvailableMoney = items.sumOf { category -> category.availableMoney },
                        isExtended = header.isExpanded,
                        onHeaderClicked = header.toggleExpanded
                    )
                }
                items(items = items, key = { item -> item.id }) { item ->
                    AnimatedVisibility(
                        visible = header.isExpanded,
                        enter = fadeIn(animationSpec = tween(ANIMATION_TIME_300)) +
                                expandVertically(animationSpec = tween(ANIMATION_TIME_300)),
                        exit = fadeOut(animationSpec = tween(ANIMATION_TIME_300)) +
                                shrinkVertically(animationSpec = tween(ANIMATION_TIME_300))
                    ) {
                        Category(
                            name = item.name,
                            budgetTarget = item.budgetTarget,
                            availableMoney = item.availableMoney,
                            optionalText = item.optionalText
                        )
                    }
                }
            }
        }
    }
}