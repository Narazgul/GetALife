package app.tinygiants.getalife.presentation.budget.composables

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
import app.tinygiants.getalife.presentation.budget.Money
import app.tinygiants.getalife.theme.*
import app.tinygiants.getalife.util.toCurrencyFormattedString

@Composable
fun Category(
    name: String = "",
    budgetTarget: Money = Money(value =  0.0),
    availableMoney: Money = Money(value =  0.0),
    progress: Float = 0f,
    optionalText: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(spacing.small)
            )
            .border(
                BorderStroke(
                    width = spacing.tiny,
                    color = MaterialTheme.colorScheme.outlineVariant
                ),
                RoundedCornerShape(spacing.small)
            )
            .padding(
                horizontal = spacing.large,
                vertical = spacing.default)
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
                availableMoney.value == 0.0 -> MaterialTheme.colorScheme.outlineVariant
                availableMoney.value < budgetTarget.value -> onWarning
                else -> onSuccess
            }
            Box(
                modifier = Modifier
                    .background(
                        color = budgetTargetBackground,
                        shape = RoundedCornerShape(spacing.large)
                    )
                    .padding(
                        horizontal = spacing.default,
                        vertical = spacing.extraSmall
                    )
            ) {
                val formattedBudgetTarget = availableMoney.formattedMoney
                val availableMoneyColor = when {
                    availableMoney.value > 0.00 -> MaterialTheme.colorScheme.scrim
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Text(
                    text = formattedBudgetTarget,
                    style = MaterialTheme.typography.titleMedium,
                    color = availableMoneyColor
                )
            }
        }
        Spacer(modifier = Modifier.height(spacing.default))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.small)
        ) {
            val progressBackground = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            val progressColor = when {
                availableMoney.value == 0.0 -> MaterialTheme.colorScheme.secondary
                availableMoney.value < budgetTarget.value -> warning
                else -> success
            }
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = progressColor,
                trackColor = progressBackground,
                strokeCap = StrokeCap.Round
            )
        }
        Spacer(modifier = Modifier.height(spacing.default))
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

@LightAndDarkPreviews
@Composable
fun FullCategoryPreview() {
    GetALifeTheme {
        Surface {
            Category(
                name = "Rent",
                budgetTarget = Money(940.00),
                availableMoney = Money(940.00),
                progress = 1f,
                optionalText = optionalExampleText(0.00)
            )
        }
    }
}

@LightAndDarkPreviews
@Composable
fun SemiFilledCategoryPreview() {
    GetALifeTheme {
        Surface {
            Category(
                name = "Rent",
                budgetTarget = Money(940.00),
                availableMoney = Money(470.00),
                progress = (470.00 / 940.00).toFloat(),
                optionalText = optionalExampleText(gap = 940.00 - 470.00)
            )
        }
    }
}

@LightAndDarkPreviews
@Composable
fun EmptyCategoryPreview() {
    GetALifeTheme {
        Surface {
            Category(
                name = "Rent",
                budgetTarget = Money(940.00),
                availableMoney = Money(0.0),
                progress = (0.0 / 940.00).toFloat(),
                optionalText = optionalExampleText(gap = 940.00 - 0.0)
            )
        }
    }
}

fun optionalExampleText(gap: Double) = "${gap.toCurrencyFormattedString()} more needed by the 30th"