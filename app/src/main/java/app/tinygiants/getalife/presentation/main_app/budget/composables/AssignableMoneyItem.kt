package app.tinygiants.getalife.presentation.main_app.budget.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import app.tinygiants.getalife.presentation.main_app.shared_composables.UiText.DynamicString
import app.tinygiants.getalife.presentation.main_app.budget.BannerUiState
import app.tinygiants.getalife.presentation.main_app.budget.BannerUiState.AllAssigned
import app.tinygiants.getalife.presentation.main_app.budget.BannerUiState.AssignableMoneyAvailable
import app.tinygiants.getalife.presentation.main_app.budget.BannerUiState.OverDistributed
import app.tinygiants.getalife.presentation.main_app.budget.BannerUiState.Overspent
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import app.tinygiants.getalife.theme.success

@Composable
fun AssignableMoney(banner: BannerUiState) {

    val backgroundColor = when (banner) {
        is AssignableMoneyAvailable -> success
        is AllAssigned -> MaterialTheme.colorScheme.outlineVariant
        is OverDistributed -> MaterialTheme.colorScheme.error
        is Overspent -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    val textColor = when (banner) {
        is AssignableMoneyAvailable -> MaterialTheme.colorScheme.onPrimary
        is AllAssigned -> MaterialTheme.colorScheme.primary
        is OverDistributed -> MaterialTheme.colorScheme.onError
        is Overspent -> MaterialTheme.colorScheme.primaryContainer
    }

    Spacer(modifier = Modifier.height(spacing.s))

    Text(
        text = banner.text.asString(),
        textAlign = TextAlign.Center,
        color = textColor,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(size = spacing.m)
            )
            .padding(spacing.l)
    )

    Spacer(modifier = Modifier.height(spacing.s))
}

@PreviewLightDark
@Composable
private fun AssignableMoneyPreview(@PreviewParameter(AssignableMoneyPreviewProvider::class) banner: BannerUiState) {
    GetALifeTheme {
        Surface {
            Column {
                AssignableMoney(banner = banner)
            }
        }
    }
}

class AssignableMoneyPreviewProvider : PreviewParameterProvider<BannerUiState> {
    override val values: Sequence<BannerUiState>
        get() = sequenceOf(
            AssignableMoneyAvailable(text = DynamicString("Distribute available money to categories: 100,00 €")),
            AllAssigned(text = DynamicString("Everything is distributed 👍🏼")),
            OverDistributed(text = DynamicString("You've distributed more than you own. Remove 100,00 € from categories")),
            Overspent(text = DynamicString("Remove 10,00 € from overspent categories"))
        )
}