package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorId: String,
    val authorName: String,
    val plushieName: String,
    val plushieBreed: String,
    val authorAvatarUri: String,
    val isAuthorVerified: Boolean = false,
    val isAuthorArtist: Boolean = false, // Visual artist indicator across feed
    val photoUri: String,
    val filterName: String = "Original",
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val caption: String,
    val tags: String = "",
    val plushieMood: String = "Cozy & Sleepy 💤",
    val soundAlbumTitle: String = "", // TikTok-style music album on posts
    val soundArtistName: String = "",
    val soundAudioUri: String = "",
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val commentCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val algorithmicScore: Double = 0.0
)
