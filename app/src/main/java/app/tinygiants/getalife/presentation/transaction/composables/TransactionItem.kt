package app.tinygiants.getalife.presentation.transaction.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import app.tinygiants.getalife.theme.success

@Composable
fun TransactionItem(
    description: String,
    category: String,
    amount: Money,
    direction: TransactionDirection
) {
    Row(
        modifier = Modifier.padding(spacing.default),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(spacing.small))
            Text(
                text = category,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = amount.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = if (direction == TransactionDirection.Inflow) success else MaterialTheme.colorScheme.error
        )
    }
}

@Preview
@Composable
private fun TransactionItemPreview() {
    GetALifeTheme {
        Surface {
            TransactionItem(
                description = "Bäckerei",
                category = "Lebensmittel",
                amount = Money(value = -3.20),
                direction = TransactionDirection.Outflow
            )
        }
    }
}