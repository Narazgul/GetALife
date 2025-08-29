package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.AccountSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.AmountInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.CompletionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.DateSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.TextInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.TransactionTypeSelector
import app.tinygiants.getalife.theme.GetALifeTheme
import java.time.LocalDate
import kotlin.time.Clock

/**
 * Step components specific to the Inflow transaction flow.
 * Inflow Flow: Type -> Amount -> Account -> Partner -> Date -> Optional -> Done
 *
 * Note: Category step is skipped for Inflow transactions
 */

// Step 1: Transaction Type Selection (shared with all flows)
@Composable
fun InflowTypeStep(
    selectedDirection: TransactionDirection?,
    availableAccounts: List<Account>,
    onTypeSelected: (TransactionDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    TransactionTypeSelector(
        selectedDirection = selectedDirection,
        availableAccounts = availableAccounts,
        onTypeSelected = onTypeSelected,
        modifier = modifier
    )
}

// Step 2: Amount Input (shared with all flows)
@Composable
fun InflowAmountStep(
    amountText: String,
    onAmountTextChanged: (String) -> Unit,
    onAmountChanged: (Money) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    AmountInput(
        amountText = amountText,
        onAmountTextChanged = onAmountTextChanged,
        onAmountChanged = onAmountChanged,
        onNextClicked = onNextClicked,
        title = "Wie viel Geld hast du erhalten?",
        modifier = modifier
    )
}

// Step 3: Account Selection (where the money goes TO)
@Composable
fun InflowAccountStep(
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit,
    onCreateAccountClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    AccountSelector(
        title = "Auf welches Konto soll das Geld?",
        accounts = accounts,
        selectedAccount = selectedAccount,
        onAccountSelected = onAccountSelected,
        onCreateAccountClicked = onCreateAccountClicked,
        modifier = modifier
    )
}

// Step 4: Partner Input
@Composable
fun InflowPartnerStep(
    currentPartner: String,
    onPartnerChanged: (String) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextInput(
        title = "Von wem hast du das Geld erhalten?",
        value = currentPartner,
        onValueChange = onPartnerChanged,
        onNextClicked = onNextClicked,
        placeholder = "z.B. Arbeitgeber, Familie, Kunde",
        modifier = modifier
    )
}

// Step 5: Date Selection (shared with all flows)
@Composable
fun InflowDateStep(
    selectedDate: LocalDate?,
    showDatePicker: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onShowDatePickerChanged: (Boolean) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    DateSelector(
        title = "Wann hast du das Geld erhalten?",
        selectedDate = selectedDate,
        showDatePicker = showDatePicker,
        onDateSelected = onDateSelected,
        onShowDatePickerChanged = onShowDatePickerChanged,
        onNextClicked = onNextClicked,
        modifier = modifier
    )
}

// Step 6: Optional Details
@Composable
fun InflowOptionalStep(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    onFinishClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextInput(
        title = "Möchtest du eine Notiz zur Einnahme hinzufügen?",
        value = description,
        onValueChange = onDescriptionChanged,
        onNextClicked = onFinishClicked,
        placeholder = "Notiz (optional)",
        nextButtonText = "✨ Einnahme speichern",
        isRequired = false,
        maxLines = 3,
        modifier = modifier
    )
}

// Step 7: Completion
@Composable
fun InflowCompletionStep(
    onSwitchToStandardMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompletionStep(
        onSwitchToStandardMode = onSwitchToStandardMode,
        title = "💰 Einnahme gespeichert!",
        message = "Deine Einnahme wurde erfolgreich hinzugefügt.\nDein Kontostand wurde entsprechend aktualisiert.",
        buttonText = "Weitere Transaktion hinzufügen",
        modifier = modifier
    )
}

// Preview Data
private val previewAccounts = listOf(
    Account(
        id = 1L,
        name = "Girokonto",
        balance = Money(1250.0),
        type = AccountType.Checking,
        listPosition = 0,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    ),
    Account(
        id = 2L,
        name = "Sparkonto",
        balance = Money(5000.0),
        type = AccountType.Savings,
        listPosition = 1,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )
)

// Previews
@Preview(name = "Inflow - Type Step", showBackground = true)
@Composable
private fun InflowTypeStepPreview() {
    GetALifeTheme {
        InflowTypeStep(
            selectedDirection = TransactionDirection.Inflow,
            availableAccounts = previewAccounts,
            onTypeSelected = { }
        )
    }
}

@Preview(name = "Inflow - Amount Step", showBackground = true)
@Composable
private fun InflowAmountStepPreview() {
    GetALifeTheme {
        InflowAmountStep(
            amountText = "1500",
            onAmountTextChanged = { },
            onAmountChanged = { },
            onNextClicked = { }
        )
    }
}

@Preview(name = "Inflow - Account Step", showBackground = true)
@Composable
private fun InflowAccountStepPreview() {
    GetALifeTheme {
        InflowAccountStep(
            accounts = previewAccounts,
            selectedAccount = previewAccounts[0],
            onAccountSelected = { },
            onCreateAccountClicked = { }
        )
    }
}

@Preview(name = "Inflow - Partner Step", showBackground = true)
@Composable
private fun InflowPartnerStepPreview() {
    GetALifeTheme {
        InflowPartnerStep(
            currentPartner = "Arbeitgeber",
            onPartnerChanged = { },
            onNextClicked = { }
        )
    }
}

@Preview(name = "Inflow - Completion Step", showBackground = true)
@Composable
private fun InflowCompletionStepPreview() {
    GetALifeTheme {
        InflowCompletionStep(
            onSwitchToStandardMode = { }
        )
    }
}