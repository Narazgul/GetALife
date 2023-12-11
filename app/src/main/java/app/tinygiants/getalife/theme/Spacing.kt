package app.tinygiants.getalife.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val halfDp: Dp = 0.5.dp,
    val tiny: Dp = 1.dp,
    val extraSmall: Dp = 2.dp,
    val small: Dp = 4.dp,
    val medium: Dp = 8.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 32.dp,
    val giant: Dp = 64.dp,

    val default: Dp = medium,
)

val LocalSpacing = compositionLocalOf { Spacing() }

val spacing: Spacing
@Composable
@ReadOnlyComposable
get() = LocalSpacing.current