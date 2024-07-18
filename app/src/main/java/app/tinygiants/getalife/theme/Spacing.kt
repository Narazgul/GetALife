package app.tinygiants.getalife.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val halfDp: Dp = 0.5.dp,
    val xxs: Dp = 1.dp,
    val xs: Dp = 2.dp,
    val s: Dp = 4.dp,
    val m: Dp = 8.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 64.dp,

    val default: Dp = m,
)

val LocalSpacing = compositionLocalOf { Spacing() }

val spacing: Spacing
@Composable
@ReadOnlyComposable
get() = LocalSpacing.current