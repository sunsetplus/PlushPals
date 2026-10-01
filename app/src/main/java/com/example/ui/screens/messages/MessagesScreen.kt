package com.example.ui.screens.messages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.local.model.ChatThreadEntity
import com.example.data.local.model.MessageEntity
import com.example.ui.components.PlushieImage
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmCaramel

@Composable
fun MessagesScreen(
    threads: List<ChatThreadEntity>,
    activeThread: ChatThreadEntity?,
    messages: List<MessageEntity>,
    currentUserId: String,
    onThreadSelected: (ChatThreadEntity) -> Unit,
    onCloseChat: () -> Unit,
    onSendMessage: (text: String, sticker: String) -> Unit
) {
    if (activeThread != null) {
        BackHandler { onCloseChat() }
        ChatConversationView(
            thread = activeThread,
            messages = messages,
            currentUserId = currentUserId,
            onBack = onCloseChat,
            onSendMessage = onSendMessage
        )
    } else {
        InboxListView(
            threads = threads,
            onThreadSelected = onThreadSelected
        )
    }
}

@Composable
fun InboxListView(
    threads: List<ChatThreadEntity>,
    onThreadSelected: (ChatThreadEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
            .testTag("messages_inbox_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Companion Messages",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.MarkChatUnread,
                contentDescription = null,
                tint = WarmCaramel
            )
        }

        if (threads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = WarmCaramel,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No private chats yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Connect with other emotional support dog plushie owners!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(threads, key = { it.threadId }) { thread ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThreadSelected(thread) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("chat_thread_${thread.threadId}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlushieImage(
                            uri = thread.participantAvatar,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = thread.participantName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (thread.isParticipantVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    VerificationBadge(size = 14.dp)
                                }
                            }
                            Text(
                                text = "🐾 ${thread.participantPlushie}",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarmCaramel
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = thread.lastMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (thread.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (thread.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }

                        if (thread.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(WarmCaramel),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${thread.unreadCount}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = WarmBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun ChatConversationView(
    thread: ChatThreadEntity,
    messages: List<MessageEntity>,
    currentUserId: String,
    onBack: () -> Unit,
    onSendMessage: (text: String, sticker: String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("chat_conversation_view")
    ) {
        // Chat Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("chat_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                PlushieImage(
                    uri = thread.participantAvatar,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = thread.participantName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (thread.isParticipantVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerificationBadge(size = 14.dp)
                        }
                    }
                    Text(
                        text = "🐾 ${thread.participantPlushie} • Online",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmCaramel
                    )
                }
            }
        }

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            reverseLayout = false,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = msg.senderId == currentUserId

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    // Check if it's a comfort sticker
                    if (msg.stickerType != "NONE") {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val stickerLabel = when (msg.stickerType) {
                                    "PAW_HUG" -> "🐾 Sent a Gentle Paw Hug"
                                    "SNUGGLE_CHECKIN" -> "🧸 Sent a Snuggle Check-in"
                                    "HIGH_PAW" -> "🐶 High Paw!"
                                    else -> "💛 Sent Warm Love"
                                }
                                Text(
                                    text = stickerLabel,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    if (msg.text.isNotBlank()) {
                        Surface(
                            color = if (isMe) WarmCaramel else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick comfort stickers row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val stickers = listOf(
                Pair("PAW_HUG", "🐾 Paw Hug"),
                Pair("SNUGGLE_CHECKIN", "🧸 Snuggle Check"),
                Pair("HIGH_PAW", "🐶 High Paw!")
            )
            stickers.forEach { (type, label) ->
                SuggestionChip(
                    onClick = { onSendMessage("", type) },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Send comforting message...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_text_input"),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSendMessage(textInput, "NONE")
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(WarmCaramel)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}
