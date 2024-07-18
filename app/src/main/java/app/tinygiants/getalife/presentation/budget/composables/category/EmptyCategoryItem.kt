package app.tinygiants.getalife.presentation.budget.composables.category

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import app.tinygiants.getalife.R
import app.tinygiants.getalife.theme.ComponentPreview
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

@Composable
fun EmptyCategoryItem(
    onUpdateNameClicked: (String) -> Unit = { },
) {
    val context = LocalContext.current
    var categoryName by rememberSaveable { mutableStateOf("") }

    Row(
        modifier = Modifier.padding(
            start = spacing.l,
            end = spacing.l,
            bottom = spacing.m
        )
    ) {
        OutlinedTextField(
            value = categoryName,
            onValueChange = { textInput -> categoryName = textInput },
            label = { Text(stringResource(id = R.string.add_category)) },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(spacing.default))
        Button(
            onClick = {
                if (categoryName.isBlank()) {
                    Toast.makeText(context, R.string.no_name_entered, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                onUpdateNameClicked(categoryName)
            },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text(text = "Save")
        }
    }
}

@ComponentPreview
@Composable
fun AddCategoryPreview() {
    GetALifeTheme {
        Surface {
            EmptyCategoryItem()
        }
    }
}