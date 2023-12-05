package app.tinygiants.getalife.presentation.budget.composables

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.theme.*
import app.tinygiants.getalife.util.toCurrencyFormattedString

@Composable
fun Category(
    name: String,
    budgetTarget: Double,
    availableMoney: Double,
    optionalText: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            val budgetTargetBackground = when {
                availableMoney == 0.0 -> MaterialTheme.colorScheme.outlineVariant
                availableMoney < budgetTarget -> onWarning
                else -> onSuccess
            }
            Box(
                modifier = Modifier
                    .background(budgetTargetBackground, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                val formattedBudgetTarget = availableMoney.toCurrencyFormattedString()
                val availableMoneyColor = when {
                    availableMoney > 0.00 -> MaterialTheme.colorScheme.scrim
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Text(
                    text = formattedBudgetTarget,
                    style = MaterialTheme.typography.titleMedium,
                    color = availableMoneyColor
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        ) {
            val progressBackground = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            val progressColor = when {
                availableMoney == 0.0 -> MaterialTheme.colorScheme.secondary
                availableMoney < budgetTarget -> warning
                else -> success
            }
            LinearProgressIndicator(
                progress = (availableMoney / budgetTarget).toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = progressColor,
                trackColor = progressBackground,
                strokeCap = StrokeCap.Round
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (!optionalText.isNullOrBlank()) {
            Text(
                text = optionalText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Preview(name = "Light", widthDp = 400)
@Preview(name = "Dark", widthDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FullCategoryPreview() {
    GetALifeTheme {
        Surface {
            Category(
                name = "Rent",
                budgetTarget = 940.00,
                availableMoney = 940.00,
                optionalText = optionalExampleText(0.00)
            )
        }
    }
}

@Preview(name = "Light", widthDp = 400)
@Preview(name = "Dark", widthDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SemiFilledCategoryPreview() {
    GetALifeTheme {
        Surface {
            Category(
                name = "Rent",
                budgetTarget = 940.00,
                availableMoney = 470.00,
                optionalText = optionalExampleText(gap = 940.00 - 470.00)
            )
        }
    }
}

@Preview(name = "Light", widthDp = 400)
@Preview(name = "Dark", widthDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EmptyCategoryPreview() {
    GetALifeTheme {
        Surface {
            Category(
                name = "Rent",
                budgetTarget = 940.00,
                availableMoney = 0.0,
                optionalText = optionalExampleText(gap = 940.00 - 0.0)
            )
        }
    }
}

fun optionalExampleText(gap: Double) = "${gap.toCurrencyFormattedString()} more needed by the 30th"