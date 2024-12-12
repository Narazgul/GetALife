package app.tinygiants.getalife.presentation.budget.composables.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.tinygiants.getalife.R
import app.tinygiants.getalife.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupBottomSheet(
    groupName: String,
    onAddCategoryClicked: (String) -> Unit,
    onUpdateGroupNameClicked: (String) -> Unit,
    onDeleteGroupClicked: () -> Unit,
    onDismissRequest: () -> Unit
) {

    var groupNameUserInput by rememberSaveable { mutableStateOf(groupName) }
    var categoryNameUserInput by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
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
                            onDismissRequest()
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
                            onDismissRequest()
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
                    onClick = {
                        onDeleteGroupClicked()
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = stringResource(R.string.delete_group, groupName),
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(spacing.xl))
    }
}