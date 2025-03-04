package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.annotation.DrawableRes
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
import app.tinygiants.getalife.domain.model.onbaordinganswers.Transportation

@Composable
fun Step3Transportation(
    onTransportationClicked: (List<Transportation>) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTransportations by remember { mutableStateOf(listOf<Transportation>()) }
    val options = Transportation.entries

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
                text = "Welche Fortbewegungsmittel nutzt du?",
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
                    val isSelected = option in selectedTransportations

                    OutlinedButton(
                        onClick = {
                            val updatedList = if (isSelected) selectedTransportations - option else selectedTransportations + option
                            selectedTransportations = updatedList
                            onTransportationClicked(updatedList)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                            Image(
                                painter = painterResource(id = option.toIconRes()),
                                contentDescription = "Icon for ${option.name}",
                                modifier = Modifier.size(25.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option.displayName(),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Weiter")
            }
        }
    }
}

@Composable
fun Transportation.displayName(): String {
    return stringResource(
        id = when (this) {
            Transportation.Auto -> R.string.transportation_auto
            Transportation.Public -> R.string.transportation_public
            Transportation.UberTaxis -> R.string.transportation_uber_taxis
            Transportation.Motorbike -> R.string.transportation_motorbike
            Transportation.Bike -> R.string.transportation_bike
            Transportation.Foot -> R.string.transportation_foot
        }
    )
}

@DrawableRes
fun Transportation.toIconRes(): Int {
    return when (this) {
        Transportation.Auto -> R.drawable.ic_car
        Transportation.Public -> R.drawable.ic_bus
        Transportation.UberTaxis -> R.drawable.ic_taxi
        Transportation.Motorbike -> R.drawable.ic_helmet
        Transportation.Bike -> R.drawable.ic_bike
        Transportation.Foot -> R.drawable.ic_walk
    }
}

@Composable
@Preview(showBackground = true)
fun Step3TransportationPreview() {
    MaterialTheme {
        Step3Transportation(
            onTransportationClicked = { },
            onNextClicked = { }
        )
    }
}