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
import app.tinygiants.getalife.domain.model.onboarding.*
import app.tinygiants.getalife.presentation.onboarding.components.OnboardingTopAppBar
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.delay

// Custom emoji mapping for daily and leisure activities
private fun getEmojiForDaily(expense: DailyExpense): String = when (expense) {
    DailyExpense.GROCERIES -> "🛒"
    DailyExpense.TAKEAWAY -> "🍕"
    DailyExpense.COFFEE_SNACKS -> "☕"
}

private fun getEmojiForHealth(expense: HealthExpense): String = when (expense) {
    HealthExpense.GYM_SPORTS -> "💪"
    HealthExpense.PHARMACY -> "💊"
    HealthExpense.DRUGSTORE -> "🧴"
}

private fun getEmojiForShopping(expense: ShoppingExpense): String = when (expense) {
    ShoppingExpense.CLOTHING_SHOES -> "👕"
    ShoppingExpense.TECH_ELECTRONICS -> "📱"
    ShoppingExpense.BOOKS_MEDIA -> "📚"
}

private fun getEmojiForLeisure(expense: LeisureExpense): String = when (expense) {
    LeisureExpense.STREAMING -> "📺"
    LeisureExpense.EVENTS -> "🎭"
    LeisureExpense.GAMING -> "🎮"
    LeisureExpense.HOBBIES -> "🎨"
    LeisureExpense.TRAVEL -> "✈️"
}

// Consistent color scheme for lifestyle categories
private fun getColorForLifestyleCategory(category: String): Color = when (category) {
    "daily" -> Color(0xFF4CAF50)
    "health" -> Color(0xFF2196F3)
    "shopping" -> Color(0xFF9C27B0)
    "leisure" -> Color(0xFFFF9800)
    else -> Color(0xFF607D8B)
}

@Composable
fun LifestyleSectionHeader(
    title: String,
    emoji: String,
    category: String,
    modifier: Modifier = Modifier
) {
    val color = getColorForLifestyleCategory(category)

    val floatOffset by animateFloatAsState(
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2300),
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
                .background(color.copy(alpha = 0.12f)),
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
fun LifestyleCheckboxOption(
    text: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    category: String,
    index: Int,
    modifier: Modifier = Modifier
) {
    val color = getColorForLifestyleCategory(category)

    // Staggered entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * 100).toLong())
        isVisible = true
    }

    val cardScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ), label = "card_entrance"
    )

    val selectedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "selected_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .scale(cardScale * selectedScale)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) color else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(
                            listOf(
                                color.copy(alpha = 0.1f),
                                color.copy(alpha = 0.05f)
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fancy checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier.size(26.dp),
                enabled = false,
                colors = CheckboxDefaults.colors(
                    checkedColor = color,
                    uncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            // Animated emoji
            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier.scale(
                    animateFloatAsState(
                        targetValue = if (isSelected) 1.2f else 1f,
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
fun LifestyleHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎯",
            fontSize = 48.sp,
            modifier = Modifier
                .scale(
                    animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2100),
                            repeatMode = RepeatMode.Reverse
                        ), label = "target_scale"
                    ).value
                )
                .padding(bottom = 8.dp)
        )

        Text(
            text = "Werfen wir einen Blick auf deinen Alltag.",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.s))

        Text(
            text = "Welche Ausgaben fallen bei dir regelmäßig an?",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun LifestyleContinueButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(
                animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1100),
                        repeatMode = RepeatMode.Reverse
                    ), label = "button_pulse"
                ).value
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🎯", fontSize = 20.sp)
            Text(
                text = "Fast geschafft!",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text("🚀", fontSize = 20.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step6DailyAndLeisure(
    navController: NavController,
    onNextClicked: (List<DailyExpense>, List<HealthExpense>, List<ShoppingExpense>, List<LeisureExpense>) -> Unit,
    modifier: Modifier = Modifier
) {
    var daily by remember { mutableStateOf<List<DailyExpense>>(emptyList()) }
    var health by remember { mutableStateOf<List<HealthExpense>>(emptyList()) }
    var shopping by remember { mutableStateOf<List<ShoppingExpense>>(emptyList()) }
    var leisure by remember { mutableStateOf<List<LeisureExpense>>(emptyList()) }

    Scaffold(
        topBar = { OnboardingTopAppBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFE8F5E8),
                            Color(0xFFE3F2FD),
                            Color(0xFFF3E5F5),
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
                LifestyleHeader()

                Spacer(modifier = Modifier.height(spacing.xl))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Daily Expenses Section
                    LifestyleSectionHeader(
                        title = "Essen & Trinken",
                        emoji = "🍽️",
                        category = "daily"
                    )

                    DailyExpense.entries.forEachIndexed { index, expense ->
                        LifestyleCheckboxOption(
                            text = expense.displayName,
                            emoji = getEmojiForDaily(expense),
                            isSelected = expense in daily,
                            onClick = {
                                daily = if (expense in daily) daily - expense else daily + expense
                            },
                            category = "daily",
                            index = index
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.l))

                    // Health Expenses Section
                    LifestyleSectionHeader(
                        title = "Gesundheit & Wohlbefinden",
                        emoji = "💪",
                        category = "health"
                    )

                    HealthExpense.entries.forEachIndexed { index, expense ->
                        LifestyleCheckboxOption(
                            text = expense.displayName,
                            emoji = getEmojiForHealth(expense),
                            isSelected = expense in health,
                            onClick = {
                                health = if (expense in health) health - expense else health + expense
                            },
                            category = "health",
                            index = index
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.l))

                    // Shopping Expenses Section
                    LifestyleSectionHeader(
                        title = "Shopping & Kleidung",
                        emoji = "🛍️",
                        category = "shopping"
                    )

                    ShoppingExpense.entries.forEachIndexed { index, expense ->
                        LifestyleCheckboxOption(
                            text = expense.displayName,
                            emoji = getEmojiForShopping(expense),
                            isSelected = expense in shopping,
                            onClick = {
                                shopping = if (expense in shopping) shopping - expense else shopping + expense
                            },
                            category = "shopping",
                            index = index
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.l))

                    // Leisure Expenses Section
                    LifestyleSectionHeader(
                        title = "Freizeit & Hobbies",
                        emoji = "🎮",
                        category = "leisure"
                    )

                    LeisureExpense.entries.forEachIndexed { index, expense ->
                        LifestyleCheckboxOption(
                            text = expense.displayName,
                            emoji = getEmojiForLeisure(expense),
                            isSelected = expense in leisure,
                            onClick = {
                                leisure = if (expense in leisure) leisure - expense else leisure + expense
                            },
                            category = "leisure",
                            index = index
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.l))

                // Continue Button
                LifestyleContinueButton(
                    onClick = { onNextClicked(daily, health, shopping, leisure) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Step6DailyAndLeisurePreview() {
    MaterialTheme {
        val navController = rememberNavController()
        Step6DailyAndLeisure(navController = navController, onNextClicked = { _, _, _, _ -> })
    }
}