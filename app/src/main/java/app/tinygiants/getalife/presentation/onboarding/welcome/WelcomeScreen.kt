package app.tinygiants.getalife.presentation.onboarding.welcome

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
import androidx.compose.ui.text.TextStyle
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
import app.tinygiants.getalife.presentation.main_app.shared_composables.UiText
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(onNavigateToPaywall: () -> Unit) {
    val viewModel: WelcomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WelcomeScreen(
        uiState = uiState,
        onNavigateToNextScreen = viewModel::onUserClickEvent,
        onNavigateToPaywall = onNavigateToPaywall
    )
}

@Composable
fun WelcomeScreen(
    uiState: WelcomeUiState,
    onNavigateToNextScreen: (UserClickEvent) -> Unit = { },
    onNavigateToPaywall: () -> Unit = { }
) {
    var quoteAlphaTarget by remember { mutableFloatStateOf(0f) }
    var quoteScaleTarget by remember { mutableFloatStateOf(1f) }

    var appNameAlphaTarget by remember { mutableFloatStateOf(0f) }
    var appNameScaleTarget by remember { mutableFloatStateOf(1f) }
    var appNameLetterSpacingTarget by remember { mutableFloatStateOf(0f) }

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

        onNavigateToPaywall()
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {

        WelcomeText(
            text = uiState.quote.asString(),
            style = MaterialTheme.typography.titleMedium.copy(textMotion = TextMotion.Animated),
            alphaTarget = quoteAlphaTarget,
            alphaDuration = 5000,
            scaleTarget = quoteScaleTarget,
            scaleDuration = 7000,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(spacing.m))

        WelcomeText(
            text = uiState.appName.asString(),
            style = MaterialTheme.typography.headlineSmall.copy(textMotion = TextMotion.Animated),
            alphaTarget = appNameAlphaTarget,
            alphaDuration = 8000,
            scaleTarget = appNameScaleTarget,
            scaleDuration = 15000,
            letterSpacingTarget = appNameLetterSpacingTarget,
            letterSpacingDuration = 8000,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WelcomeText(
    text: String,
    style: TextStyle,
    alphaTarget: Float,
    alphaDuration: Int,
    scaleTarget: Float,
    scaleDuration: Int,
    letterSpacingTarget: Float = 0f,
    letterSpacingDuration: Int = 0,
    modifier: Modifier = Modifier,
) {
    val letterSpacing by animateFloatAsState(
        targetValue = letterSpacingTarget,
        animationSpec = tween(durationMillis = letterSpacingDuration),
        label = "${text}letterSpacing"
    )

    val alpha by animateFloatAsState(
        targetValue = alphaTarget,
        animationSpec = tween(durationMillis = alphaDuration),
        label = "${text}Alpha"
    )
    val scale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = tween(durationMillis = scaleDuration),
        label = "${text}Scale"
    )

    Text(
        text = text,
        style = style,
        letterSpacing = TextUnit(value = letterSpacing, TextUnitType.Em),
        textAlign = TextAlign.Center,
        modifier = modifier
            .alpha(alpha)
            .padding(horizontal = spacing.xl)
            .scale(scale)
    )
}

@Preview
@Composable
private fun OnboardingScreenPreview(@PreviewParameter(OnboardingScreenPreviewProvider::class) uiState: WelcomeUiState) {
    GetALifeTheme {
        Surface {
            WelcomeScreen(uiState = uiState)
        }
    }
}

class OnboardingScreenPreviewProvider : PreviewParameterProvider<WelcomeUiState> {
    override val values: Sequence<WelcomeUiState>
        get() = sequenceOf(
            WelcomeUiState(
                quote = UiText.DynamicString("Bewegendes Zitat hier"),
                appName = UiText.StringResource(R.string.appName),
                isLoading = false,
                errorMessage = null
            )
        )
}