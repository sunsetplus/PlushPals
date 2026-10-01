package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorName: String,
    val authorAvatarUri: String,
    val plushieName: String,
    val mediaUri: String,
    val caption: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isViewed: Boolean = false
)
