package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import app.tinygiants.getalife.domain.model.onbaordinganswers.Debt
import app.tinygiants.getalife.domain.model.onboarding.FinancialDependant
import app.tinygiants.getalife.domain.model.onboarding.InsuranceType
import app.tinygiants.getalife.presentation.onboarding.components.OnboardingTopAppBar
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.delay

// Custom emoji mapping for commitments
private fun getEmojiForDependant(dependant: FinancialDependant): String = when (dependant) {
    FinancialDependant.ONLY_MYSELF -> "🙋‍♀️"
    FinancialDependant.PARTNER -> "💑"
    FinancialDependant.CHILDREN -> "👶"
}

private fun getEmojiForInsurance(insurance: InsuranceType): String = when (insurance) {
    InsuranceType.LIABILITY -> "🛡️"
    InsuranceType.HOUSEHOLD -> "🏠"
    InsuranceType.DISABILITY -> "⚡"
    InsuranceType.LEGAL -> "⚖️"
}

private fun getEmojiForDebt(debt: Debt): String = when (debt) {
    Debt.CreditCard -> "💳"
    Debt.StudentLoan -> "🎓"
    Debt.CarLoan -> "🚗"
    Debt.Mortgage -> "🏠"
    Debt.PersonalLoan -> "💰"
    Debt.NoDebt -> "✅"
}

// Consistent color scheme
private fun getColorForCommitmentCategory(category: String): Color = when (category) {
    "dependants" -> Color(0xFFE91E63)
    "insurance" -> Color(0xFF3F51B5)
    "debt" -> Color(0xFFFF5722)
    else -> Color(0xFF9C27B0)
}

@Composable
fun CommitmentSectionHeader(
    title: String,
    emoji: String,
    category: String,
    modifier: Modifier = Modifier
) {
    val color = getColorForCommitmentCategory(category)

    val floatOffset by animateFloatAsState(
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500),
            repeatMode = RepeatMode.Reverse
        ), label = "header_float"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.m)
            .offset(y = floatOffset.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

@Composable
fun CommitmentCheckboxOption(
    text: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    category: String,
    index: Int,
    modifier: Modifier = Modifier
) {
    val color = getColorForCommitmentCategory(category)

    // Staggered entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * 120).toLong())
        isVisible = true
    }

    val cardScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "card_entrance"
    )

    val selectedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "selected_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(cardScale * selectedScale)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) color else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 10.dp else 3.dp
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(
                            listOf(
                                color.copy(alpha = 0.12f),
                                color.copy(alpha = 0.06f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fancy checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier.size(28.dp),
                enabled = false,
                colors = CheckboxDefaults.colors(
                    checkedColor = color,
                    uncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            // Animated emoji
            Text(
                text = emoji,
                fontSize = 26.sp,
                modifier = Modifier.scale(
                    animateFloatAsState(
                        targetValue = if (isSelected) 1.3f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        ), label = "emoji_scale"
                    ).value
                )
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CommitmentsHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "👥",
            fontSize = 48.sp,
            modifier = Modifier
                .scale(
                    animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2200),
                            repeatMode = RepeatMode.Reverse
                        ), label = "people_scale"
                    ).value
                )
                .padding(bottom = 8.dp)
        )

        Text(
            text = "Wer oder was ist noch Teil deines Lebens?",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.s))

        Text(
            text = "Damit wir deine Verantwortungen und Absicherung berücksichtigen können.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun CommitmentsContinueButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(
                animateFloatAsState(
                    targetValue = if (enabled) 1f else 0.95f,
                    animationSpec = if (enabled) {
                        infiniteRepeatable(
                            animation = tween(1200),
                            repeatMode = RepeatMode.Reverse
                        )
                    } else {
                        tween(200)
                    }, label = "button_pulse"
                ).value
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (enabled) {
                Text("👥", fontSize = 20.sp)
            }
            Text(
                text = if (enabled) "Verpflichtungen notiert!" else "Wähle deine Situation",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            if (enabled) {
                Text("💪", fontSize = 20.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step5Commitments(
    navController: NavController,
    onNextClicked: (List<FinancialDependant>, List<InsuranceType>, List<Debt>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDependants by remember { mutableStateOf<List<FinancialDependant>>(emptyList()) }
    var selectedInsurances by remember { mutableStateOf<List<InsuranceType>>(emptyList()) }
    var selectedDebts by remember { mutableStateOf<List<Debt>>(emptyList()) }

    Scaffold(
        topBar = { OnboardingTopAppBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFCE4EC),
                            Color(0xFFE8EAF6),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.m, vertical = spacing.l)
            ) {
                // Header
                CommitmentsHeader()

                Spacer(modifier = Modifier.height(spacing.xl))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Financial Dependants Section
                    CommitmentSectionHeader(
                        title = "Für wen sorgst du finanziell?",
                        emoji = "👥",
                        category = "dependants"
                    )

                    FinancialDependant.entries.forEachIndexed { index, dependant ->
                        CommitmentCheckboxOption(
                            text = dependant.displayName,
                            emoji = getEmojiForDependant(dependant),
                            isSelected = dependant in selectedDependants,
                            onClick = {
                                selectedDependants = if (dependant == FinancialDependant.ONLY_MYSELF) {
                                    if (dependant in selectedDependants) emptyList() else listOf(FinancialDependant.ONLY_MYSELF)
                                } else {
                                    if (dependant in selectedDependants) {
                                        selectedDependants - dependant
                                    } else {
                                        (selectedDependants - FinancialDependant.ONLY_MYSELF) + dependant
                                    }
                                }
                            },
                            category = "dependants",
                            index = index
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.l))

                    // Insurance Section
                    CommitmentSectionHeader(
                        title = "Welche Versicherungen sind für dich wichtig?",
                        emoji = "🛡️",
                        category = "insurance"
                    )

                    InsuranceType.entries.forEachIndexed { index, insurance ->
                        CommitmentCheckboxOption(
                            text = insurance.displayName,
                            emoji = getEmojiForInsurance(insurance),
                            isSelected = insurance in selectedInsurances,
                            onClick = {
                                selectedInsurances = if (insurance in selectedInsurances) {
                                    selectedInsurances - insurance
                                } else {
                                    selectedInsurances + insurance
                                }
                            },
                            category = "insurance",
                            index = index
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.l))

                    // Debt Section
                    CommitmentSectionHeader(
                        title = "Zahlst du aktiv Kredite ab?",
                        emoji = "💳",
                        category = "debt"
                    )

                    Debt.entries.forEachIndexed { index, debt ->
                        if (debt.displayName.isNotBlank()) {
                            CommitmentCheckboxOption(
                                text = debt.displayName,
                                emoji = getEmojiForDebt(debt),
                                isSelected = debt in selectedDebts,
                                onClick = {
                                    selectedDebts = if (debt in selectedDebts) {
                                        selectedDebts - debt
                                    } else {
                                        selectedDebts + debt
                                    }
                                },
                                category = "debt",
                                index = index
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.l))

                // Continue Button
                CommitmentsContinueButton(
                    enabled = selectedDependants.isNotEmpty(),
                    onClick = { onNextClicked(selectedDependants, selectedInsurances, selectedDebts) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Step5CommitmentsPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        Step5Commitments(navController = navController, onNextClicked = { _, _, _ -> })
    }
}