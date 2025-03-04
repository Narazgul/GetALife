package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun CreateBudget(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    val overallProgress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 10_000, easing = LinearEasing)
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    val progressPercent = (overallProgress * 100).roundToInt()

    val bar1Raw = (overallProgress * 3f).coerceIn(0f, 1f)
    val bar1Progress = 1 - (1 - bar1Raw).pow(2)

    val bar2Progress = ((overallProgress * 3f) - 1f).coerceIn(0f, 1f)

    val bar3Raw = ((overallProgress * 3f) - 2f).coerceIn(0f, 1f)
    val bar3Progress = customEase(bar3Raw)

    val animationDone = overallProgress >= 0.9999f

    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 46.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "SarahJH",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Ein echter Wendepunkt!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Budget wird zusammengestellt",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                LinearProgressIndicator(
                    progress = { bar1Progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Kategorien vorbereiten",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                LinearProgressIndicator(
                    progress = { bar2Progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Plan abschließen",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                LinearProgressIndicator(
                    progress = { bar3Progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            Button(
                onClick = onFinished,
                shape = RoundedCornerShape(8.dp),
                enabled = animationDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors()
            ) {
                Text("Weiter")
            }
        }
    }
}

fun customEase(t: Float): Float {
    return if (t < 0.5f) {
        // First half: easeOutQuad
        0.5f * (1 - (1 - t * 2).pow(2))
    } else {
        // Second half: slower progress using a quadratic function
        0.5f + 0.5f * ((t - 0.5f) * 2).pow(2)
    }
}

@Preview(showBackground = true)
@Composable
fun CreateBudgetPreview() {
    MaterialTheme {
        CreateBudget(onFinished = {})
    }
}