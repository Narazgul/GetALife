package app.tinygiants.getalife.presentation.budget.composables

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

@Composable
fun AddCategoryItem(
    onItemClick: () -> Int
) {
    val color = MaterialTheme.colorScheme.outline
    val stroke = Stroke(
        width = 10f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 30f))
    )
    val cornerRadius = spacing.default

    Text(
        text = "Jetzt Kategorie hinzufügen",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(spacing.small)
            )
            .drawBehind {
                drawRoundRect(
                    color = color,
                    style = stroke,
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
            .padding(
                horizontal = spacing.large,
                vertical = spacing.large
            )
    )
}

@Preview(name = "Light", widthDp = 400)
@Preview(name = "Dark", widthDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddCategoryPreview() {
    GetALifeTheme {
        Surface {
            AddCategoryItem { 0 }
        }
    }
}