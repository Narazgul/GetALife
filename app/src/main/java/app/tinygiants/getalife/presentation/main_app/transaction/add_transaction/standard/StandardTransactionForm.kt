package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.standard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransactionViewModel
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.TransactionInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.TransactionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.components.AccountSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.components.AmountInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.components.CategorySelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.components.DateSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.components.TextInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.components.TransactionTypeSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared.getStepTitle
import app.tinygiants.getalife.theme.spacing

@Composable
fun StandardTransactionForm(
    transactionInput: TransactionInput,
    categories: List<Category>,
    accounts: List<Account>,
    viewModel: AddTransactionViewModel,
    onShowAddAccountDialog: () -> Unit,
    onShowAddCategoryDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFlowSelected = transactionInput.direction != TransactionDirection.Unknown

    var amountText by rememberSaveable {
        mutableStateOf(transactionInput.amount?.asDouble()?.toString() ?: "")
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    AnimatedContent(
        targetState = isFlowSelected,
        label = "Standard Mode Flow Animation",
        transitionSpec = {
            slideInVertically(
                animationSpec = tween(400),
                initialOffsetY = { fullHeight -> if (targetState) -fullHeight else 0 }
            ) + fadeIn(animationSpec = tween(400)) togetherWith
                    slideOutVertically(
                        animationSpec = tween(400),
                        targetOffsetY = { fullHeight -> if (targetState) -fullHeight else fullHeight }
                    ) + fadeOut(animationSpec = tween(400))
        },
        modifier = modifier
    ) { flowSelected ->
        if (!flowSelected) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                TransactionTypeSelector(
                    selectedDirection = transactionInput.direction,
                    availableAccounts = accounts,
                    onTypeSelected = viewModel::onTransactionDirectionSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                // Transaction Type Selector (Compact version at top)
                TransactionTypeSelector(
                    selectedDirection = transactionInput.direction,
                    availableAccounts = accounts,
                    onTypeSelected = viewModel::onTransactionDirectionSelected,
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount Input
                AmountInput(
                    amountText = amountText,
                    onAmountTextChanged = { amountText = it },
                    onAmountChanged = viewModel::onAmountChanged,
                    onNextClicked = { },
                    title = when (transactionInput.direction) {
                        TransactionDirection.Inflow -> "Wie viel Geld hast du erhalten?"
                        TransactionDirection.Outflow -> "Wie viel Geld hast du ausgegeben?"
                        TransactionDirection.AccountTransfer -> "Wie viel Geld möchtest du transferieren?"
                        else -> "Gib den Betrag ein"
                    },
                    showNextButton = false,
                    modifier = Modifier.fillMaxWidth()
                )

                // From Account
                AccountSelector(
                    title = getStepTitle(TransactionStep.FromAccount, transactionInput),
                    accounts = accounts,
                    selectedAccount = transactionInput.fromAccount,
                    onAccountSelected = viewModel::onFromAccountSelected,
                    onCreateAccountClicked = onShowAddAccountDialog,
                    modifier = Modifier.fillMaxWidth()
                )

                // To Account (only for transfers)
                if (transactionInput.direction == TransactionDirection.AccountTransfer) {
                    AccountSelector(
                        title = getStepTitle(TransactionStep.ToAccount, transactionInput),
                        accounts = accounts.filter { it.id != transactionInput.fromAccount?.id },
                        selectedAccount = transactionInput.toAccount,
                        onAccountSelected = viewModel::onToAccountSelected,
                        onCreateAccountClicked = onShowAddAccountDialog,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Partner (not for transfers)
                if (transactionInput.direction != TransactionDirection.AccountTransfer) {
                    TextInput(
                        title = when (transactionInput.direction) {
                            TransactionDirection.Inflow -> "Von wem hast du das Geld erhalten?"
                            TransactionDirection.Outflow -> "Wo hast du das Geld ausgegeben?"
                            else -> "Partner eingeben"
                        },
                        value = transactionInput.partner,
                        onValueChange = viewModel::onPartnerChanged,
                        onNextClicked = { },
                        placeholder = when (transactionInput.direction) {
                            TransactionDirection.Inflow -> "z.B. Arbeitgeber, Familie, Kunde"
                            TransactionDirection.Outflow -> "z.B. Supermarkt, Restaurant"
                            else -> "Name des Partners"
                        },
                        showNextButton = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Category (only for outflow)
                if (transactionInput.direction == TransactionDirection.Outflow) {
                    CategorySelector(
                        title = "Für welche Kategorie?",
                        categories = categories,
                        selectedCategory = transactionInput.category,
                        onCategorySelected = viewModel::onCategorySelected,
                        onCreateCategoryClicked = onShowAddCategoryDialog,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Date
                DateSelector(
                    title = "Wann war das?",
                    selectedDate = transactionInput.date,
                    showDatePicker = showDatePicker,
                    onDateSelected = viewModel::onDateSelected,
                    onShowDatePickerChanged = { showDatePicker = it },
                    onNextClicked = { },
                    showNextButton = false,
                    modifier = Modifier.fillMaxWidth()
                )

                // Description (optional)
                TextInput(
                    title = "Möchtest du eine Notiz hinzufügen?",
                    value = transactionInput.description,
                    onValueChange = viewModel::onDescriptionChanged,
                    onNextClicked = { },
                    placeholder = "Notiz (optional)",
                    showNextButton = false,
                    isRequired = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(spacing.l))

                // Save Button
                Button(
                    onClick = { viewModel.saveTransaction() },
                    enabled = transactionInput.isValid(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Transaktion speichern",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(spacing.xl))
            }
        }
    }
}