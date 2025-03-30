package app.tinygiants.getalife.presentation.shared_composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.theme.spacing

@Composable
fun LoadingIndicator(
    isLoading: Boolean,
    errorMessage: ErrorMessage?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isLoading && errorMessage == null,
        modifier = modifier.padding(top = spacing.m)
    ) {
        CircularProgressIndicator(
            strokeWidth = spacing.xs,
            modifier = Modifier.size(20.dp)
        )
    }
}