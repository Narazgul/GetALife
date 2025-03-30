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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.onbaordinganswers.Debt
import app.tinygiants.getalife.presentation.shared_composables.AnimatedBorderButton

@Composable
fun Step4Debts(
    onDebtClicked: (List<Debt>) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDebts by remember { mutableStateOf(listOf<Debt>()) }
    val options = Debt.entries

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
                text = "Hast du derzeit Schulden?",
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
                options.forEach { debtOption ->
                    val isSelected = debtOption in selectedDebts

                    val onClick = {
                        val updatedList = if (debtOption == Debt.NoDebt) {
                            if (isSelected) {
                                selectedDebts - Debt.NoDebt
                            } else {
                                listOf(Debt.NoDebt)
                            }
                        } else {
                            if (isSelected) {
                                selectedDebts - debtOption
                            } else {
                                (selectedDebts - Debt.NoDebt) + debtOption
                            }
                        }
                        selectedDebts = updatedList
                        onDebtClicked(updatedList)
                    }

                    if (debtOption == Debt.NoDebt) AnimatedBorderButton(
                        repeatCount = 4,
                        borderWidth = 2.dp,
                        onCardClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                        ) {
                        ButtonContent(debtOption = debtOption)
                    }
                    else OutlinedButton(
                        onClick = onClick,
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
                    ) { ButtonContent(debtOption = debtOption) }
                }
            }

            Button(
                onClick = onNextClicked,
                enabled = selectedDebts.isNotEmpty(),
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
fun ButtonContent(debtOption: Debt) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = debtOption.toIconRes()),
            contentDescription = "${debtOption.name} icon",
            modifier = Modifier.size(25.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = debtOption.displayName(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

fun Debt.displayName(): String {
    return when (this) {
        Debt.CreditCard -> "Kreditkarte"
        Debt.StudentLoan -> "Studienkredit"
        Debt.CarLoan -> "Autokredit"
        Debt.Mortgage -> "Hauskredit"
        Debt.PersonalLoan -> "Persönliche Kredite"
        Debt.NoDebt -> "Keine Schulden <3"
    }
}

@DrawableRes
fun Debt.toIconRes(): Int {
    return when (this) {
        Debt.CreditCard -> R.drawable.ic_creditcard
        Debt.StudentLoan -> R.drawable.ic_book
        Debt.CarLoan -> R.drawable.ic_car
        Debt.Mortgage -> R.drawable.ic_house
        Debt.PersonalLoan -> R.drawable.ic_person
        Debt.NoDebt -> R.drawable.ic_celebrate
    }
}

@Preview(showBackground = true)
@Composable
fun Step4DebtPreview() {
    MaterialTheme {
        Step4Debts(
            onDebtClicked = { },
            onNextClicked = { }
        )
    }
}