package app.tinygiants.getalife.presentation.main_app.budget.composables.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TargetType
import app.tinygiants.getalife.theme.spacing
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryBottomSheet(
    categoryName: String,
    budgetTarget: Money,
    monthlyTarget: Money?,
    targetType: TargetType = TargetType.NONE,
    targetAmount: Money? = null,
    targetDate: LocalDate? = null,
    onUpdateCategoryName: (String) -> Unit = { },
    onBudgetTargetChanged: (Money) -> Unit = { },
    onMonthlyTargetChanged: (Money?) -> Unit = { },
    onTargetTypeChanged: (TargetType) -> Unit = {},
    onTargetAmountChanged: (Money?) -> Unit = {},
    onTargetDateChanged: (LocalDate?) -> Unit = {},
    onDeleteCategoryClicked: () -> Unit = { },
    onDismissRequest: () -> Unit = { }
) {
    var categoryNameUserInput by rememberSaveable { mutableStateOf(categoryName) }
    var budgetTargetMoney by remember { mutableStateOf(budgetTarget) }

    // Target System States - FIX: Use rememberSaveable and sync with incoming parameter
    var currentTargetType by rememberSaveable { mutableStateOf(targetType) }
    var targetAmountInput by rememberSaveable { mutableStateOf(targetAmount?.formattedPositiveMoney ?: "") }
    var selectedDate by rememberSaveable { mutableStateOf(targetDate) }
    var isRepeating by rememberSaveable { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Validation
    var amountError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // FIX: Sync state with incoming parameters when they change
    androidx.compose.runtime.LaunchedEffect(targetType, targetAmount, targetDate) {
        android.util.Log.d(
            "EditCategoryBottomSheet",
            "Received params: targetType=$targetType, targetAmount=$targetAmount, targetDate=$targetDate"
        )
        android.util.Log.d("EditCategoryBottomSheet", "Current state: currentTargetType=$currentTargetType")

        if (currentTargetType != targetType) {
            android.util.Log.d("EditCategoryBottomSheet", "Updating targetType from $currentTargetType to $targetType")
            currentTargetType = targetType
        }
        if (selectedDate != targetDate) {
            android.util.Log.d("EditCategoryBottomSheet", "Updating targetDate from $selectedDate to $targetDate")
            selectedDate = targetDate
        }
        if (targetAmount?.formattedPositiveMoney != targetAmountInput && targetAmount != null) {
            android.util.Log.d(
                "EditCategoryBottomSheet",
                "Updating targetAmount from '$targetAmountInput' to '${targetAmount.formattedPositiveMoney}'"
            )
            targetAmountInput = targetAmount.formattedPositiveMoney
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = spacing.xs),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(spacing.xs)
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.l, vertical = spacing.m)
        ) {
            // Header
            Text(
                text = "Kategorie bearbeiten",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(spacing.l))

            // Category Name
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(spacing.l)
            ) {
                TextField(
                    value = categoryNameUserInput,
                    onValueChange = { userInput ->
                        categoryNameUserInput = userInput
                        onUpdateCategoryName(userInput)
                    },
                    label = { Text("Kategorien Name") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(spacing.m)
                )
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // Target Type Selection
            Text(
                text = "Zieltyp",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(spacing.s))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(spacing.l)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDropdown = true }
                        .padding(spacing.m),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentTargetType) {
                            TargetType.NONE -> "Kein Ziel"
                            TargetType.NEEDED_FOR_SPENDING -> "Ausgaben-Ziel"
                            TargetType.SAVINGS_BALANCE -> "Sparziel"
                        },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                    
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Kein Ziel") },
                            onClick = {
                                currentTargetType = TargetType.NONE
                                onTargetTypeChanged(TargetType.NONE)
                                showDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text("Ausgaben-Ziel", fontWeight = FontWeight.Medium)
                                    Text(
                                        "Monatlich benötigter Betrag",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                currentTargetType = TargetType.NEEDED_FOR_SPENDING
                                onTargetTypeChanged(TargetType.NEEDED_FOR_SPENDING)
                                showDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text("Sparziel", fontWeight = FontWeight.Medium)
                                    Text(
                                        "Auf einen Betrag ansparen",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                currentTargetType = TargetType.SAVINGS_BALANCE
                                onTargetTypeChanged(TargetType.SAVINGS_BALANCE)
                                showDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // Target Amount (shown for both target types)
            if (currentTargetType != TargetType.NONE) {
                Text(
                    text = when (currentTargetType) {
                        TargetType.NEEDED_FOR_SPENDING -> "Monatlich benötigt"
                        TargetType.SAVINGS_BALANCE -> "Zielbetrag"
                        TargetType.NONE -> ""
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(spacing.s))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(spacing.l)
                ) {
                    Column {
                        TextField(
                            value = targetAmountInput,
                            onValueChange = { input ->
                                targetAmountInput = input
                                val validationResult = TargetValidation.validateAmount(input)
                                if (input.isBlank()) {
                                    amountError = null
                                    onTargetAmountChanged(null)
                                } else if (validationResult.errorMessage != null) {
                                    amountError = validationResult.errorMessage
                                } else {
                                    amountError = null
                                    onTargetAmountChanged(Money(validationResult.value!!))
                                }
                            },
                            label = { Text("Betrag in €") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(spacing.m),
                            isError = amountError != null
                        )
                        
                        if (amountError != null) {
                            Text(
                                text = amountError!!,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = spacing.m, vertical = spacing.xs)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.l))
            }

            // Target Date (only for SAVINGS_BALANCE)
            if (currentTargetType == TargetType.SAVINGS_BALANCE) {
                Text(
                    text = "Zieldatum (optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(spacing.s))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(spacing.l)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .padding(spacing.m),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(spacing.s))
                        
                        Text(
                            text = selectedDate?.toString() ?: "Kein Datum ausgewählt",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedDate != null) 
                                MaterialTheme.colorScheme.onSurface 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (selectedDate != null) {
                            TextButton(
                                onClick = { 
                                    selectedDate = null
                                    onTargetDateChanged(null)
                                }
                            ) {
                                Text("Entfernen")
                            }
                        }
                    }
                }

                // Repeating Checkbox (only if date is set)
                if (selectedDate != null) {
                    Spacer(modifier = Modifier.height(spacing.m))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isRepeating = !isRepeating }
                            .padding(spacing.s),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isRepeating,
                            onCheckedChange = { isRepeating = it }
                        )
                        
                        Spacer(modifier = Modifier.width(spacing.s))
                        
                        Column {
                            Text(
                                text = "Ziel wiederholt sich jährlich",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Nach Erreichen wird das Ziel für das nächste Jahr zurückgesetzt",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.l))
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // Delete Button
            Button(
                onClick = {
                    onDeleteCategoryClicked()
                    onDismissRequest()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(spacing.l)
            ) {
                Text(
                    text = "Kategorie löschen",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val javaDate = java.time.LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            val kotlinDate = javaDate.toKotlinLocalDate()
                            selectedDate = kotlinDate
                            onTargetDateChanged(kotlinDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}