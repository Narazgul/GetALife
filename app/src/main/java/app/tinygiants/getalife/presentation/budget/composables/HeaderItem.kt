package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.presentation.budget.Money
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.LightAndDarkPreviews
import app.tinygiants.getalife.theme.spacing

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoryHeader(
    name: String = "",
    sumOfAvailableMoney: Money = Money(value = 0.00),
    isExpanded: Boolean = false,
    onHeaderClicked: () -> Unit = { },
    onUpdateHeaderNameClicked: (String) -> Unit = { },
    onDeleteHeaderClicked: () -> Unit = { },
    onAddCategoryClicked: (String) -> Unit = { },
) {

    var showBottomSheet by remember { mutableStateOf(false) }

    var headerNameUserInput by rememberSaveable { mutableStateOf(name) }
    var categoryNameUserInput by rememberSaveable { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onHeaderClicked,
                onLongClick = { showBottomSheet = true }
            )
            .background(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(size = spacing.small)
            )
            .padding(horizontal = spacing.large),
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = spacing.default)
        )
        Spacer(modifier = Modifier.weight(1f))
        val alignment = if (isExpanded) Alignment.CenterHorizontally else Alignment.End
        Column(horizontalAlignment = alignment) {
            Text(text = "Available")
            val text = if (isExpanded) "to spend" else sumOfAvailableMoney.formattedMoney
            val style = if (!isExpanded) MaterialTheme.typography.titleMedium else LocalTextStyle.current
            Text(text = text, style = style)
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
            Column(
                modifier = Modifier.padding(horizontal = spacing.large)
            ) {
                Row {
                    TextField(
                        value = categoryNameUserInput,
                        onValueChange = { userInput -> categoryNameUserInput = userInput },
                        label = { Text("Neue Kategorie hinzufügen") },
                        modifier = Modifier
                            .weight(1f)
                            .background(color = MaterialTheme.colorScheme.background)
                    )
                    Spacer(modifier = Modifier.width(spacing.default))
                    Button(
                        onClick = {
                            if (categoryNameUserInput.isNotBlank()) {
                                onAddCategoryClicked(categoryNameUserInput)
                                categoryNameUserInput = ""
                                showBottomSheet = false
                            }
                        }
                    ) { Text(text = "Speichern") }
                }
                Spacer(modifier = Modifier.height(spacing.default))
                Row {
                    TextField(
                        value = headerNameUserInput,
                        onValueChange = { userInput -> headerNameUserInput = userInput },
                        label = { Text("Titel ändern") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(spacing.default))
                    Button(
                        onClick = {
                            if (headerNameUserInput.isNotBlank()) {
                                onUpdateHeaderNameClicked(headerNameUserInput)
                                headerNameUserInput = ""
                                showBottomSheet = false
                            }
                        }
                    ) { Text(text = "Speichern") }
                }
                Spacer(modifier = Modifier.height(spacing.default))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onDeleteHeaderClicked,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            text = "$name-Gruppe löschen",
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(spacing.extraLarge))
        }
    }
}

@LightAndDarkPreviews
@Composable
fun CategoryHeaderPreview() {
    GetALifeTheme {
        Surface {
            CategoryHeader(
                name = "Quality of Life",
                sumOfAvailableMoney = Money(value = 12345.67)
            )
        }
    }
}

@LightAndDarkPreviews
@Composable
fun CategoryHeaderExpandedPreview() {
    GetALifeTheme {
        Surface {
            CategoryHeader(
                name = "Quality of Life",
                sumOfAvailableMoney = Money(value = 100.00),
                isExpanded = true
            )
        }
    }
}