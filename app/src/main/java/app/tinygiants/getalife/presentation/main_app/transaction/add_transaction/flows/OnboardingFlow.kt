package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.flows

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransactionViewModel
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.TransactionInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.TransactionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.flows.composables.AccountSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.flows.composables.AmountInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.flows.composables.CategorySelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.flows.composables.CompletedStepsChecklist
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.flows.composables.CompletionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.flows.composables.DateSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.flows.composables.OnboardingStepCounter
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.flows.composables.TextInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.flows.composables.TransactionTypeSelector
import app.tinygiants.getalife.theme.spacing

@Composable
fun OnboardingFlow(
    currentStep: TransactionStep,
    transactionInput: TransactionInput,
    categories: List<Category>,
    accounts: List<Account>,
    viewModel: AddTransactionViewModel,
    onShowAddAccountDialog: () -> Unit,
    onShowAddCategoryDialog: () -> Unit,
    currentStepTitle: String,
    modifier: Modifier = Modifier
) {
    var amountText by rememberSaveable {
        mutableStateOf(transactionInput.amount?.asDouble()?.toString() ?: "")
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
        CompletedStepsChecklist(
            transactionInput = transactionInput,
            currentStep = currentStep,
            onStepClicked = viewModel::goToStep
        )

        OnboardingStepCounter(
            currentStep = currentStep,
            transactionInput = transactionInput
        )

        AnimatedContent(
            targetState = currentStep,
            label = "Guided Step Animation",
            transitionSpec = {
                slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { fullHeight -> fullHeight }
                ) + fadeIn(animationSpec = tween(300)) togetherWith
                        slideOutVertically(
                            animationSpec = tween(300),
                            targetOffsetY = { fullHeight -> -fullHeight }
                        ) + fadeOut(animationSpec = tween(300))
            }
        ) { step ->
            when (step) {
                TransactionStep.FlowSelection -> {
                    TransactionTypeSelector(
                        selectedDirection = transactionInput.direction,
                        availableAccounts = accounts,
                        onTypeSelected = viewModel::onTransactionDirectionSelected,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.Amount -> {
                    AmountInput(
                        amountText = amountText,
                        onAmountTextChanged = { amountText = it },
                        onAmountChanged = viewModel::onAmountChanged,
                        onNextClicked = viewModel::moveToNextStep,
                        title = when (transactionInput.direction) {
                            TransactionDirection.Inflow -> "Wie viel Geld hast du erhalten?"
                            TransactionDirection.Outflow -> "Wie viel Geld hast du ausgegeben?"
                            TransactionDirection.AccountTransfer -> "Wie viel Geld möchtest du transferieren?"
                            else -> "Gib den Betrag ein"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.FromAccount -> {
                    AccountSelector(
                        title = currentStepTitle,
                        accounts = accounts,
                        selectedAccount = transactionInput.fromAccount,
                        onAccountSelected = viewModel::onFromAccountSelected,
                        onCreateAccountClicked = onShowAddAccountDialog,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.ToAccount -> {
                    AccountSelector(
                        title = currentStepTitle,
                        accounts = accounts.filter { it.id != transactionInput.fromAccount?.id },
                        selectedAccount = transactionInput.toAccount,
                        onAccountSelected = viewModel::onToAccountSelected,
                        onCreateAccountClicked = onShowAddAccountDialog,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.Partner -> {
                    TextInput(
                        title = when (transactionInput.direction) {
                            TransactionDirection.Inflow -> "Von wem hast du das Geld erhalten?"
                            TransactionDirection.Outflow -> "Wo hast du das Geld ausgegeben?"
                            else -> "Partner eingeben"
                        },
                        value = transactionInput.partner,
                        onValueChange = viewModel::onPartnerChanged,
                        onNextClicked = viewModel::moveToNextStep,
                        placeholder = when (transactionInput.direction) {
                            TransactionDirection.Inflow -> "z.B. Arbeitgeber, Familie, Kunde"
                            TransactionDirection.Outflow -> "z.B. Supermarkt, Restaurant"
                            else -> "Name des Partners"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.Category -> {
                    CategorySelector(
                        title = "Für welche Kategorie?",
                        categories = categories,
                        selectedCategory = transactionInput.category,
                        onCategorySelected = viewModel::onCategorySelected,
                        onCreateCategoryClicked = onShowAddCategoryDialog,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.Date -> {
                    DateSelector(
                        title = "Wann war das?",
                        selectedDate = transactionInput.date,
                        showDatePicker = showDatePicker,
                        onDateSelected = viewModel::onDateSelected,
                        onShowDatePickerChanged = { showDatePicker = it },
                        onNextClicked = viewModel::moveToNextStep,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.Optional -> {
                    TextInput(
                        title = "Möchtest du eine Notiz hinzufügen?",
                        value = transactionInput.description,
                        onValueChange = viewModel::onDescriptionChanged,
                        onNextClicked = { viewModel.saveTransaction() },
                        placeholder = "Notiz (optional)",
                        nextButtonText = "✨ Transaktion speichern",
                        isRequired = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.Done -> {
                    CompletionStep(
                        onSwitchToStandardMode = viewModel::switchToStandardMode,
                        title = when (transactionInput.direction) {
                            TransactionDirection.Inflow -> "💰 Einnahme gespeichert!"
                            TransactionDirection.Outflow -> "💸 Ausgabe gespeichert!"
                            TransactionDirection.AccountTransfer -> "🔄 Transfer abgeschlossen!"
                            else -> "✅ Transaktion gespeichert!"
                        },
                        message = "Deine Transaktion wurde erfolgreich hinzugefügt.\nDein Kontostand wurde entsprechend aktualisiert.",
                        buttonText = "Weitere Transaktion hinzufügen",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}