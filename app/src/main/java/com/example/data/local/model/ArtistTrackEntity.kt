package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artist_tracks")
data class ArtistTrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val artistId: String,
    val artistName: String,
    val title: String,
    val audioUri: String, // Real content:// or file:// URI of the uploaded MP3
    val category: String = "Bedtime Lullaby", // "Bedtime Lullaby", "432Hz Calm", "Rain & Nature", "Plushie Heartbeat"
    val durationText: String = "Audio Track",
    val timestamp: Long = System.currentTimeMillis(),
    val playCount: Int = 0,
    val isLiked: Boolean = false
)
