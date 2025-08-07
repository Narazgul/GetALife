package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import app.tinygiants.getalife.domain.model.onboarding.FinancialFeeling
import app.tinygiants.getalife.presentation.onboarding.components.OnboardingTopAppBar
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.delay

// Custom emoji mapping for feelings
private fun getEmojiForFeeling(feeling: FinancialFeeling): String = when (feeling) {
    FinancialFeeling.OVERWHELMED -> "😰"
    FinancialFeeling.COULD_BE_BETTER -> "🤔"
    FinancialFeeling.CLUELESS -> "🤷‍♀️"
    FinancialFeeling.OPTIMIZING -> "🎯"
}

// Custom gradient colors for feelings
private fun getGradientForFeeling(feeling: FinancialFeeling): List<Color> = when (feeling) {
    FinancialFeeling.OVERWHELMED -> listOf(Color(0xFFFF6B6B), Color(0xFFFF8E8E))
    FinancialFeeling.COULD_BE_BETTER -> listOf(Color(0xFFFFA726), Color(0xFFFFCC80))
    FinancialFeeling.CLUELESS -> listOf(Color(0xFF42A5F5), Color(0xFF90CAF9))
    FinancialFeeling.OPTIMIZING -> listOf(Color(0xFF66BB6A), Color(0xFFA5D6A7))
}

@Composable
fun AnimatedEmoji(
    emoji: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "emoji_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isSelected) 360f else 0f,
        animationSpec = tween(800), label = "emoji_rotation"
    )

    Text(
        text = emoji,
        fontSize = 32.sp,
        modifier = modifier
            .scale(scale)
            .rotate(rotation)
    )
}

@Composable
fun FancyFeelingCard(
    feeling: FinancialFeeling,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = getGradientForFeeling(feeling)
    val emoji = getEmojiForFeeling(feeling)

    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "card_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 4.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "card_elevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xs)
            .scale(cardScale)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(gradient)
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Emoji circle background
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedEmoji(
                    emoji = emoji,
                    isSelected = isSelected
                )
            }

            Text(
                text = feeling.displayName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PulsingHeartIcon() {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = "heart_pulse"
    )

    Icon(
        Icons.Default.Favorite,
        contentDescription = null,
        tint = Color(0xFFE91E63),
        modifier = Modifier
            .size(24.dp)
            .scale(scale)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1EmotionalCheck(
    navController: NavController,
    onFeelingSelected: (FinancialFeeling) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFeeling by remember { mutableStateOf<FinancialFeeling?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }

    // Automatically navigate after a selection and a short delay
    LaunchedEffect(selectedFeeling) {
        if (selectedFeeling != null) {
            showConfirmation = true
            delay(1500) // Wait for 1.5 seconds to show the confirmation
            onFeelingSelected(selectedFeeling!!)
        }
    }

    Scaffold(
        topBar = { OnboardingTopAppBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = showConfirmation,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) + scaleIn(animationSpec = tween(600)) togetherWith
                            fadeOut(animationSpec = tween(300))
                }, label = "content_transition"
            ) { isConfirmation ->
                if (isConfirmation) {
                    // Confirmation State with celebration animation
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = spacing.m, vertical = spacing.l),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Success animation
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFF4CAF50), Color(0xFF81C784))
                                    )
                                )
                                .padding(spacing.m),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✨",
                                fontSize = 48.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.xl))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PulsingHeartIcon()
                            Text(
                                text = "Verstanden!",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            PulsingHeartIcon()
                        }

                        Spacer(modifier = Modifier.height(spacing.m))

                        Text(
                            text = "Keine Sorge, das ändern wir jetzt gemeinsam.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    // Selection State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = spacing.m, vertical = spacing.l),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Animated title with heart emoji
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("❤️", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hand aufs Herz:",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.s))

                        Text(
                            text = "Wie fühlst du dich bei deinen Finanzen?",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        Text(
                            text = "Deine ehrliche Antwort hilft uns, den perfekten Startpunkt für dich zu finden.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(spacing.xl))

                        // Fancy feeling cards
                        FinancialFeeling.entries.forEach { feeling ->
                            FancyFeelingCard(
                                feeling = feeling,
                                isSelected = selectedFeeling == feeling,
                                onClick = { selectedFeeling = feeling }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Step1EmotionalCheckPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        Step1EmotionalCheck(navController = navController, onFeelingSelected = {})
    }
}