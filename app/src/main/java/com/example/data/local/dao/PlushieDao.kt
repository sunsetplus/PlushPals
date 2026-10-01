package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlushieDao {

    // --- Profiles ---
    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserSync(): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles ORDER BY displayName ASC")
    fun getAllUsers(): Flow<List<UserProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserProfileEntity>)

    @Update
    suspend fun updateUser(user: UserProfileEntity)

    // --- Posts ---
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    fun getPostById(postId: Long): Flow<PostEntity?>

    @Query("SELECT * FROM posts WHERE authorId = :authorId ORDER BY timestamp DESC")
    fun getPostsByAuthor(authorId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE isLiked = 1 ORDER BY timestamp DESC")
    fun getLikedPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("UPDATE posts SET isLiked = :isLiked, likeCount = likeCount + :delta WHERE id = :postId")
    suspend fun updateLikeStatus(postId: Long, isLiked: Boolean, delta: Int)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Long)

    // --- Comments ---
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("UPDATE posts SET commentCount = commentCount + 1 WHERE id = :postId")
    suspend fun incrementCommentCount(postId: Long)

    // --- Stories ---
    @Query("SELECT * FROM stories ORDER BY timestamp DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Query("UPDATE stories SET isViewed = 1 WHERE id = :storyId")
    suspend fun markStoryViewed(storyId: Long)

    // --- Reels ---
    @Query("SELECT * FROM reels ORDER BY timestamp DESC")
    fun getAllReels(): Flow<List<ReelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: ReelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReels(reels: List<ReelEntity>)

    @Query("UPDATE reels SET isLiked = :isLiked, likeCount = likeCount + :delta WHERE id = :reelId")
    suspend fun updateReelLike(reelId: Long, isLiked: Boolean, delta: Int)

    // --- Chat & Messages ---
    @Query("SELECT * FROM chat_threads ORDER BY lastTimestamp DESC")
    fun getAllChatThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThread(threadId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateThread(thread: ChatThreadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreads(threads: List<ChatThreadEntity>)

    @Query("UPDATE chat_threads SET unreadCount = 0 WHERE threadId = :threadId")
    suspend fun markThreadRead(threadId: String)

    @Query("UPDATE chat_threads SET lastMessage = :lastMsg, lastTimestamp = :timestamp WHERE threadId = :threadId")
    suspend fun updateThreadLastMessage(threadId: String, lastMsg: String, timestamp: Long)

    // --- User Affinities (Discovery Algorithm) ---
    @Query("SELECT * FROM user_affinities")
    fun getAllAffinities(): Flow<List<UserAffinityEntity>>

    @Query("SELECT * FROM user_affinities WHERE affinityKey = :key LIMIT 1")
    suspend fun getAffinity(key: String): UserAffinityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAffinity(affinity: UserAffinityEntity)

    // --- Night AI Bedtime & Music Messages ---
    @Query("SELECT * FROM night_ai_messages ORDER BY timestamp ASC")
    fun getAllNightAiMessages(): Flow<List<NightAiMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNightAiMessage(message: NightAiMessageEntity)

    @Query("DELETE FROM night_ai_messages")
    suspend fun clearNightAiMessages()

    // --- Artist Profiles & MP3 Tracks ---
    @Query("SELECT * FROM artist_tracks ORDER BY timestamp DESC")
    fun getAllArtistTracks(): Flow<List<ArtistTrackEntity>>

    @Query("SELECT * FROM artist_tracks WHERE artistId = :artistId ORDER BY timestamp DESC")
    fun getTracksByArtist(artistId: String): Flow<List<ArtistTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtistTrack(track: ArtistTrackEntity): Long

    @Query("DELETE FROM artist_tracks WHERE id = :trackId")
    suspend fun deleteArtistTrack(trackId: Long)

    @Query("UPDATE artist_tracks SET playCount = playCount + 1 WHERE id = :trackId")
    suspend fun incrementTrackPlayCount(trackId: Long)

    @Query("UPDATE artist_tracks SET isLiked = :isLiked WHERE id = :trackId")
    suspend fun updateTrackLike(trackId: Long, isLiked: Boolean)

    // --- Music Albums (TikTok-style sound albums) ---
    @Query("SELECT * FROM artist_albums ORDER BY timestamp DESC")
    fun getAllArtistAlbums(): Flow<List<ArtistAlbumEntity>>

    @Query("SELECT * FROM artist_albums WHERE artistId = :artistId ORDER BY timestamp DESC")
    fun getAlbumsByArtist(artistId: String): Flow<List<ArtistAlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtistAlbum(album: ArtistAlbumEntity): Long

    @Query("DELETE FROM artist_albums WHERE id = :albumId")
    suspend fun deleteArtistAlbum(albumId: Long)

    @Query("UPDATE artist_albums SET useCount = useCount + 1 WHERE id = :albumId")
    suspend fun incrementAlbumUseCount(albumId: Long)
}
