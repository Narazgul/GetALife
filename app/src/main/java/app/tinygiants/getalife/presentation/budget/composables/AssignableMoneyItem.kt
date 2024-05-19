package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import app.tinygiants.getalife.theme.success

@Composable
fun ColumnScope.AssignableMoney(assignableMoney: Money) {
    val background = when {
        assignableMoney.value > 0.0 -> success
        assignableMoney.value == 0.0 -> MaterialTheme.colorScheme.outlineVariant
        else -> MaterialTheme.colorScheme.error
    }
    val textColor = when {
        assignableMoney.value > 0.0 -> MaterialTheme.colorScheme.onPrimary
        assignableMoney.value == 0.0 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onError
    }
    val text = when {
        assignableMoney.value > 0.0 -> "Verteil mich: ${assignableMoney.formattedMoney}"
        assignableMoney.value == 0.0 -> "Alles verteilt"
        else -> "Zu hast mehr Geld verteilt als du hast. Entferne ${assignableMoney.formattedPositiveMoney} aus den Kategorien"
    }

    Spacer(modifier = Modifier.height(spacing.small))

    Text(
        text = text,
        textAlign = TextAlign.Center,
        color = textColor,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .width(300.dp)
            .background(
                color = background,
                shape = RoundedCornerShape(size = spacing.medium)
            )
            .padding(spacing.large)
    )

    Spacer(modifier = Modifier.height(spacing.small))
}

@PreviewLightDark
@Composable
private fun AssignableMoneyPreview(@PreviewParameter(AssignableMoneyPreviewProvider::class) money: Money) {
    GetALifeTheme {
        Surface {
            Column {
                AssignableMoney(assignableMoney = money)
            }
        }
    }
}

class AssignableMoneyPreviewProvider : PreviewParameterProvider<Money> {
    override val values: Sequence<Money>
        get() = sequenceOf(
            Money(value = 100.00),
            Money(value = 0.00),
            Money(value = -100.00)
        )
}