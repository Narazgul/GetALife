package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import app.tinygiants.getalife.domain.model.onbaordinganswers.Subscription

@Composable
fun Step6Subscription(
    onSubscriptionClicked: (List<Subscription>) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubscriptions by remember { mutableStateOf(listOf<Subscription>()) }
    val options = Subscription.entries

    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welche Abo’s hast du?",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEach { sub ->
                    val isSelected = sub in selectedSubscriptions
                    OutlinedButton(
                        onClick = {
                            val updatedList = if (sub == Subscription.NoneOfThese) {
                                if (isSelected) selectedSubscriptions - Subscription.NoneOfThese else listOf(Subscription.NoneOfThese)
                            } else {
                                if (isSelected) selectedSubscriptions - sub else (selectedSubscriptions - Subscription.NoneOfThese) + sub
                            }
                            selectedSubscriptions = updatedList
                            onSubscriptionClicked(updatedList)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub.displayName(),
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onNextClicked,
                shape = RoundedCornerShape(8.dp),
                enabled = selectedSubscriptions.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Weiter")
            }
        }
    }
}

fun Subscription.displayName(): String {
    return when (this) {
        Subscription.Spotify -> "Spotify"
        Subscription.YouTubePremium -> "YouTube Premium"
        Subscription.Streaming -> "Netflix & Co"
        Subscription.Gym -> "Gym"
        Subscription.OnlineCourses -> "Online Kurse"
        Subscription.AudioOrEbooks -> "Audio oder ebooks"
        Subscription.News -> "Nachrichten"
        Subscription.FoodDelivery -> "Essenslieferungen"
        Subscription.NoneOfThese -> "nichts davon"
    }
}

@Composable
@Preview(showBackground = true)
fun Step6SubscriptionPreview() {
    MaterialTheme {
        Step6Subscription(
            onSubscriptionClicked = {},
            onNextClicked = {}
        )
    }
}