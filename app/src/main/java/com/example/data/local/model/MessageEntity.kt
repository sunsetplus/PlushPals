package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val threadId: String,
    val senderId: String,
    val senderName: String,
    val recipientId: String,
    val text: String,
    val stickerType: String = "NONE", // "NONE", "PAW_HUG", "SNUGGLE_CHECKIN", "HIGH_PAW", "HEART_CUDDLE"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = true
)
