package app.tinygiants.getalife.presentation.shared_composables

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.theme.GetALifeTheme
import kotlin.math.roundToInt

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

@Composable
fun FluidMenu(modifier: Modifier = Modifier) {

    val renderEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getRenderEffect()
    } else {
        null
    }

    Box(modifier = modifier
        .height(450.dp)
        .width(200.dp)) {
        AnimatedFabGroup(renderEffect = renderEffect?.asComposeRenderEffect())
        AnimatedFabGroup()
    }
}

@Composable
fun AnimatedFabGroup(
    modifier: Modifier = Modifier,
    renderEffect: androidx.compose.ui.graphics.RenderEffect? = null,
) {

    val firstButtonAnimation = rememberInfiniteTransition(label = "firstOffset")
    val secondButtonAnimation = rememberInfiniteTransition(label = "secondOffset")

    val firstButtonYOffset by firstButtonAnimation.animateFloat(
        initialValue = 0f,
        targetValue = -400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "firstYOffset"
    )
    val secondButtonYOffset by secondButtonAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "secondYOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { this.renderEffect = renderEffect }
    ) {
        AnimatedFab(modifier = Modifier
            .align(Alignment.Center)
            .offset { IntOffset(x = 0, y = firstButtonYOffset.roundToInt()) })
        AnimatedFab(modifier = Modifier.align(Alignment.Center))
        AnimatedFab(modifier = Modifier
            .align(Alignment.Center)
            .offset { IntOffset(x = 0, y = secondButtonYOffset.roundToInt()) })
    }
}

@Composable
fun AnimatedFab(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.secondaryContainer,
    onClick: () -> Unit = { }
) {
    FloatingActionButton(
        onClick = onClick,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        containerColor = background,
        shape = RoundedCornerShape(50.dp),
        modifier = modifier
    ) {
        Icon(Icons.Filled.Add, "Floating action button.")
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview
@Composable
private fun CustomFabAddPreview() {
    GetALifeTheme {
        Surface {
            Box {
                FluidMenu()
            }
        }
    }
}