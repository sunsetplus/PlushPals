package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String,
    val displayName: String,
    val plushieName: String,
    val plushieBreed: String,
    val bio: String,
    val comfortRole: String,
    val profileAvatarUri: String,
    val isVerified: Boolean = false,
    val verificationBadgeType: String = "CERTIFIED_COMPANION", // "CERTIFIED_COMPANION", "SENIOR_SNUGGLE_PRO", "TOP_COMFORT_ANCHOR"
    val isArtist: Boolean = false, // Artist profile toggle
    val artistGenre: String = "Bedtime Lullabies & Ambient Sounds",
    val artistBio: String = "",
    val joinedDate: Long = System.currentTimeMillis(),
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val isCurrentUser: Boolean = false
)
