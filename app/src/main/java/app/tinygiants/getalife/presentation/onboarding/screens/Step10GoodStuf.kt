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
import app.tinygiants.getalife.domain.model.onbaordinganswers.GoodStuff

@Composable
fun Step10GoodStuff(
    onGoodStuffClicked: (List<GoodStuff>) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGoodStuff by remember { mutableStateOf(listOf<GoodStuff>()) }
    val options = GoodStuff.entries

    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Noch mehr Lust auf die guten Sachen?",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { stuff ->
                    val isSelected = stuff in selectedGoodStuff
                    OutlinedButton(
                        onClick = {
                            val updatedList = if (stuff == GoodStuff.NoneOfThese) {
                                if (isSelected) selectedGoodStuff - GoodStuff.NoneOfThese
                                else listOf(GoodStuff.NoneOfThese)
                            } else {
                                if (isSelected) selectedGoodStuff - stuff
                                else (selectedGoodStuff - GoodStuff.NoneOfThese) + stuff
                            }
                            selectedGoodStuff = updatedList
                            onGoodStuffClicked(updatedList)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stuff.displayName(),
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onNextClicked,
                shape = RoundedCornerShape(8.dp),
                enabled = selectedGoodStuff.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Weiter")
            }
        }
    }
}

fun GoodStuff.displayName(): String {
    return when (this) {
        GoodStuff.DiningOut -> "Essen gehen"
        GoodStuff.Entertainment -> "Unterhaltung"
        GoodStuff.VideoGames -> "Videospiele"
        GoodStuff.Hobbies -> "Hobbies"
        GoodStuff.Donations -> "Spenden"
        GoodStuff.Gifts -> "Geschenke"
        GoodStuff.HomeImprovement -> "Heim verschönern"
        GoodStuff.Party -> "Feiern"
        GoodStuff.GuiltFreeSpending -> "Ausgeben ohne schuldig fühlen"
        else -> "Nichts davon"
    }
}

@Composable
@Preview(showBackground = true)
fun Step10GoodStuffPreview() {
    MaterialTheme {
        Step10GoodStuff(
            onGoodStuffClicked = {},
            onNextClicked = {}
        )
    }
}