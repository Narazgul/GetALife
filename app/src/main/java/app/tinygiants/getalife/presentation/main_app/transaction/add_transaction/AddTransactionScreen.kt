package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.composables.AddTransactionItem
import app.tinygiants.getalife.presentation.main_app.transaction.composables.Description
import app.tinygiants.getalife.presentation.main_app.transaction.composables.TransactionPartner
import app.tinygiants.getalife.presentation.main_app.transaction.composables.waveAnimationBackground
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.onSuccess
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.launch

@Composable
fun AddTransaction() {
    val viewModel: AddTransactionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val partnerSuggestions by viewModel.partners.collectAsStateWithLifecycle()

    val onAddTransactionClicked =
        { amount: Money,
          account: Account,
          category: Category?,
          direction: TransactionDirection,
          description: Description,
          transactionPartner: TransactionPartner,
          dateOfTransaction: kotlin.time.Instant,
          recurrenceFrequency: RecurrenceFrequency? ->

            viewModel.onSaveTransactionClicked(
                amount = amount,
                direction = direction,
                accountId = account.id,
                description = description,
                transactionPartner = transactionPartner,
                category = category,
                dateOfTransaction = dateOfTransaction,
                recurrenceFrequency = recurrenceFrequency
            )
        }

    AddTransactionContent(
        categories = uiState.categories,
        accounts = uiState.accounts,
        partnerSuggestions = partnerSuggestions,
        onAddTransactionClicked = onAddTransactionClicked
    )
}

@Composable
fun AddTransactionContent(
    categories: List<Category>,
    accounts: List<Account>,
    partnerSuggestions: List<String>,
    onAddTransactionClicked: (amount: Money, account: Account, category: Category?, direction: TransactionDirection, description: Description, transactionPartner: TransactionPartner, dateOfTransaction: kotlin.time.Instant, recurrenceFrequency: RecurrenceFrequency?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val neutralBackground = MaterialTheme.colorScheme.primary.toArgb()
    val inflowBackground = onSuccess.toArgb()
    val outflowBackground = MaterialTheme.colorScheme.errorContainer.toArgb()
    var waveColor by remember { mutableIntStateOf(neutralBackground) }

    val transactionSavedString = stringResource(id = R.string.transaction_saved)
    val showTransactionAddedSnackbar = { scope.launch { snackbarHostState.showSnackbar(transactionSavedString) } }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(450.dp)
                    .waveAnimationBackground(color = waveColor)
            )

            AddTransactionItem(
                categories = categories,
                accounts = accounts,
                partnerSuggestions = partnerSuggestions,
                onTransactionDirectionClicked = { transactionDirection ->
                    waveColor = when (transactionDirection) {
                        TransactionDirection.Inflow -> inflowBackground
                        TransactionDirection.Outflow -> outflowBackground
                        else -> neutralBackground
                    }
                },
                onAddTransactionClicked = { amount, account, category, direction, description, transactionPartner, dateOfTransaction, recurrenceFrequency ->
                    onAddTransactionClicked(
                        amount,
                        account,
                        category,
                        direction,
                        description,
                        transactionPartner,
                        dateOfTransaction,
                        recurrenceFrequency
                    )
                    showTransactionAddedSnackbar()
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(spacing.m)
            )
        }
    }
}

@Preview
@Composable
private fun AddTransactionPreview() {
    GetALifeTheme {
        Surface {
            AddTransactionContent(
                categories = emptyList(),
                accounts = emptyList(),
                partnerSuggestions = emptyList(),
                onAddTransactionClicked = { _, _, _, _, _, _, _, _ -> }
            )
        }
    }
}