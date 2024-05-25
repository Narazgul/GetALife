package app.tinygiants.getalife.presentation.composables

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.R
import app.tinygiants.getalife.theme.GetALifeTheme

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun CustomFabAdd() {
    val renderEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) getRenderEffect().asComposeRenderEffect()
    else null

    CustomFabAdd(renderEffect = renderEffect)
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun CustomFabAdd(modifier: Modifier = Modifier, renderEffect: androidx.compose.ui.graphics.RenderEffect?) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedFabGroup(renderEffect = renderEffect)
        AnimatedFabGroup()
    }
}

@Composable
fun AnimatedFabGroup(
    animationProgress: Float = 0f,
    renderEffect: androidx.compose.ui.graphics.RenderEffect? = null,
    toggleAnimation: () -> Unit = { }
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp)
            .graphicsLayer { this.renderEffect = renderEffect },
        contentAlignment = Alignment.BottomCenter
    ) {

        AnimatedFab(
            icon = ImageVector.vectorResource(R.drawable.ic_group),
            modifier = Modifier.padding(bottom = 200.dp),
//                PaddingValues(bottom = 72.dp, end = 210.dp) * FastOutSlowInEasing.transform(0f, 0.8f, animationProgress)),
            opacity = LinearEasing.transform(0.2f, 0.7f, animationProgress)
        )

        AnimatedFab(
            icon = Icons.Default.Add,
//            text = stringResource(R.string.add_transaction),
            modifier = Modifier.padding(
                PaddingValues(
                    bottom = 72.dp,
                    start = 210.dp
                ) * FastOutSlowInEasing.transform(0.2f, 1.0f, animationProgress)
            ),
            opacity = LinearEasing.transform(0.4f, 0.9f, animationProgress)
        )

        AnimatedFab(
            icon = Icons.Default.Add,
            modifier = Modifier.
            padding(bottom = 20.dp)
                .rotate(
                225 * FastOutSlowInEasing.transform(from = 0.35f, to = 0.65f, value = animationProgress)
            ),
            onClick = toggleAnimation
        )
    }
}

@Composable
fun AnimatedFab(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    opacity: Float = 1f,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = { }
) {
    FloatingActionButton(
        onClick = onClick,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        containerColor = backgroundColor,
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )

            if (text != null) Text(text = text)
        }
    }
}

fun Easing.transform(from: Float, to: Float, value: Float): Float {
    return transform(((value - from) * (1f / (to - from))).coerceIn(0f, 1f))
}

operator fun PaddingValues.times(value: Float): PaddingValues = PaddingValues(
    top = calculateTopPadding() * value,
    bottom = calculateBottomPadding() * value,
    start = calculateStartPadding(LayoutDirection.Ltr) * value,
    end = calculateEndPadding(LayoutDirection.Ltr) * value
)

@RequiresApi(Build.VERSION_CODES.S)
private fun getRenderEffect(): RenderEffect {
    val blurEffect = RenderEffect
        .createBlurEffect(80f, 80f, Shader.TileMode.MIRROR)

    val alphaMatrix = RenderEffect.createColorFilterEffect(
        ColorMatrixColorFilter(
            ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1f, 0f, 0f,
                    0f, 0f, 0f, 50f, -1500f
                )
            )
        )
    )

    return RenderEffect
        .createChainEffect(alphaMatrix, blurEffect)
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview
@Composable
private fun CustomFabAddPreview() {
    GetALifeTheme {
        Surface {
            CustomFabAdd()
        }
    }
}