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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.onbaordinganswers.DailyCost

@Composable
fun Step5DailyCosts(
    onDailyCostsClicked: (List<DailyCost>) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCosts by remember { mutableStateOf(listOf<DailyCost>()) }
    val options = DailyCost.entries

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
                text = "Jeder Tag ist anders, aber jeden Tag zahl ich was anderes. Was sind deine täglichen Kosten?",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEach { cost ->
                    val isSelected = cost in selectedCosts
                    OutlinedButton(
                        onClick = {
                            val updatedList = if (cost == DailyCost.NoneOfThese) {
                                if (isSelected) selectedCosts - DailyCost.NoneOfThese else listOf(DailyCost.NoneOfThese)
                            } else {
                                if (isSelected) {
                                    selectedCosts - cost
                                } else {
                                    (selectedCosts - DailyCost.NoneOfThese) + cost
                                }
                            }
                            selectedCosts = updatedList
                            onDailyCostsClicked(updatedList)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
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
                                painter = painterResource(id = cost.toIconRes()),
                                contentDescription = "${cost.name} icon",
                                modifier = Modifier.size(25.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = cost.displayName(),
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onNextClicked,
                enabled = selectedCosts.isNotEmpty(),
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

fun DailyCost.displayName(): String {
    return when (this) {
        DailyCost.Shopping -> "Einkäufe"
        DailyCost.Phone -> "Telefon"
        DailyCost.Gym -> "Gym"
        DailyCost.Internet -> "Internet"
        DailyCost.SelfCare -> "Selbstführsorge, Gesundheit und Entspannung"
        DailyCost.Clothing -> "Kleidung"
        DailyCost.NoneOfThese -> "nichts davon"
    }
}

@DrawableRes
fun DailyCost.toIconRes(): Int {
    return when (this) {
        DailyCost.Shopping -> R.drawable.ic_shopping
        DailyCost.Phone -> R.drawable.ic_phone
        DailyCost.Gym -> R.drawable.ic_sport
        DailyCost.Internet -> R.drawable.ic_wifi
        DailyCost.SelfCare -> R.drawable.ic_meditate
        DailyCost.Clothing -> R.drawable.ic_accessable
        DailyCost.NoneOfThese -> R.drawable.ic_none
    }
}

@Preview(showBackground = true)
@Composable
fun Step5DailyCostsPreview() {
    MaterialTheme {
        Step5DailyCosts(
            onDailyCostsClicked = {},
            onNextClicked = {}
        )
    }
}