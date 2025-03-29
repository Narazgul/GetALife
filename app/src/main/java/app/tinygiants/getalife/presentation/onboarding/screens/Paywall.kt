package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.tinygiants.getalife.R
import app.tinygiants.getalife.theme.spacing
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.superwall.sdk.compose.PaywallComposable
import com.superwall.sdk.paywall.presentation.internal.state.PaywallResult
import com.superwall.sdk.paywall.view.PaywallView
import com.superwall.sdk.paywall.view.delegate.PaywallViewCallback

@Composable
fun PaywallNeverSubscribed(onNavigateToMainApp: () -> Unit) {
    PaywallComposable(
        placement = "NeverSubscribed",
        errorComposable = { throwable -> PaywallError(throwable = throwable) },
        delegate = object : PaywallViewCallback {
            override fun onFinished(paywall: PaywallView, result: PaywallResult, shouldDismiss: Boolean) {
                if (result is PaywallResult.Purchased) onNavigateToMainApp()
            }
        })
}

@Composable
fun PaywallPastSubscriber(onNavigateToMainApp: () -> Unit) {
    PaywallComposable(
        placement = "PastSubscriber",
        errorComposable = { throwable -> PaywallError(throwable = throwable) },
        delegate = object : PaywallViewCallback {
            override fun onFinished(paywall: PaywallView, result: PaywallResult, shouldDismiss: Boolean) {
                if (result is PaywallResult.Purchased) onNavigateToMainApp()
            }
        })
}

@Composable
fun PaywallError(throwable: Throwable) {
    Firebase.crashlytics.recordException(throwable)
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.display_paywall_error),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(spacing.xxl)
        )
    }
}