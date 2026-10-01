package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey
    val threadId: String,
    val participantId: String,
    val participantName: String,
    val participantPlushie: String,
    val participantAvatar: String,
    val isParticipantVerified: Boolean = false,
    val lastMessage: String,
    val lastTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)
