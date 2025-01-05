package app.tinygiants.getalife.presentation

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable

@Composable
fun ComponentActivity.CrispChat(shouldOpenCrispChat: Boolean = false) {
    if (!shouldOpenCrispChat) return

    val crispIntent = Intent(this, im.crisp.client.external.ChatActivity::class.java)
    startActivity(crispIntent)
}