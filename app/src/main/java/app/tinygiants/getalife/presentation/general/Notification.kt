package app.tinygiants.getalife.presentation.general

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.data.remote.firebase.RemoteFirebaseUsers
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import im.crisp.client.external.notification.CrispNotificationClient
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun ComponentActivity.Notification(
    notificationLauncher: ActivityResultLauncher<String>,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val onSendTokenToCrisp = { firebaseToken: String ->
        CrispNotificationClient.sendTokenToCrisp(this, firebaseToken)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            LaunchedEffect(Unit) {
                viewModel.sendNotificationTokenToFirestoreAndCrisp(onSendTokenToCrisp = onSendTokenToCrisp)
            }
        } else {
            RequestNotificationPermission(this, notificationLauncher)
        }

    } else {
        LaunchedEffect(Unit) {
            viewModel.sendNotificationTokenToFirestoreAndCrisp(onSendTokenToCrisp = onSendTokenToCrisp)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RequestNotificationPermission(
    activity: Activity,
    notificationLauncher: ActivityResultLauncher<String>
) {
    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS))
        NotificationPermissionRationale(onConfirmClick = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) })
    else notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
}

@Preview
@Composable
fun NotificationPermissionRationale(onConfirmClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Benachrichtigungen aktivieren",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Erhalte wichtige Updates direkt auf dein Gerät. " +
                        "Bitte erlaube uns, Benachrichtigungen zu senden.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = {}) {
                    Text("Später")
                }
                Button(onClick = onConfirmClick) {
                    Text("Erlauben")
                }
            }
        }
    }
}

@HiltViewModel
class NotificationViewModel @Inject constructor(private val remoteFirebaseUsers: RemoteFirebaseUsers) : ViewModel() {

    fun sendNotificationTokenToFirestoreAndCrisp(onSendTokenToCrisp: (firebaseToken: String) -> Unit) =
        FirebaseMessaging.getInstance().token.addOnSuccessListener { firebaseToken ->

            viewModelScope.launch {
                onSendTokenToCrisp(firebaseToken)
                remoteFirebaseUsers.updateNotificationToken(firebaseToken)
            }
        }
}