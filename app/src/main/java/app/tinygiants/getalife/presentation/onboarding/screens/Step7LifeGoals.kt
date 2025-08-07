package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import app.tinygiants.getalife.domain.model.onboarding.LifeGoal
import app.tinygiants.getalife.presentation.onboarding.components.OnboardingTopAppBar
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// Custom emoji mapping for life goals
private fun getEmojiForLifeGoal(goal: LifeGoal): String = when (goal) {
    LifeGoal.DREAM_VACATION -> "🏝️"
    LifeGoal.NEW_CAR -> "🚗"
    LifeGoal.WEDDING -> "💒"
    LifeGoal.CHILD -> "👶"
    LifeGoal.SPECIAL_GIFT -> "🎁"
    LifeGoal.MAJOR_PURCHASE -> "🛋️"
    LifeGoal.EMERGENCY_FUND -> "💰"
}

// Dream-like gradient colors for life goals
private fun getGradientForLifeGoal(goal: LifeGoal): List<Color> = when (goal) {
    LifeGoal.DREAM_VACATION -> listOf(Color(0xFF00BCD4), Color(0xFF4FC3F7), Color(0xFF81D4FA))
    LifeGoal.NEW_CAR -> listOf(Color(0xFF607D8B), Color(0xFF90A4AE), Color(0xFFB0BEC5))
    LifeGoal.WEDDING -> listOf(Color(0xFFE91E63), Color(0xFFF06292), Color(0xFFF8BBD9))
    LifeGoal.CHILD -> listOf(Color(0xFFFFEB3B), Color(0xFFFFF176), Color(0xFFFFF9C4))
    LifeGoal.SPECIAL_GIFT -> listOf(Color(0xFF9C27B0), Color(0xFFBA68C8), Color(0xFFE1BEE7))
    LifeGoal.MAJOR_PURCHASE -> listOf(Color(0xFF795548), Color(0xFFA1887F), Color(0xFFD7CCC8))
    LifeGoal.EMERGENCY_FUND -> listOf(Color(0xFF4CAF50), Color(0xFF81C784), Color(0xFFC8E6C9))
}

@Composable
fun FinalSparkleEffect(
    modifier: Modifier = Modifier
) {
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Restart
        ), label = "final_sparkles"
    )

    Canvas(modifier = modifier) {
        val sparkleCount = 12
        repeat(sparkleCount) { index ->
            val angle = (360f / sparkleCount) * index + (animationProgress * 720f)
            val radius = 80f + (animationProgress * 40f)
            val x = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * radius
            val y = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * radius

            val alpha = (sin(animationProgress * Math.PI * 3 + index).toFloat() + 1f) / 2f

            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.8f),
                radius = 4f,
                center = Offset(x, y)
            )

            // Additional smaller sparkles
            val smallRadius = 30f
            val smallX = center.x + cos(Math.toRadians(angle.toDouble() * 2)).toFloat() * smallRadius
            val smallY = center.y + sin(Math.toRadians(angle.toDouble() * 2)).toFloat() * smallRadius

            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = alpha * 0.6f),
                radius = 2f,
                center = Offset(smallX, smallY)
            )
        }
    }
}

@Composable
fun LifeGoalDreamCard(
    goal: LifeGoal,
    isSelected: Boolean,
    onClick: () -> Unit,
    index: Int,
    modifier: Modifier = Modifier
) {
    val emoji = getEmojiForLifeGoal(goal)
    val gradient = getGradientForLifeGoal(goal)

    // Staggered entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * 150).toLong())
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
        targetValue = if (isSelected) 20.dp else 6.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "card_elevation"
    )

    val borderGlow by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ), label = "border_glow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .scale(cardScale)
            .border(
                width = if (isSelected) (3 + borderGlow * 2).dp else 0.dp,
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
                            Brush.linearGradient(gradient.map { it.copy(alpha = 0.2f) })
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent))
                        }
                    )
            )

            // Sparkle effect for selected goals
            if (isSelected) {
                FinalSparkleEffect(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                )
            }

            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Fancy checkbox
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) gradient.first() else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(
                                    animateFloatAsState(
                                        targetValue = 360f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(3000),
                                            repeatMode = RepeatMode.Restart
                                        ), label = "star_rotation"
                                    ).value
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                        )
                    }
                }

                // Goal emoji with animation
                Text(
                    text = emoji,
                    fontSize = 32.sp,
                    modifier = Modifier.scale(
                        animateFloatAsState(
                            targetValue = if (isSelected) 1.3f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy
                            ), label = "emoji_scale"
                        ).value
                    )
                )

                // Goal text
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = goal.displayName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = if (isSelected) 17.sp else 16.sp,
                            color = if (isSelected) gradient.first() else MaterialTheme.colorScheme.onSurface
                        )
                    )

                    if (isSelected) {
                        Text(
                            text = "✨ Ausgewähltes Sparziel!",
                            style = MaterialTheme.typography.bodySmall,
                            color = gradient.first().copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinalGoalsHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated trophy with sparkles
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp)
        ) {
            FinalSparkleEffect(modifier = Modifier.fillMaxSize())

            Text(
                text = "🏆",
                fontSize = 56.sp,
                modifier = Modifier
                    .scale(
                        animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000),
                                repeatMode = RepeatMode.Reverse
                            ), label = "trophy_scale"
                        ).value
                    )
            )
        }

        Spacer(modifier = Modifier.height(spacing.m))

        Text(
            text = "Zum Schluss:",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Wofür möchtest du gezielt sparen?",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B35)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.s))

        Text(
            text = "Gib deinen Sparzielen einen Namen. Das motiviert ungemein.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun FinalCreatePlanButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shimmerAnimation by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500),
            repeatMode = RepeatMode.Restart
        ), label = "shimmer"
    )

    val bounceScale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = if (enabled) {
            infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(200)
        }, label = "bounce"
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
            .height(64.dp)
            .scale(bounceScale)
            .background(
                if (enabled) {
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF6B35).copy(alpha = 0.4f * shimmerAnimation),
                            Color.Transparent,
                            Color(0xFFFF6B35).copy(alpha = 0.4f * shimmerAnimation)
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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (enabled) {
                Text("🎯", fontSize = 24.sp)
            }
            Text(
                text = if (enabled) "Meinen persönlichen Plan erstellen!" else "Wähle mindestens ein Sparziel",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
            if (enabled) {
                Text("✨", fontSize = 24.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step7LifeGoals(
    navController: NavController,
    onNextClicked: (List<LifeGoal>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGoals by remember { mutableStateOf<List<LifeGoal>>(emptyList()) }

    Scaffold(
        topBar = { OnboardingTopAppBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFF3E0),
                            Color(0xFFFFE0B2),
                            Color(0xFFFFF8E1),
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
                FinalGoalsHeader()

                Spacer(modifier = Modifier.height(spacing.xl))

                // Scrollable goal cards
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LifeGoal.entries.forEachIndexed { index, goal ->
                        LifeGoalDreamCard(
                            goal = goal,
                            isSelected = goal in selectedGoals,
                            onClick = {
                                selectedGoals = if (goal in selectedGoals) {
                                    selectedGoals - goal
                                } else {
                                    selectedGoals + goal
                                }
                            },
                            index = index
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.l))

                // Final create plan button
                FinalCreatePlanButton(
                    enabled = selectedGoals.isNotEmpty(),
                    onClick = { onNextClicked(selectedGoals) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Step7LifeGoalsPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        Step7LifeGoals(navController = navController, onNextClicked = { })
    }
}