package app.tinygiants.getalife.presentation.main_app.budget.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus

@Composable
fun MonthNavigator(
    currentMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentRealMonth = YearMonth(today.year, today.month)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.m),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                val previousMonth = currentMonth.minus(1, DateTimeUnit.MONTH)
                onMonthChanged(previousMonth)
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month"
            )
        }

        Text(
            text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clickable {
                    if (currentMonth != currentRealMonth) {
                        onMonthChanged(currentRealMonth)
                    }
                }
        )

        IconButton(
            onClick = {
                val nextMonth = currentMonth.plus(1, DateTimeUnit.MONTH)
                onMonthChanged(nextMonth)
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month"
            )
        }
    }
}

@Preview
@Composable
fun MonthNavigatorPreview() {
    GetALifeTheme {
        MonthNavigator(
            currentMonth = YearMonth(2024, Month.JANUARY),
            onMonthChanged = {}
        )
    }
}