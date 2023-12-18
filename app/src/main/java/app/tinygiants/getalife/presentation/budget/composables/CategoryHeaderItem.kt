package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.presentation.budget.Money
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.LightAndDarkPreviews
import app.tinygiants.getalife.theme.spacing

@Composable
fun CategoryHeader(
    name: String = "",
    sumOfAvailableMoney: Money = Money(value = 0.00),
    isExpanded: Boolean = false,
    onHeaderClicked: () -> Unit = { }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHeaderClicked() }
            .height(50.dp)
            .background(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(size = spacing.small)
            )
            .padding(horizontal = spacing.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = spacing.default)
        )
        Spacer(modifier = Modifier.weight(1f))
        val alignment = if (isExpanded) Alignment.CenterHorizontally else Alignment.End
        Column(horizontalAlignment = alignment) {
            Text(text = "Available")
            val text = if (isExpanded) "to spend" else sumOfAvailableMoney.formattedMoney
            val style = if (!isExpanded) MaterialTheme.typography.titleMedium else LocalTextStyle.current
            Text(text = text, style = style)
        }
    }
}

@LightAndDarkPreviews
@Composable
fun CategoryHeaderPreview() {
    GetALifeTheme {
        Surface {
            CategoryHeader(
                name = "Quality of Life",
                sumOfAvailableMoney = Money(value = 12345.67)
            )
        }
    }
}

@LightAndDarkPreviews
@Composable
fun CategoryHeaderExpandedPreview() {
    GetALifeTheme {
        Surface {
            CategoryHeader(
                name = "Quality of Life",
                sumOfAvailableMoney = Money(value = 100.00),
                isExpanded = true
            )
        }
    }
}