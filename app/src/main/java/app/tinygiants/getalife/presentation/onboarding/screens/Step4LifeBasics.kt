package app.tinygiants.getalife.presentation.onboarding.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import app.tinygiants.getalife.domain.model.onboarding.HousingSituation
import app.tinygiants.getalife.domain.model.onboarding.Pet
import app.tinygiants.getalife.domain.model.onboarding.TransportationType
import app.tinygiants.getalife.presentation.onboarding.components.OnboardingTopAppBar
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.delay

// Custom emoji mapping for life basics
private fun getEmojiForHousing(housing: HousingSituation): String = when (housing) {
    HousingSituation.RENT -> "🏠"
    HousingSituation.OWN_WITH_MORTGAGE -> "🏘️"
    HousingSituation.OWN_PAID_OFF -> "🏡"
    HousingSituation.SHARED_LIVING -> "👥"
}

private fun getEmojiForTransport(transport: TransportationType): String = when (transport) {
    TransportationType.CAR -> "🚗"
    TransportationType.MOTORCYCLE -> "🏍️"
    TransportationType.PUBLIC_TRANSPORT -> "🚌"
    TransportationType.BIKE_OR_FOOT -> "🚶‍♀️"
}

private fun getEmojiForPet(pet: Pet): String = when (pet) {
    Pet.DOG -> "🐕"
    Pet.CAT -> "🐱"
    Pet.SMALL_ANIMAL -> "🐹"
    Pet.NONE -> "🚫"
}

// Consistent color scheme with previous screens
private fun getColorForCategory(category: String): Color = when (category) {
    "housing" -> Color(0xFF2196F3)
    "transport" -> Color(0xFF4CAF50)
    "pets" -> Color(0xFFFF9800)
    else -> Color(0xFF9C27B0)
}

@Composable
fun AnimatedSectionHeader(
    title: String,
    emoji: String,
    category: String,
    modifier: Modifier = Modifier
) {
    val color = getColorForCategory(category)

    val floatOffset by animateFloatAsState(
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
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
                .background(color.copy(alpha = 0.1f)),
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
fun FancyRadioOption(
    text: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    category: String,
    index: Int,
    modifier: Modifier = Modifier
) {
    val color = getColorForCategory(category)

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
            .padding(vertical = 4.dp)
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
            // Custom radio indicator
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier.scale(
                    if (isSelected) 1.2f else 1f
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
fun FancyCheckboxOption(
    text: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    category: String,
    index: Int,
    modifier: Modifier = Modifier
) {
    val color = getColorForCategory(category)

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
            .padding(vertical = 4.dp)
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
                modifier = Modifier.size(28.dp),
                enabled = false,
                colors = CheckboxDefaults.colors(
                    checkedColor = color,
                    uncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier.scale(
                    if (isSelected) 1.2f else 1f
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
fun LifeBasicsHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏠",
            fontSize = 48.sp,
            modifier = Modifier
                .scale(
                    animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000),
                            repeatMode = RepeatMode.Reverse
                        ), label = "house_scale"
                    ).value
                )
                .padding(bottom = 8.dp)
        )

        Text(
            text = "Beginnen wir mit den Grundlagen.",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.s))

        Text(
            text = "Diese helfen uns, deine größten Fixkosten automatisch anzulegen.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun LifeBasicsContinueButton(
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
                            animation = tween(1000),
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
                Text("🏠", fontSize = 20.sp)
            }
            Text(
                text = if (enabled) "Grundlagen festhalten!" else "Vervollständige die Angaben",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            if (enabled) {
                Text("✨", fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun Step4LifeBasics(
    navController: NavController,
    onNextClicked: (HousingSituation?, List<TransportationType>, List<Pet>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHousing by remember { mutableStateOf<HousingSituation?>(null) }
    var selectedTransport by remember { mutableStateOf<List<TransportationType>>(emptyList()) }
    var selectedPets by remember { mutableStateOf<List<Pet>>(emptyList()) }

    Scaffold(
        topBar = { OnboardingTopAppBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFFF1F8E9),
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
                LifeBasicsHeader()

                Spacer(modifier = Modifier.height(spacing.xl))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Housing Section
                    AnimatedSectionHeader(
                        title = "Wie wohnst du?",
                        emoji = "🏠",
                        category = "housing"
                    )

                    HousingSituation.entries.forEachIndexed { index, situation ->
                        FancyRadioOption(
                            text = situation.displayName,
                            emoji = getEmojiForHousing(situation),
                            isSelected = selectedHousing == situation,
                            onClick = { selectedHousing = situation },
                            category = "housing",
                            index = index
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.l))

                    // Transportation Section
                    AnimatedSectionHeader(
                        title = "Wie bist du unterwegs?",
                        emoji = "🚗",
                        category = "transport"
                    )

                    TransportationType.entries.forEachIndexed { index, transport ->
                        FancyCheckboxOption(
                            text = transport.displayName,
                            emoji = getEmojiForTransport(transport),
                            isSelected = transport in selectedTransport,
                            onClick = {
                                selectedTransport = if (transport in selectedTransport) {
                                    selectedTransport - transport
                                } else {
                                    selectedTransport + transport
                                }
                            },
                            category = "transport",
                            index = index
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.l))

                    // Pets Section
                    AnimatedSectionHeader(
                        title = "Hast du ein Haustier?",
                        emoji = "🐕",
                        category = "pets"
                    )

                    Pet.entries.forEachIndexed { index, pet ->
                        FancyCheckboxOption(
                            text = pet.displayName,
                            emoji = getEmojiForPet(pet),
                            isSelected = pet in selectedPets,
                            onClick = {
                                selectedPets = if (pet == Pet.NONE) {
                                    if (pet in selectedPets) emptyList() else listOf(Pet.NONE)
                                } else {
                                    if (pet in selectedPets) {
                                        selectedPets - pet
                                    } else {
                                        (selectedPets - Pet.NONE) + pet
                                    }
                                }
                            },
                            category = "pets",
                            index = index
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.l))

                // Continue Button
                LifeBasicsContinueButton(
                    enabled = selectedHousing != null && selectedTransport.isNotEmpty() && selectedPets.isNotEmpty(),
                    onClick = { onNextClicked(selectedHousing, selectedTransport, selectedPets) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Step4LifeBasicsPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        Step4LifeBasics(navController = navController, onNextClicked = { _, _, _ -> })
    }
}