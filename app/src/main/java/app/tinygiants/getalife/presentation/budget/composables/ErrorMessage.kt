package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun BoxScope.ErrorMessage(errorMessage: String?) {
    AnimatedVisibility(
        visible = errorMessage.isNullOrBlank().not(),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .background(color = MaterialTheme.colorScheme.onError)
    ) {
        Text(
            text = errorMessage!!,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}