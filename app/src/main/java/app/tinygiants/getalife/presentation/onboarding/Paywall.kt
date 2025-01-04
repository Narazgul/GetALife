package app.tinygiants.getalife.presentation.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.superwall.sdk.Superwall
import com.superwall.sdk.paywall.presentation.register

@Composable
fun Paywall(onNavigateToMainApp: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Superwall.instance.register("ShowPaywall") {
            onNavigateToMainApp()
        }
    }
}