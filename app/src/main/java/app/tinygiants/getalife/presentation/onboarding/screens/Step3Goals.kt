package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import app.tinygiants.getalife.domain.model.onboarding.SavingGoal
import app.tinygiants.getalife.presentation.onboarding.components.OnboardingTopAppBar
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// Custom emoji mapping for goals with sparkles
private fun getEmojiForGoal(goal: SavingGoal): String = when (goal) {
    SavingGoal.BECOME_DEBT_FREE -> "💸"
    SavingGoal.BUILD_EMERGENCY_FUND -> "🛡️"
    SavingGoal.SAVE_FOR_VACATION -> "✈️"
    SavingGoal.SAVE_FOR_PROPERTY -> "🏠"
    SavingGoal.MORE_FINANCIAL_FREEDOM -> "🗽"
    SavingGoal.INVEST_AND_GROW_WEALTH -> "📈"
}

// Dream-like gradient colors for goals
private fun getGradientForGoal(goal: SavingGoal): List<Color> = when (goal) {
    SavingGoal.BECOME_DEBT_FREE -> listOf(Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39))
    SavingGoal.BUILD_EMERGENCY_FUND -> listOf(Color(0xFF2196F3), Color(0xFF03DAC6), Color(0xFF00BCD4))
    SavingGoal.SAVE_FOR_VACATION -> listOf(Color(0xFFFF9800), Color(0xFFFFEB3B), Color(0xFFFFC107))
    SavingGoal.SAVE_FOR_PROPERTY -> listOf(Color(0xFF9C27B0), Color(0xFFE91E63), Color(0xFFFF5722))
    SavingGoal.MORE_FINANCIAL_FREEDOM -> listOf(Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3))
    SavingGoal.INVEST_AND_GROW_WEALTH -> listOf(Color(0xFFFF5722), Color(0xFFFF9800), Color(0xFFFFC107))
}

@Composable
fun AnimatedSparkles(
    modifier: Modifier = Modifier
) {
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Restart
        ), label = "sparkles"
    )

    Canvas(modifier = modifier) {
        val sparkleCount = 8
        repeat(sparkleCount) { index ->
            val angle = (360f / sparkleCount) * index + (animationProgress * 360f)
            val radius = 50f + (animationProgress * 20f)
            val x = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * radius
            val y = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * radius

            val alpha = (sin(animationProgress * Math.PI * 2 + index).toFloat() + 1f) / 2f

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 3f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun FloatingDreamIcon(
    emoji: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val floatOffset by animateFloatAsState(
        targetValue = if (isSelected) -15f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ), label = "dream_float"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isSelected) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Restart
        ), label = "dream_rotation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ), label = "dream_scale"
    )

    Box(
        modifier = modifier
            .offset(y = floatOffset.dp)
            .scale(scale)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            AnimatedSparkles(modifier = Modifier.size(100.dp))
        }

        Text(
            text = emoji,
            fontSize = 28.sp
        )
    }
}

@Composable
fun DreamyGoalCard(
    goal: SavingGoal,
    isSelected: Boolean,
    onClick: () -> Unit,
    index: Int,
    modifier: Modifier = Modifier
) {
    val emoji = getEmojiForGoal(goal)
    val gradient = getGradientForGoal(goal)

    // Staggered entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * 200).toLong())
        isVisible = true
    }

    val cardScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "card_entrance"
    )

    val selectedElevation by animateDpAsState(
        targetValue = if (isSelected) 16.dp else 4.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "card_elevation"
    )

    val borderGlow by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ), label = "border_glow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .scale(cardScale)
            .border(
                width = if (isSelected) (2 + borderGlow).dp else 0.dp,
                brush = if (isSelected) Brush.horizontalGradient(gradient) else Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = selectedElevation),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box {
            // Animated background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isSelected) {
                            Brush.linearGradient(gradient.map { it.copy(alpha = 0.15f) })
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent))
                        }
                    )
            )

            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Animated goal icon
                FloatingDreamIcon(
                    emoji = emoji,
                    isSelected = isSelected,
                    modifier = Modifier.size(60.dp)
                )

                // Goal text
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = goal.displayName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = if (isSelected) 16.sp else 15.sp
                        ),
                        color = if (isSelected) {
                            gradient.first()
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    if (isSelected) {
                        Text(
                            text = "✨ Dein Traumziel!",
                            style = MaterialTheme.typography.bodySmall,
                            color = gradient.first().copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Selection indicator
                if (isSelected) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = gradient.first(),
                        modifier = Modifier
                            .size(32.dp)
                            .scale(
                                animateFloatAsState(
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000),
                                        repeatMode = RepeatMode.Reverse
                                    ), label = "star_pulse"
                                ).value
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun DreamyHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated dream cloud
        Text(
            text = "💭",
            fontSize = 48.sp,
            modifier = Modifier
                .scale(
                    animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000),
                            repeatMode = RepeatMode.Reverse
                        ), label = "cloud_scale"
                    ).value
                )
                .padding(bottom = 8.dp)
        )

        Text(
            text = "Genug von Problemen geredet.",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("🌟", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Wovon träumst du?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("🌟", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(spacing.s))

        Text(
            text = "Ein Ziel ist der stärkste Motivator. Was ist deins?",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun MagicContinueButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shimmer by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ), label = "button_shimmer"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) Color(0xFFFF6B35) else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                if (enabled) {
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF6B35).copy(alpha = 0.3f * shimmer),
                            Color.Transparent,
                            Color(0xFFFF6B35).copy(alpha = 0.3f * shimmer)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (enabled) {
                Text("✨", fontSize = 20.sp)
            }
            Text(
                text = if (enabled) "Träume verwirklichen!" else "Wähle dein Traumziel",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            if (enabled) {
                Text("🚀", fontSize = 20.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3Goals(
    navController: NavController,
    onGoalsSelected: (List<SavingGoal>) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGoals by remember { mutableStateOf<List<SavingGoal>>(emptyList()) }

    Scaffold(
        topBar = { OnboardingTopAppBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFF3E5F5),
                            Color(0xFFE8F5E8),
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
                // Dreamy header
                DreamyHeader()

                Spacer(modifier = Modifier.height(spacing.xl))

                // Goal cards
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(SavingGoal.entries) { index, goal ->
                        DreamyGoalCard(
                            goal = goal,
                            isSelected = goal in selectedGoals,
                            onClick = {
                                selectedGoals = if (goal in selectedGoals) {
                                    selectedGoals - goal
                                } else {
                                    selectedGoals + goal
                                }
                                onGoalsSelected(selectedGoals)
                            },
                            index = index
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.l))

                // Magic continue button
                MagicContinueButton(
                    enabled = selectedGoals.isNotEmpty(),
                    onClick = onNextClicked
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Step3GoalsPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        Step3Goals(navController = navController, onGoalsSelected = {}, onNextClicked = {})
    }
}