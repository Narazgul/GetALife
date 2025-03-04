package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BudgetPlanFinish(
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bulletPoints = listOf(
        "⏱ Bessere Kontrolle über deine Ausgaben",
        "📚 Konkrete Ziele statt unklarer Budgets",
        "✅ Weniger Stress durch klare Aufteilung",
        "⚙️ Flexibilität, wenn sich etwas ändert",
        "💰 Mehr Geld für das, was wirklich zählt"
    )

    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Oberer Bereich
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\uD83C\uDF89",
                    fontSize = 60.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Dein persönliches Zero-Based Budgeting Programm ist fertig!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    bulletPoints.forEach { text ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // Weiter-Button
            Button(
                onClick = onNextClicked,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors()
            ) {
                Text("Weiter")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetPlanFinishScreenPreview() {
    MaterialTheme {
        BudgetPlanFinish(onNextClicked = {})
    }
}