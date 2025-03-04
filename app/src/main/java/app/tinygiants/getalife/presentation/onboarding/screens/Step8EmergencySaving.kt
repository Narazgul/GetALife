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
import app.tinygiants.getalife.domain.model.onbaordinganswers.EmergencySaving

@Composable
fun Step8EmergencySaving(
    onSavingsClicked: (List<EmergencySaving>) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSavings by remember { mutableStateOf(listOf<EmergencySaving>()) }
    val options = EmergencySaving.entries

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
                text = "Ein kleines Finanzpolster beruhigt. Was davon möchtest du anlegen?",
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
                options.forEach { saving ->
                    val isSelected = saving in selectedSavings
                    OutlinedButton(
                        onClick = {
                            val updatedList = if (saving == EmergencySaving.NoneOfThese) {
                                if (isSelected) selectedSavings - EmergencySaving.NoneOfThese
                                else listOf(EmergencySaving.NoneOfThese)
                            } else {
                                if (isSelected) {
                                    selectedSavings - saving
                                } else {
                                    (selectedSavings - EmergencySaving.NoneOfThese) + saving
                                }
                            }
                            selectedSavings = updatedList
                            onSavingsClicked(updatedList)
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
                            .height(70.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = saving.displayName(),
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
                enabled = selectedSavings.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Weiter")
            }
        }
    }
}

fun EmergencySaving.displayName(): String {
    return when (this) {
        EmergencySaving.EmergencyFund -> "Notgroschen"
        EmergencySaving.RetirementInsurance -> "Rentenzusatzversicherung"
        EmergencySaving.Investments -> "Investitionen"
        EmergencySaving.NoneOfThese -> "nichts davon"
    }
}

@Composable
@Preview(showBackground = true)
fun Step8EmergencySavingPreview() {
    MaterialTheme {
        Step8EmergencySaving(
            onSavingsClicked = {},
            onNextClicked = {}
        )
    }
}