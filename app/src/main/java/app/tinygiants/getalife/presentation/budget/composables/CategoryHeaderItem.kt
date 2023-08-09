package app.tinygiants.getalife.presentation.budget.composables

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.util.toCurrency

@Composable
fun CategoryHeader(
    name: String = "",
    sumOfAvailableMoney: Double = 0.00,
    isExtended: Boolean = true,
    onHeaderClicked: () -> Unit = { }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHeaderClicked() }
            .height(50.dp)
            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isExtended) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        val alignment = if (isExtended) Alignment.CenterHorizontally else Alignment.End
        Column(horizontalAlignment = alignment) {
            Text(text = "Available")
            val text = if (isExtended) "to spend" else sumOfAvailableMoney.toCurrency()
            val style = if (!isExtended) MaterialTheme.typography.titleMedium else LocalTextStyle.current
            Text(text = text, style = style)
        }
    }
}

@Preview(name = "Light", widthDp = 400)
@Preview(name = "Dark", widthDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CategoryHeaderExtendedPreview() {
    GetALifeTheme {
        Surface {
            CategoryHeader(
                name = "Quality of Life",
                sumOfAvailableMoney = 100.00
            )
        }
    }
}

@Preview(name = "Light", widthDp = 400)
@Preview(name = "Dark", widthDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CategoryHeaderPreview() {
    GetALifeTheme {
        Surface {
            CategoryHeader(
                name = "Quality of Life",
                sumOfAvailableMoney = 12345.67,
                isExtended = false
            )
        }
    }
}