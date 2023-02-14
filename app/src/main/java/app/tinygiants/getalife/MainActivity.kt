package app.tinygiants.getalife

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.ui.theme.GetALifeTheme
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GetALifeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(30.dp)
            .wrapContentSize(Alignment.Center)
            .clip(shape = MaterialTheme.shapes.medium)
    ) {
        Text(
            text = "Test",
            style = MaterialTheme.typography.displayLarge,
            modifier = modifier.border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.large
            )
        )
        LoadingAnimation(lottieAnimationRawId = R.raw.loading)
        LoadingAnimation(lottieAnimationRawId = R.raw.jumper)
    }
}

@Composable
private fun LoadingAnimation(lottieAnimationRawId: Int, isRepeating: Boolean = true) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieAnimationRawId))

    if (isRepeating) LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    else LottieAnimation(composition = composition)
}

@Preview(name = "Light", widthDp = 400, heightDp = 400)
@Preview(name = "Dark", widthDp = 400, heightDp = 400, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    GetALifeTheme {
        Surface {
            HomeScreen()
        }
    }
}