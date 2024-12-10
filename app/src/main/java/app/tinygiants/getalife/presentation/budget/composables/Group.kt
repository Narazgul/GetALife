package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.res.stringResource
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.ComponentPreview
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Group(
    name: String = "",
    sumOfAvailableMoney: Money = Money(value = 0.00),
    isExpanded: Boolean = false,
    onGroupClicked: () -> Unit = { },
    onUpdateGroupNameClicked: (String) -> Unit = { },
    onDeleteGroupClicked: () -> Unit = { },
    onAddCategoryClicked: (String) -> Unit = { },
) {

    var showBottomSheet by remember { mutableStateOf(false) }

    var groupNameUserInput by rememberSaveable { mutableStateOf(name) }
    var categoryNameUserInput by rememberSaveable { mutableStateOf("") }

    val availableText = stringResource(R.string.available)
    val forSpendingText = stringResource(R.string.to_spend)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onGroupClicked,
                onLongClick = { showBottomSheet = true }
            )
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(size = spacing.s)
            )
            .padding(horizontal = spacing.l),
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = null
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = spacing.m)
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.clickable { showBottomSheet = true }
        )
        Spacer(modifier = Modifier.width(spacing.l))
        val alignment = if (isExpanded) Alignment.CenterHorizontally else Alignment.End
        Column(horizontalAlignment = alignment) {
            Text(text = availableText, color = MaterialTheme.colorScheme.onPrimaryContainer)
            val text = if (isExpanded) forSpendingText else sumOfAvailableMoney.formattedMoney
            val style = if (!isExpanded) MaterialTheme.typography.titleMedium else LocalTextStyle.current
            Text(text = text, style = style, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
            Column(
                modifier = Modifier.padding(horizontal = spacing.l)
            ) {
                Row {
                    TextField(
                        value = categoryNameUserInput,
                        onValueChange = { userInput -> categoryNameUserInput = userInput },
                        label = { Text(stringResource(R.string.add_category)) },
                        modifier = Modifier
                            .weight(1f)
                            .background(color = MaterialTheme.colorScheme.background)
                    )
                    Spacer(modifier = Modifier.width(spacing.m))
                    Button(
                        onClick = {
                            if (categoryNameUserInput.isNotBlank()) {
                                onAddCategoryClicked(categoryNameUserInput)
                                categoryNameUserInput = ""
                                showBottomSheet = false
                            }
                        }
                    ) { Text(text = stringResource(id = R.string.save)) }
                }
                Spacer(modifier = Modifier.height(spacing.m))
                Row {
                    TextField(
                        value = groupNameUserInput,
                        onValueChange = { userInput -> groupNameUserInput = userInput },
                        label = { Text(stringResource(R.string.change_title)) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(spacing.m))
                    Button(
                        onClick = {
                            if (groupNameUserInput.isNotBlank()) {
                                onUpdateGroupNameClicked(groupNameUserInput)
                                groupNameUserInput = ""
                                showBottomSheet = false
                            }
                        }
                    ) { Text(text = stringResource(id = R.string.save)) }
                }
                Spacer(modifier = Modifier.height(spacing.m))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onDeleteGroupClicked,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            text = stringResource(R.string.delete_group, name),
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(spacing.xl))
        }
    }
}

@ComponentPreview
@Composable
fun CategoryGroupPreview() {
    GetALifeTheme {
        Surface {
            Group(
                name = "Quality of Life",
                sumOfAvailableMoney = Money(value = 12345.67)
            )
        }
    }
}

@ComponentPreview
@Composable
fun CategoryGroupExpandedPreview() {
    GetALifeTheme {
        Surface {
            Group(
                name = "Quality of Life",
                sumOfAvailableMoney = Money(value = 100.00),
                isExpanded = true
            )
        }
    }
}