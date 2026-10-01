package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "night_ai_messages")
data class NightAiMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER" or "NIGHT_AI"
    val text: String,
    val hasMusicTrack: Boolean = false,
    val musicTrackTitle: String = "",
    val musicTrackPreset: String = "LULLABY_432HZ", // "LULLABY_432HZ", "RAIN_HEARTBEAT", "FIREPLACE_CHIMES", "DREAM_HARMONY"
    val timestamp: Long = System.currentTimeMillis()
)
