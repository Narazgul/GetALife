package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.tinygiants.getalife.presentation.budget.ErrorMessage

@Composable
fun BoxScope.ErrorMessage(errorMessage: ErrorMessage?) {
    AnimatedVisibility(
        visible = errorMessage != null,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .background(color = MaterialTheme.colorScheme.onError)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = errorMessage?.title ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = errorMessage?.subtitle ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}