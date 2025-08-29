package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.AmountInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.CompletionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.DateSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.AccountSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.TextInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.TransactionTypeSelector
import app.tinygiants.getalife.theme.GetALifeTheme
import java.time.LocalDate
import kotlin.time.Clock

/**
 * Step components specific to the Transfer transaction flow.
 * Transfer Flow: Type -> Amount -> FromAccount -> ToAccount -> Date -> Optional -> Done
 *
 * Note: Partner and Category steps are skipped for Transfer transactions
 */

// Step 1: Transaction Type Selection (shared with all flows)
@Composable
fun TransferTypeStep(
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
fun TransferAmountStep(
    currentAmount: Money?,
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
        title = "Wie viel Geld möchtest du transferieren?",
        modifier = modifier
    )
}

// Step 3: From Account Selection (where the money comes FROM)
@Composable
fun TransferFromAccountStep(
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit,
    onCreateAccountClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    AccountSelector(
        title = "Von welchem Konto soll das Geld abgehen?",
        accounts = accounts,
        selectedAccount = selectedAccount,
        onAccountSelected = onAccountSelected,
        onCreateAccountClicked = onCreateAccountClicked,
        modifier = modifier
    )
}

// Step 4: To Account Selection (where the money goes TO)
@Composable
fun TransferToAccountStep(
    accounts: List<Account>, // Already filtered to exclude source account
    selectedToAccount: Account?,
    onToAccountSelected: (Account) -> Unit,
    modifier: Modifier = Modifier
) {
    AccountSelector(
        title = "Auf welches Konto soll das Geld transferiert werden?",
        accounts = accounts,
        selectedAccount = selectedToAccount,
        onAccountSelected = onToAccountSelected,
        onCreateAccountClicked = { /* No create new for destination account - user must pick from existing */ },
        modifier = modifier
    )
}

// Step 5: Date Selection (shared with all flows)
@Composable
fun TransferDateStep(
    selectedDate: LocalDate?,
    showDatePicker: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onShowDatePickerChanged: (Boolean) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    DateSelector(
        title = "Wann soll der Transfer stattfinden?",
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
fun TransferOptionalStep(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    onFinishClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextInput(
        title = "Möchtest du eine Notiz zum Transfer hinzufügen?",
        value = description,
        onValueChange = onDescriptionChanged,
        onNextClicked = onFinishClicked,
        placeholder = "Notiz (optional)",
        nextButtonText = "✨ Transfer ausführen",
        isRequired = false,
        maxLines = 3,
        modifier = modifier
    )
}

// Step 7: Completion
@Composable
fun TransferCompletionStep(
    onSwitchToStandardMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompletionStep(
        onSwitchToStandardMode = onSwitchToStandardMode,
        title = "↔️ Transfer abgeschlossen!",
        message = "Das Geld wurde erfolgreich zwischen deinen Konten transferiert.\nBeide Kontostände wurden entsprechend aktualisiert.",
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
    ),
    Account(
        id = 3L,
        name = "Tagesgeld",
        balance = Money(2500.0),
        type = AccountType.Savings,
        listPosition = 2,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )
)

// Previews
@Preview(name = "Transfer - Type Step", showBackground = true)
@Composable
private fun TransferTypeStepPreview() {
    GetALifeTheme {
        TransferTypeStep(
            selectedDirection = TransactionDirection.Unknown, // Transfer
            availableAccounts = previewAccounts,
            onTypeSelected = { }
        )
    }
}

@Preview(name = "Transfer - Amount Step", showBackground = true)
@Composable
private fun TransferAmountStepPreview() {
    GetALifeTheme {
        TransferAmountStep(
            currentAmount = Money(500.0),
            amountText = "500",
            onAmountTextChanged = { },
            onAmountChanged = { },
            onNextClicked = { }
        )
    }
}

@Preview(name = "Transfer - From Account Step", showBackground = true)
@Composable
private fun TransferFromAccountStepPreview() {
    GetALifeTheme {
        TransferFromAccountStep(
            accounts = previewAccounts,
            selectedAccount = previewAccounts[0], // Girokonto
            onAccountSelected = { },
            onCreateAccountClicked = { }
        )
    }
}

@Preview(name = "Transfer - To Account Step", showBackground = true)
@Composable
private fun TransferToAccountStepPreview() {
    GetALifeTheme {
        TransferToAccountStep(
            accounts = previewAccounts.drop(1), // Exclude source account (Girokonto)
            selectedToAccount = previewAccounts[1], // Sparkonto
            onToAccountSelected = { }
        )
    }
}

@Preview(name = "Transfer - To Account Step Empty", showBackground = true)
@Composable
private fun TransferToAccountStepEmptyPreview() {
    GetALifeTheme {
        TransferToAccountStep(
            accounts = emptyList(), // No destination accounts available
            selectedToAccount = null,
            onToAccountSelected = { }
        )
    }
}

@Preview(name = "Transfer - Completion Step", showBackground = true)
@Composable
private fun TransferCompletionStepPreview() {
    GetALifeTheme {
        TransferCompletionStep(
            onSwitchToStandardMode = { }
        )
    }
}