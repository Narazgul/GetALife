package app.tinygiants.getalife.presentation.budget.composables

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.tinygiants.getalife.R
import app.tinygiants.getalife.presentation.budget.UserClickEvent
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHeaderBottomSheet(
    onUserClickEvent: (UserClickEvent) -> Unit,
    onDismissRequest: () -> Unit = { },
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var headerName by rememberSaveable { mutableStateOf("") }

    val onNameChanged = { newName: String -> headerName = newName }
    val onAddHeaderClicked = { onUserClickEvent(UserClickEvent.AddHeader(name = headerName)) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(spacing.medium)
        ) {
            OutlinedTextField(
                value = headerName,
                onValueChange = { newName -> onNameChanged(newName) },
                label = { Text(stringResource(R.string.enter_groupname)) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(spacing.default))
            Button(
                onClick = {
                    if (headerName.isBlank()) {
                        Toast.makeText(context, context.getString(R.string.no_name_entered), Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    onAddHeaderClicked()
                    onNameChanged("")
                    focusManager.clearFocus()
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
        Spacer(modifier = Modifier.height(spacing.extraLarge))
    }
}

@PreviewLightDark
@Composable
fun AddHeaderItemPreview() {
    GetALifeTheme {
        Surface {
            AddHeaderBottomSheet( onUserClickEvent = { } )
        }
    }
}