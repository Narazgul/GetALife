package app.tinygiants.getalife.presentation.budget.composables.group

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Group(
    groupName: String = "",
    sumOfAvailableMoney: Money = Money(value = 0.00),
    isExpanded: Boolean = false,
    onGroupClicked: () -> Unit = { },
    onUpdateGroupNameClicked: (String) -> Unit = { },
    onDeleteGroupClicked: () -> Unit = { },
    onAddCategoryClicked: (String) -> Unit = { },
) {

    var showBottomSheet by remember { mutableStateOf(false) }

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
            text = groupName,
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
        Column(horizontalAlignment = Alignment.End) {
            Text(text = stringResource(R.string.available), color = MaterialTheme.colorScheme.onPrimaryContainer)
            val text = if (isExpanded) stringResource(R.string.to_spend) else sumOfAvailableMoney.formattedMoney
            val style = if (!isExpanded) MaterialTheme.typography.titleMedium else LocalTextStyle.current
            Text(text = text, style = style, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }

    if (showBottomSheet) EditGroupBottomSheet(
        groupName = groupName,
        onAddCategoryClicked = onAddCategoryClicked,
        onUpdateGroupNameClicked = onUpdateGroupNameClicked,
        onDeleteGroupClicked = onDeleteGroupClicked,
        onDismissRequest = { showBottomSheet = false }
    )
}

@PreviewLightDark
@Composable
fun CategoryGroupPreview() {
    GetALifeTheme {
        Surface {
            Group(
                groupName = "Quality of Life",
                sumOfAvailableMoney = Money(value = 12345.67)
            )
        }
    }
}

@PreviewLightDark
@Composable
fun CategoryGroupExpandedPreview() {
    GetALifeTheme {
        Surface {
            Group(
                groupName = "Quality of Life",
                sumOfAvailableMoney = Money(value = 100.00),
                isExpanded = true
            )
        }
    }
}