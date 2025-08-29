package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.AccountSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.AmountInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.CategorySelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.CompletionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.DateSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.TextInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components.TransactionTypeSelector
import app.tinygiants.getalife.theme.GetALifeTheme
import java.time.LocalDate
import kotlin.time.Clock

/**
 * Step components specific to the Outflow transaction flow.
 * Outflow Flow: Type -> Amount -> Account -> Partner -> Category -> Date -> Optional -> Done
 *
 * Note: ToAccount step is skipped for Outflow transactions
 */

// Step 1: Transaction Type Selection (shared with all flows)
@Composable
fun OutflowTypeStep(
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
fun OutflowAmountStep(
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
        title = "Wie viel Geld hast du ausgegeben?",
        modifier = modifier
    )
}

// Step 3: Account Selection (where the money comes FROM)
@Composable
fun OutflowAccountStep(
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

// Step 4: Partner Input
@Composable
fun OutflowPartnerStep(
    currentPartner: String,
    onPartnerChanged: (String) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextInput(
        title = "Wofür hast du das Geld ausgegeben?",
        value = currentPartner,
        onValueChange = onPartnerChanged,
        onNextClicked = onNextClicked,
        placeholder = "z.B. Netflix, Edeka, Tankstelle",
        modifier = modifier
    )
}

// Step 5: Category Selection (required for Outflow)
@Composable
fun OutflowCategoryStep(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    onCreateCategoryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    CategorySelector(
        title = "In welche Kategorie fällt diese Ausgabe?",
        categories = categories,
        selectedCategory = selectedCategory,
        onCategorySelected = onCategorySelected,
        onCreateCategoryClicked = onCreateCategoryClicked,
        modifier = modifier
    )
}

// Step 6: Date Selection (shared with all flows)
@Composable
fun OutflowDateStep(
    selectedDate: LocalDate?,
    showDatePicker: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onShowDatePickerChanged: (Boolean) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    DateSelector(
        title = "Wann hast du das Geld ausgegeben?",
        selectedDate = selectedDate,
        showDatePicker = showDatePicker,
        onDateSelected = onDateSelected,
        onShowDatePickerChanged = onShowDatePickerChanged,
        onNextClicked = onNextClicked,
        modifier = modifier
    )
}

// Step 7: Optional Details
@Composable
fun OutflowOptionalStep(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    onFinishClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextInput(
        title = "Möchtest du eine Notiz zur Ausgabe hinzufügen?",
        value = description,
        onValueChange = onDescriptionChanged,
        onNextClicked = onFinishClicked,
        placeholder = "Notiz (optional)",
        nextButtonText = "✨ Ausgabe speichern",
        isRequired = false,
        maxLines = 3,
        modifier = modifier
    )
}

// Step 8: Completion
@Composable
fun OutflowCompletionStep(
    onSwitchToStandardMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompletionStep(
        onSwitchToStandardMode = onSwitchToStandardMode,
        title = "💳 Ausgabe gespeichert!",
        message = "Deine Ausgabe wurde erfolgreich kategorisiert.\nDein Budget und Kontostand wurden aktualisiert.",
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
        name = "Kreditkarte",
        balance = Money(-245.30),
        type = AccountType.CreditCard,
        listPosition = 1,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )
)

private val previewCategories = listOf(
    Category(
        id = 1L,
        groupId = 1L,
        emoji = "🍕",
        name = "Lebensmittel",
        budgetTarget = Money(400.0),
        monthlyTargetAmount = null,
        targetMonthsRemaining = null,
        listPosition = 1,
        isInitialCategory = false,
        linkedAccountId = null,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    ),
    Category(
        id = 2L,
        groupId = 1L,
        emoji = "🚗",
        name = "Transport",
        budgetTarget = Money(200.0),
        monthlyTargetAmount = null,
        targetMonthsRemaining = null,
        listPosition = 2,
        isInitialCategory = false,
        linkedAccountId = null,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )
)

// Previews
@Preview(name = "Outflow - Type Step", showBackground = true)
@Composable
private fun OutflowTypeStepPreview() {
    GetALifeTheme {
        OutflowTypeStep(
            selectedDirection = TransactionDirection.Outflow,
            availableAccounts = previewAccounts,
            onTypeSelected = { }
        )
    }
}

@Preview(name = "Outflow - Amount Step", showBackground = true)
@Composable
private fun OutflowAmountStepPreview() {
    GetALifeTheme {
        OutflowAmountStep(
            amountText = "45.99",
            onAmountTextChanged = { },
            onAmountChanged = { },
            onNextClicked = { }
        )
    }
}

@Preview(name = "Outflow - Account Step", showBackground = true)
@Composable
private fun OutflowAccountStepPreview() {
    GetALifeTheme {
        OutflowAccountStep(
            accounts = previewAccounts,
            selectedAccount = previewAccounts[1], // Kreditkarte
            onAccountSelected = { },
            onCreateAccountClicked = { }
        )
    }
}

@Preview(name = "Outflow - Partner Step", showBackground = true)
@Composable
private fun OutflowPartnerStepPreview() {
    GetALifeTheme {
        OutflowPartnerStep(
            currentPartner = "Edeka",
            onPartnerChanged = { },
            onNextClicked = { }
        )
    }
}

@Preview(name = "Outflow - Category Step", showBackground = true)
@Composable
private fun OutflowCategoryStepPreview() {
    GetALifeTheme {
        OutflowCategoryStep(
            categories = previewCategories,
            selectedCategory = previewCategories[0],
            onCategorySelected = { },
            onCreateCategoryClicked = { }
        )
    }
}

@Preview(name = "Outflow - Completion Step", showBackground = true)
@Composable
private fun OutflowCompletionStepPreview() {
    GetALifeTheme {
        OutflowCompletionStep(
            onSwitchToStandardMode = { }
        )
    }
}