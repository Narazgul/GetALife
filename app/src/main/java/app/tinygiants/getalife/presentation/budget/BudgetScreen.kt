package app.tinygiants.getalife.presentation.budget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.presentation.budget.composables.Category
import app.tinygiants.getalife.presentation.budget.composables.CategoryHeader

@Composable
fun BudgetScreen(budgetState: BudgetState) {
    BudgetContent(
        categories = budgetState.categories,
        isLoading = budgetState.isLoading,
        errorMessage = budgetState.errorMessage
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetContent(
    categories: Map<Header, List<Category>>,
    isLoading: Boolean,
    errorMessage: String?
) {
    Box {
        AnimatedVisibility(
            visible = !errorMessage.isNullOrBlank(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(text = errorMessage!!)
        }
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !isLoading && errorMessage.isNullOrBlank(),
            enter = slideInVertically(),
            exit = slideOutVertically()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
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
                            enter = fadeIn(),
                            exit = fadeOut()
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
        AnimatedVisibility(
            visible = isLoading && errorMessage.isNullOrBlank(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(50.dp)
                .padding(16.dp)
        ) {
            CircularProgressIndicator(strokeWidth = 2.dp)
        }
    }
}