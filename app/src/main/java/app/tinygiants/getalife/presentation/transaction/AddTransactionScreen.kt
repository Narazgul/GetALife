package app.tinygiants.getalife.presentation.transaction

import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.transaction.composables.AddTransactionItem
import app.tinygiants.getalife.presentation.transaction.composables.Description
import app.tinygiants.getalife.presentation.transaction.composables.TransactionPartner
import app.tinygiants.getalife.presentation.transaction.composables.waveAnimationBackground
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.launch

@Composable
fun AddTransaction() {
    val viewModel: AddTransactionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val onAddTransactionClicked =
        { amount: Money,
          account: Account?,
          category: Category?,
          direction: TransactionDirection,
          description: Description?,
          transactionPartner: TransactionPartner? ->

            viewModel.onSaveTransactionClicked(
                amount = amount,
                direction = direction,
                accountId = account?.id,
                description = description,
                transactionPartner = transactionPartner,
                category = category
            )
        }

    AddTransaction(
        categories = uiState.categories,
        accounts = uiState.accounts,
        onAddTransactionClicked = onAddTransactionClicked
    )
}

@Composable
fun AddTransaction(
    categories: List<Category>,
    accounts: List<Account>,
    onAddTransactionClicked: (amount: Money, account: Account?, category: Category?, direction: TransactionDirection, description: Description, transactionPartner: TransactionPartner) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val transactionSavedString = stringResource(id = R.string.transaction_saved)
    val showTransactionAddedSnackbar = {
        scope.launch { snackbarHostState.showSnackbar(transactionSavedString) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(450.dp)
                    .waveAnimationBackground(color = MaterialTheme.colorScheme.primary.toArgb())
            )

            AddTransactionItem(
                categories = categories,
                accounts = accounts,
                onAddTransactionClicked = { amount, account, category, direction, description, transactionPartner ->

                    onAddTransactionClicked(amount, account, category, direction, description, transactionPartner)
                    showTransactionAddedSnackbar()
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(spacing.default)
            )
        }
    }
}

@Preview
@Composable
private fun AddTransactionPreview() {
    GetALifeTheme {
        Surface {
            AddTransaction(
                categories = emptyList(),
                accounts = emptyList(),
                onAddTransactionClicked = { _, _, _, _, _, _ -> }
            )
        }
    }
}