package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.alpha
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
import app.tinygiants.getalife.domain.model.onboarding.FinancialChallenge
import app.tinygiants.getalife.presentation.onboarding.components.OnboardingTopAppBar
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.delay

// Custom emoji mapping for challenges
private fun getEmojiForChallenge(challenge: FinancialChallenge): String = when (challenge) {
    FinancialChallenge.UNEXPECTED_BILLS -> "⚡"
    FinancialChallenge.HIDDEN_SUBSCRIPTIONS -> "🔍"
    FinancialChallenge.SPONTANEOUS_PURCHASES -> "🛒"
    FinancialChallenge.HIGH_FIXED_COSTS -> "📈"
    FinancialChallenge.DEBT_REPAYMENT -> "💳"
    FinancialChallenge.NO_RETIREMENT_PLAN -> "🎯"
}

// Custom colors for different challenges
private fun getColorForChallenge(challenge: FinancialChallenge): Color = when (challenge) {
    FinancialChallenge.UNEXPECTED_BILLS -> Color(0xFFFF5722)
    FinancialChallenge.HIDDEN_SUBSCRIPTIONS -> Color(0xFF9C27B0)
    FinancialChallenge.SPONTANEOUS_PURCHASES -> Color(0xFFFF9800)
    FinancialChallenge.HIGH_FIXED_COSTS -> Color(0xFFF44336)
    FinancialChallenge.DEBT_REPAYMENT -> Color(0xFF3F51B5)
    FinancialChallenge.NO_RETIREMENT_PLAN -> Color(0xFF009688)
}

@Composable
fun AnimatedCheckIcon(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Checkbox(
        checked = isSelected,
        onCheckedChange = null,
        modifier = modifier.size(28.dp),
        enabled = false
    )
}

@Composable
fun FloatingEmoji(
    emoji: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ), label = "emoji_float"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ), label = "emoji_alpha"
    )

    Text(
        text = emoji,
        fontSize = 24.sp,
        modifier = modifier
            .offset(y = offsetY.dp)
            .alpha(alpha)
    )
}

@Composable
fun FancyChallengeCard(
    challenge: FinancialChallenge,
    isSelected: Boolean,
    onClick: () -> Unit,
    index: Int,
    modifier: Modifier = Modifier
) {
    val emoji = getEmojiForChallenge(challenge)
    val color = getColorForChallenge(challenge)

    // Staggered animation entry
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * 150).toLong())
        isVisible = true
    }

    val cardScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ), label = "card_entry"
    )

    val selectedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "selected_scale"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        animationSpec = tween(300), label = "border_width"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .scale(cardScale * selectedScale)
            .border(
                width = borderWidth,
                color = color,
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
            // Animated checkbox
            AnimatedCheckIcon(isSelected = isSelected)

            // Challenge content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = challenge.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            // Floating emoji
            FloatingEmoji(
                emoji = emoji,
                isVisible = isSelected
            )
        }
    }
}

@Composable
fun ProgressIndicator(
    selectedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (totalCount > 0) selectedCount.toFloat() / totalCount else 0f,
        animationSpec = tween(500), label = "progress"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ausgewählt:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "$selectedCount",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (selectedCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.5f
                    )
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
fun PulsingContinueButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = if (enabled) {
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(200)
        }, label = "button_pulse"
    )

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
            .scale(scale)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weiter",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            if (enabled) {
                Text("🚀", fontSize = 20.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2Challenges(
    navController: NavController,
    onChallengesSelected: (List<FinancialChallenge>) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChallenges by remember { mutableStateOf<List<FinancialChallenge>>(emptyList()) }

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
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
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
                // Animated header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎯",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Was sind deine größten Herausforderungen?",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(spacing.s))

                    Text(
                        text = "Wähle aus, was auf dich zutrifft. Mehrfachauswahl ist möglich.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.l))

                // Progress indicator
                ProgressIndicator(
                    selectedCount = selectedChallenges.size,
                    totalCount = FinancialChallenge.entries.size
                )

                Spacer(modifier = Modifier.height(spacing.l))

                // Challenge cards
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(FinancialChallenge.entries) { index, challenge ->
                        FancyChallengeCard(
                            challenge = challenge,
                            isSelected = challenge in selectedChallenges,
                            onClick = {
                                selectedChallenges = if (challenge in selectedChallenges) {
                                    selectedChallenges - challenge
                                } else {
                                    selectedChallenges + challenge
                                }
                                onChallengesSelected(selectedChallenges)
                            },
                            index = index
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.l))

                // Animated continue button
                PulsingContinueButton(
                    enabled = selectedChallenges.isNotEmpty(),
                    onClick = onNextClicked
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Step2ChallengesPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        Step2Challenges(navController = navController, onChallengesSelected = {}, onNextClicked = {})
    }
}