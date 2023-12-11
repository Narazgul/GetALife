package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.presentation.budget.ErrorMessage
import app.tinygiants.getalife.theme.spacing

@Composable
fun BoxScope.LoadingIndicator(isLoading: Boolean, errorMessage: ErrorMessage?) {
    AnimatedVisibility(
        visible = isLoading && errorMessage == null,
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        CircularProgressIndicator(
            strokeWidth = spacing.extraSmall,
            modifier = Modifier.size(20.dp)
        )
    }
}