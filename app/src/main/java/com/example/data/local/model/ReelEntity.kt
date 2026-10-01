package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reels")
data class ReelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorName: String,
    val authorAvatarUri: String,
    val plushieName: String,
    val plushieBreed: String,
    val isAuthorVerified: Boolean = false,
    val mediaUri: String,
    val caption: String,
    val audioTrack: String = "Gentle Rain & Soft Breathing 🌧️",
    val tags: String = "",
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val commentCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
