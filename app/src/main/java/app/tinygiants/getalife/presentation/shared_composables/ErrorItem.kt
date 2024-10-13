package app.tinygiants.getalife.presentation.shared_composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.tinygiants.getalife.R
import app.tinygiants.getalife.presentation.UiText
import app.tinygiants.getalife.theme.ComponentPreview
import app.tinygiants.getalife.theme.GetALifeTheme

@Immutable
data class ErrorMessage(
    val title: UiText?,
    val subtitle: UiText?
)

@Composable
fun ErrorMessage(
    errorMessage: ErrorMessage?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = errorMessage != null,
        modifier = modifier.background(color = MaterialTheme.colorScheme.onError)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = errorMessage?.title?.asString() ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = errorMessage?.subtitle?.asString() ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@ComponentPreview
@Composable
fun ErrorMessagePreview() {
    GetALifeTheme {
        ErrorMessage(
            errorMessage = ErrorMessage(
                title = UiText.StringResource(R.string.error_title),
                subtitle = UiText.StringResource(R.string.error_subtitle)
            )
        )
    }
}