package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_affinities")
data class UserAffinityEntity(
    @PrimaryKey
    val affinityKey: String, // e.g. "breed:Golden Retriever", "tag:#BedtimeCuddles", "author:maya_cuddles"
    val interactionScore: Double = 1.0,
    val lastUpdated: Long = System.currentTimeMillis()
)
