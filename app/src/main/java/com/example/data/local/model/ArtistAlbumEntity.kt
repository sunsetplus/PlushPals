package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artist_albums")
data class ArtistAlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val artistId: String,
    val artistName: String,
    val title: String,
    val coverUri: String = "drawable://plushie_hero_dog",
    val description: String = "",
    val genre: String = "Bedtime Lullabies & 432Hz Calm",
    val audioUri: String, // MP3 Audio file for the sound album
    val trackCount: Int = 1,
    val useCount: Int = 0, // Number of posts using this album sound
    val timestamp: Long = System.currentTimeMillis()
)
