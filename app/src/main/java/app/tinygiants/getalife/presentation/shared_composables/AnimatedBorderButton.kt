package app.tinygiants.getalife.presentation.shared_composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.theme.spacing

@Composable
fun AnimatedInfiniteBorderButton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(size = spacing.m),
    borderWidth: Dp = 1.dp,
    gradient: Brush = Brush.sweepGradient(listOf(Color.Magenta, Color.Cyan, Color.Magenta)),
    animationDuration: Int = 1000,
    onCardClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Infinite Color Animation")
    val degrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Infinite Colors"
    )

    Surface(
        modifier = modifier
            .clip(shape)
            .clickable { onCardClick() },
        shape = shape
    ) {
        Surface(
            modifier = Modifier
                .padding(borderWidth)
                .drawWithContent {
                    rotate(degrees = degrees) {
                        drawCircle(
                            brush = gradient,
                            radius = size.width,
                            blendMode = BlendMode.SrcIn,
                        )
                    }
                    drawContent()
                },
            color = MaterialTheme.colorScheme.surface,
            shape = shape
        ) {
            content()
        }
    }
}

@Composable
fun AnimatedBorderButton(
    modifier: Modifier = Modifier,
    repeatCount: Int = 2,
    shape: Shape = RoundedCornerShape(size = spacing.m),
    borderWidth: Dp = 1.dp,
    gradient: Brush = Brush.sweepGradient(listOf(Color.Magenta, Color.Cyan, Color.Magenta)),
    animationDuration: Int = 2000,
    onCardClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.outline

    val animatable = remember { Animatable(0f) }
    var animationFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repeat(repeatCount) {
            animatable.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = animationDuration, easing = LinearEasing)
            )
            if (it < 1) {
                animatable.snapTo(0f)
            }
        }
        animationFinished = true
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .clickable { onCardClick() },
        shape = shape
    ) {
        Surface(
            modifier = Modifier
                .padding(borderWidth)
                .drawWithContent {
                    if (animationFinished) {
                        drawCircle(
                            brush = SolidColor(borderColor),
                            radius = size.width,
                            blendMode = BlendMode.SrcIn
                        )
                    } else {
                        rotate(degrees = animatable.value) {
                            drawCircle(
                                brush = gradient,
                                radius = size.width,
                                blendMode = BlendMode.SrcIn
                            )
                        }
                    }
                    drawContent()
                },
            color = MaterialTheme.colorScheme.surface,
            shape = shape
        ) {
            content()
        }
    }
}

@Preview
@Composable
private fun AnimatedBorderButtonPreview() {
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedInfiniteBorderButton(
                    content = {
                        TextButton(
                            onClick = { }
                        ) {
                            Text(text = "Unendliche Animation")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(spacing.m))
                AnimatedBorderButton(
                    content = {
                        TextButton(
                            onClick = { }
                        ) {
                            Text(text = "Kurze Animation")
                        }
                    }
                )
            }
        }
    }
}