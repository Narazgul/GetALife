package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.LoadingIndicator(isLoading: Boolean, errorMessage: String?) {
    AnimatedVisibility(
        visible = isLoading && errorMessage.isNullOrBlank(),
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier.size(20.dp)
        )
    }
}