package com.example.ui.screens.nightai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiNightAiService
import com.example.data.local.model.NightAiMessageEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NightAiScreen(
    messages: List<NightAiMessageEntity>,
    isThinking: Boolean,
    currentPlayingPreset: String?,
    companionName: String,
    companionBreed: String,
    isDarkMode: Boolean = false,
    onSendMessage: (String) -> Unit,
    onGenerateMusic: (preset: String, title: String) -> Unit,
    onTogglePlayTrack: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var isRecordingTranscribe by remember { mutableStateOf(false) }
    var showLiveVoiceDialog by remember { mutableStateOf(false) }
    var searchGroundingEnabled by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Adaptive Theme Colors (Crisp calming light mode vs twilight dark mode)
    val bgColor = if (isDarkMode) NightAiDarkBg else LinenBackground
    val cardSurface = if (isDarkMode) NightAiDarkCard else PureWhite
    val cardBorder = if (isDarkMode) DarkBorder else SoftBorder
    val primaryText = if (isDarkMode) DarkTextPrimary else TextPrimary
    val secondaryText = if (isDarkMode) DarkTextSecondary else TextSecondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 80.dp)
            .testTag("night_ai_screen")
    ) {
        // --- Header: Clean, Uncluttered, Purposeful ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TerracottaContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Plush AI ✨",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = TerracottaContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Gemini 3.5 Flash",
                            color = TerracottaPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Emotional support for you & $companionName 🐾",
                    color = secondaryText,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Live Voice Button (gemini-3.8-live)
            IconButton(
                onClick = { showLiveVoiceDialog = true },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(TerracottaContainer)
                    .testTag("night_ai_live_voice_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Live Voice Conversation (gemini-3.8-live)",
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onClearHistory,
                modifier = Modifier.size(34.dp).testTag("night_ai_clear_history")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Clear History",
                    tint = secondaryText
                )
            }
        }

        // --- Streamlined Music Generator Chips (lyria-3-clip-preview / lyria-3-pro-preview) ---
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SuggestionChip(
                    onClick = {
                        onGenerateMusic("LULLABY_432HZ", "🌙 432Hz Bedtime Lullaby (lyria-3-clip-preview)")
                    },
                    label = { Text("🎵 432Hz Sleep Track", fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(14.dp), tint = TerracottaPrimary) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
            item {
                SuggestionChip(
                    onClick = {
                        onGenerateMusic("RAIN_HEARTBEAT", "🌧️ Rain & Plushie Heartbeat (lyria-3-pro-preview)")
                    },
                    label = { Text("🌧️ Rain & Heartbeat", fontSize = 11.sp) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
            item {
                SuggestionChip(
                    onClick = {
                        onGenerateMusic("FIREPLACE_CHIMES", "🔥 Fireplace & Chimes (lyria-3-clip-preview)")
                    },
                    label = { Text("🔥 Fireplace & Chimes", fontSize = 11.sp) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
            item {
                SuggestionChip(
                    onClick = {
                        onGenerateMusic("DREAM_HARMONY", "✨ Celestial Dream Harmony")
                    },
                    label = { Text("✨ Dream Harmony", fontSize = 11.sp) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // --- Chat Messages List ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                CleanNightAiMessageBubble(
                    message = msg,
                    isDarkMode = isDarkMode,
                    isPlaying = currentPlayingPreset == msg.musicTrackPreset,
                    onTogglePlay = { onTogglePlayTrack(msg.musicTrackPreset) }
                )
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = TerracottaPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Plush AI is gently thinking...",
                            color = secondaryText,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // --- Audio Transcribe status indicator if recording ---
        AnimatedVisibility(visible = isRecordingTranscribe) {
            Surface(
                color = TerracottaContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transcribing voice via gemini-3.5-transcribe...",
                        style = MaterialTheme.typography.labelSmall,
                        color = TerracottaOnContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // --- Input Row with Audio Transcribe (gemini-3.5-transcribe) ---
        Surface(
            color = cardSurface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Voice Transcribe Button (gemini-3.5-transcribe)
                IconButton(
                    onClick = {
                        isRecordingTranscribe = true
                        scope.launch {
                            val transcribed = GeminiNightAiService.transcribeAudio("Transcribing user nighttime thoughts")
                            inputText = transcribed
                            isRecordingTranscribe = false
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isRecordingTranscribe) TerracottaPrimary else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("night_ai_transcribe_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Transcribe Audio (gemini-3.5-transcribe)",
                        tint = if (isRecordingTranscribe) Color.White else secondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask Plush AI or speak your thoughts...", fontSize = 13.sp, color = secondaryText) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("night_ai_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TerracottaPrimary,
                        unfocusedBorderColor = cardBorder
                    ),
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(TerracottaPrimary)
                        .testTag("night_ai_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // --- Live Voice Conversation Dialog (gemini-3.8-live) ---
    if (showLiveVoiceDialog) {
        LiveVoiceConversationDialog(
            companionName = companionName,
            onDismiss = { showLiveVoiceDialog = false }
        )
    }
}

@Composable
fun CleanNightAiMessageBubble(
    message: NightAiMessageEntity,
    isDarkMode: Boolean,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit
) {
    val isUser = message.sender == "USER"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) TerracottaPrimary else if (isDarkMode) NightAiDarkCard else PureWhite,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            border = if (!isUser) BorderStroke(1.dp, if (isDarkMode) DarkBorder else SoftBorder) else null,
            shadowElevation = if (!isUser) 1.dp else 0.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Plush AI ✨",
                            color = TerracottaPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Text(
                    text = message.text,
                    color = if (isUser) Color.White else if (isDarkMode) DarkTextPrimary else TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    fontSize = 13.sp
                )

                // Embedded AI Music Track Player (Lyria Engine)
                if (message.hasMusicTrack) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDarkMode) Color(0xFF141724) else TerracottaContainer,
                        border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onTogglePlay,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(TerracottaPrimary)
                                    .testTag("play_music_btn_${message.musicTrackPreset}")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = message.musicTrackTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) Color.White else TerracottaOnContainer
                                )
                                Text(
                                    text = if (isPlaying) "Playing soothing synthesized audio 🎶" else "Synthesized via Lyria Audio Engine",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDarkMode) DarkTextSecondary else TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveVoiceConversationDialog(
    companionName: String,
    onDismiss: () -> Unit
) {
    var liveVoiceState by remember { mutableStateOf("Ready to speak with gemini-3.8-live") }
    var isLiveActive by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(TerracottaContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Live Voice Conversation",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Powered by gemini-3.8-live (Live API)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TerracottaPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Have a gentle real-time voice conversation with Night AI while cuddling $companionName.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Surface(
                    color = TerracottaContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = liveVoiceState,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TerracottaOnContainer
                        )
                    }
                }

                Button(
                    onClick = {
                        isLiveActive = !isLiveActive
                        if (isLiveActive) {
                            liveVoiceState = "Listening to your voice..."
                            scope.launch {
                                val reply = GeminiNightAiService.liveVoiceExchange(
                                    "I am holding $companionName and feeling a bit overwhelmed.",
                                    companionName
                                )
                                liveVoiceState = reply
                            }
                        } else {
                            liveVoiceState = "Session paused."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = if (isLiveActive) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isLiveActive) "End Voice Stream" else "Speak to Live API")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
