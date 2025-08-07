package app.tinygiants.getalife.presentation.onboarding.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Emoji categories with their emojis
data class EmojiCategory(
    val name: String,
    val icon: String,
    val emojis: List<String>
)

private val emojiCategories = listOf(
    EmojiCategory(
        name = "Gesichter",
        icon = "😀",
        emojis = listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃", "😉", "😊", "😇",
            "🥰", "😍", "🤩", "😘", "😗", "😚", "😙", "🥲", "😋", "😛", "😜", "🤪", "😝",
            "🤑", "🤗", "🤭", "🤫", "🤔", "🤐", "🤨", "😐", "😑", "😶", "😶‍🌫️", "😏", "😒",
            "🙄", "😬", "😮‍💨", "🤥", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮"
        )
    ),
    EmojiCategory(
        name = "Geld",
        icon = "💰",
        emojis = listOf(
            "💰", "💵", "💴", "💶", "💷", "💸", "💳", "💎", "⚖️", "🏦", "🏪", "🏬",
            "💰", "🪙", "💲", "💱", "💹", "📊", "📈", "📉", "💼", "👔", "🎯", "✨"
        )
    ),
    EmojiCategory(
        name = "Transport",
        icon = "🚗",
        emojis = listOf(
            "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐", "🛻", "🚚",
            "🚛", "🚜", "🏍️", "🛵", "🚲", "🛴", "🛹", "🚁", "✈️", "🛩️", "🚀", "🚢"
        )
    ),
    EmojiCategory(
        name = "Häuser",
        icon = "🏠",
        emojis = listOf(
            "🏠", "🏡", "🏘️", "🏰", "🏢", "🏬", "🏦", "🏪", "🏫", "🏨", "🏩", "💒",
            "🏛️", "⛪", "🕌", "🏝️", "🏞️", "🏟️", "🏗️", "🏚️", "🏓", "🛖", "🏔️", "⛰️"
        )
    ),
    EmojiCategory(
        name = "Essen",
        icon = "🍕",
        emojis = listOf(
            "🍕", "🍔", "🍟", "🌭", "🥪", "🌮", "🌯", "🥙", "🧆", "🥚", "🍳", "🥞",
            "🧇", "🥓", "🥩", "🍗", "🍖", "🦴", "🌶️", "🥕", "🌽", "🥒", "🥬", "🥦"
        )
    ),
    EmojiCategory(
        name = "Aktivitäten",
        icon = "⚽",
        emojis = listOf(
            "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🎱", "🪀", "🏓", "🏸",
            "🥅", "⛳", "🪁", "🏹", "🎣", "🤿", "🥊", "🥋", "🎽", "🛹", "🛼", "🎿"
        )
    ),
    EmojiCategory(
        name = "Reisen",
        icon = "✈️",
        emojis = listOf(
            "✈️", "🛩️", "🛫", "🛬", "🪂", "💺", "🚁", "🚀", "🛸", "🚢", "⛵", "🚤",
            "🛶", "⛴️", "🚂", "🚝", "🚄", "🚅", "🚆", "🚇", "🚈", "🚉", "🚊", "🚋"
        )
    ),
    EmojiCategory(
        name = "Objekte",
        icon = "📱",
        emojis = listOf(
            "📱", "💻", "🖥️", "🖨️", "⌨️", "🖱️", "🖲️", "💽", "💾", "💿", "📀", "📼",
            "📷", "📸", "📹", "🎥", "📞", "☎️", "📟", "📠", "📺", "📻", "����️", "🎚️"
        )
    )
)

@Composable
fun CustomEmojiPicker(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        // Header
        Text(
            text = "Emoji auswählen",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )

        // Category tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(emojiCategories.size) { index ->
                EmojiCategoryTab(
                    category = emojiCategories[index],
                    isSelected = selectedCategory == index,
                    onClick = { selectedCategory = index }
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        // Emoji grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(emojiCategories[selectedCategory].emojis) { emoji ->
                EmojiItem(
                    emoji = emoji,
                    isSelected = selectedEmoji == emoji,
                    onClick = { onEmojiSelected(emoji) }
                )
            }
        }
    }
}

@Composable
private fun EmojiCategoryTab(
    category: EmojiCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300), label = "tab_background"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "tab_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = category.icon,
                fontSize = 16.sp
            )
            if (isSelected) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun EmojiItem(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "emoji_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(200), label = "emoji_background"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() }
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerBottomSheet(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(Modifier.size(width = 32.dp, height = 4.dp))
            }
        }
    ) {
        CustomEmojiPicker(
            selectedEmoji = selectedEmoji,
            onEmojiSelected = { emoji ->
                onEmojiSelected(emoji)
                onDismiss()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomEmojiPickerPreview() {
    MaterialTheme {
        CustomEmojiPicker(
            selectedEmoji = "💰",
            onEmojiSelected = { }
        )
    }
}

/**
 * Example usage component showing how to integrate the emoji picker.
 */
@Composable
fun EmojiPickerExample() {
    var selectedEmoji by remember { mutableStateOf("💰") }
    var showEmojiPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Aktuelles Emoji:",
            style = MaterialTheme.typography.bodyLarge
        )

        // Clickable emoji button
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .clickable { showEmojiPicker = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectedEmoji,
                fontSize = 32.sp
            )
        }

        Button(
            onClick = { showEmojiPicker = true }
        ) {
            Text("Emoji ändern")
        }
    }

    // Show emoji picker options
    if (showEmojiPicker) {
        // Option 1: BottomSheet (can have category switching issues)
        EmojiPickerBottomSheet(
            selectedEmoji = selectedEmoji,
            onEmojiSelected = { selectedEmoji = it },
            onDismiss = { showEmojiPicker = false }
        )

        // Option 2: FullScreen Dialog (recommended - works perfectly)
        // Uncomment the line below and comment the BottomSheet above to use
        /*
        EmojiPickerFullScreenDialog(
            selectedEmoji = selectedEmoji,
            onEmojiSelected = { selectedEmoji = it },
            onDismiss = { showEmojiPicker = false }
        )
        */
    }
}

@Preview(showBackground = true)
@Composable
private fun EmojiPickerExamplePreview() {
    MaterialTheme {
        EmojiPickerExample()
    }
}

/**
 * Shows an alternative fullscreen dialog for picking emojis.
 *
 * @param selectedEmoji The currently selected emoji (to highlight).
 * @param onEmojiSelected Callback when a user picks an emoji.
 * @param onDismiss Called when user dismisses dialog.
 * @param modifier Modifier for dialog body (container).
 */
@Composable
fun EmojiPickerFullScreenDialog(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(0.dp)
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Close button and header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 8.dp,
                                top = 8.dp,
                                end = 8.dp,
                                bottom = 0.dp,
                            )
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Schließen",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Emoji auswählen",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(end = 48.dp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // The actual picker
                    CustomEmojiPicker(
                        selectedEmoji = selectedEmoji,
                        onEmojiSelected = {
                            onEmojiSelected(it)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}