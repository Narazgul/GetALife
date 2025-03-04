package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.onbaordinganswers.HowYouKnowUs
import app.tinygiants.getalife.theme.spacing

@Composable
fun Step2HowYouKnowUs(
    onHowYouKnowUsClicked: (HowYouKnowUs) -> Unit,
    onNextClicked: () -> Unit
) {
    var howYouKnowUs by remember { mutableStateOf(HowYouKnowUs.Unknown) }
    val options = HowYouKnowUs.entries - HowYouKnowUs.Unknown

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Wie hast du von GetALife erfahren?",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEach { option ->
                    val isSelected = (option == howYouKnowUs)

                    OutlinedButton(
                        onClick = {
                            howYouKnowUs = option
                            onHowYouKnowUsClicked(option)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        ),
                        shape = RoundedCornerShape(spacing.m),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(option.toIconRes()),
                                contentDescription = "${option.name} icon",
                                modifier = Modifier.size(25.dp)
                            )
                            Spacer(modifier = Modifier.width(spacing.xl))
                            Text(
                                text = stringResource(option.toTitleRes()),
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(spacing.xl))

            Button(
                onClick = onNextClicked,
                shape = RoundedCornerShape(spacing.m),
                enabled = howYouKnowUs != HowYouKnowUs.Unknown,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Weiter")
            }
        }
    }
}

@StringRes
fun HowYouKnowUs.toTitleRes(): Int {
    return when (this) {
        HowYouKnowUs.AppStore -> R.string.how_you_know_us_app_store
        HowYouKnowUs.Instagram -> R.string.how_you_know_us_instagram
        HowYouKnowUs.Facebook -> R.string.how_you_know_us_facebook
        HowYouKnowUs.Tiktok -> R.string.how_you_know_us_tiktok
        HowYouKnowUs.News -> R.string.how_you_know_us_news
        HowYouKnowUs.FriendsAndFamily -> R.string.how_you_know_us_friends_and_family
        HowYouKnowUs.GoogleSearch -> R.string.how_you_know_us_google_search
        HowYouKnowUs.Youtube -> R.string.how_you_know_us_youtube
        HowYouKnowUs.Other -> R.string.how_you_know_us_other
        else -> R.string.error_title
    }
}

@DrawableRes
fun HowYouKnowUs.toIconRes(): Int {
    return when (this) {
        HowYouKnowUs.AppStore -> R.drawable.ic_playstore
        HowYouKnowUs.Instagram -> R.drawable.ic_instagram
        HowYouKnowUs.Facebook -> R.drawable.ic_facebook
        HowYouKnowUs.Tiktok -> R.drawable.ic_tiktok
        HowYouKnowUs.News -> R.drawable.ic_news
        HowYouKnowUs.FriendsAndFamily -> R.drawable.ic_friends_family
        HowYouKnowUs.GoogleSearch -> R.drawable.ic_google
        HowYouKnowUs.Youtube -> R.drawable.ic_youtube
        HowYouKnowUs.Other -> R.drawable.ic_question
        else -> R.drawable.ic_add
    }
}

@Composable
@Preview(showBackground = true)
fun Step2Preview() {
    MaterialTheme {
        Step2HowYouKnowUs(
            onHowYouKnowUsClicked = { },
            onNextClicked = { }
        )
    }
}