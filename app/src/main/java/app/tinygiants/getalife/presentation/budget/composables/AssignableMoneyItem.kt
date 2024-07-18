package app.tinygiants.getalife.presentation.budget.composables

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import app.tinygiants.getalife.theme.success

@Composable
fun AssignableMoney(assignableMoney: Money) {
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
        assignableMoney.value > 0.0 -> stringResource(
            R.string.distribute_available_money,
            assignableMoney.formattedMoney
        )
        assignableMoney.value == 0.0 -> stringResource(R.string.everything_distributed)
        else -> stringResource(R.string.more_distributed_than_available, assignableMoney.formattedPositiveMoney)
    }

    Spacer(modifier = Modifier.height(spacing.s))

    Text(
        text = text,
        textAlign = TextAlign.Center,
        color = textColor,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .background(
                color = background,
                shape = RoundedCornerShape(size = spacing.m)
            )
            .padding(spacing.l)
    )

    Spacer(modifier = Modifier.height(spacing.s))
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