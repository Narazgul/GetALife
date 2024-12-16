package app.tinygiants.getalife.presentation.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.R
import app.tinygiants.getalife.presentation.UiText
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen() {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScreen(
        uiState = uiState,
        onUserClickEvent = viewModel::onUserClickEvent
    )
}

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onUserClickEvent: (UserClickEvent) -> Unit = { }
) {
    var quoteAlphaTarget by remember { mutableFloatStateOf(0f) }
    var quoteScaleTarget by remember { mutableFloatStateOf(1f) }

    var appNameAlphaTarget by remember { mutableFloatStateOf(0f) }
    var appNameScaleTarget by remember { mutableFloatStateOf(1f) }
    var appNameLetterSpacingTarget by remember { mutableFloatStateOf(0f) }

    val quoteAlpha by animateFloatAsState(
        targetValue = quoteAlphaTarget,
        animationSpec = tween(durationMillis = 5000),
        label = "quoteAlpha"
    )
    val quoteScale by animateFloatAsState(
        targetValue = quoteScaleTarget,
        animationSpec = tween(durationMillis = 7000),
        label = "quoteScale"
    )

    val appNameAlpha by animateFloatAsState(
        targetValue = appNameAlphaTarget,
        animationSpec = tween(8000),
        label = "appNameAlpha"
    )
    val appNameScale by animateFloatAsState(
        targetValue = appNameScaleTarget,
        animationSpec = tween(
            durationMillis = 15000,
            easing = FastOutSlowInEasing
        ),
        label = "appNameScale"
    )
    val appNameLetterSpacing by animateFloatAsState(
        targetValue = appNameLetterSpacingTarget,
        animationSpec = tween(durationMillis = 8000),
        label = "quoteLetterSpacing"
    )

    LaunchedEffect(Unit) {
        quoteAlphaTarget = 1f
        quoteScaleTarget = 1.1f
        delay(timeMillis = 5000)
        appNameAlphaTarget = 1f
        appNameScaleTarget = 1.5f
        appNameLetterSpacingTarget = 0.2f
        delay(timeMillis = 2000)
        quoteAlphaTarget = 0f
        delay(timeMillis = 5000)
        onUserClickEvent(UserClickEvent.Click)
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = uiState.quote.asString(),
            style = MaterialTheme.typography.titleMedium.copy(textMotion = TextMotion.Animated),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(quoteAlpha)
                .padding(horizontal = spacing.xl)
                .scale(quoteScale)
        )

        Spacer(modifier = Modifier.size(spacing.m))

        Text(
            text = uiState.appName.asString(),
            style = MaterialTheme.typography.headlineSmall.copy(textMotion = TextMotion.Animated),
            letterSpacing = TextUnit(value = appNameLetterSpacing, TextUnitType.Em),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(appNameAlpha)
                .padding(horizontal = spacing.xl)
                .scale(appNameScale)
        )
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview(@PreviewParameter(OnboardingScreenPreviewProvider::class) uiState: OnboardingUiState) {
    GetALifeTheme {
        Surface {
            OnboardingScreen(uiState = uiState)
        }
    }
}

class OnboardingScreenPreviewProvider : PreviewParameterProvider<OnboardingUiState> {
    override val values: Sequence<OnboardingUiState>
        get() = sequenceOf(
            OnboardingUiState(
                quote = UiText.DynamicString("Bewegendes Zitat hier"),
                appName = UiText.StringResource(R.string.appName),
                isLoading = false,
                errorMessage = null
            )
        )
}