package com.example.data.repository

import com.example.data.local.dao.PlushieDao
import com.example.data.local.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.max

data class AlgorithmScoreBreakdown(
    val totalScore: Double,
    val breedBonus: Double,
    val matchedBreed: String,
    val tagBonus: Double,
    val matchedTags: List<String>,
    val engagementBonus: Double,
    val recencyBonus: Double,
    val verifiedBonus: Double,
    val explanation: List<String>
)

class PlushieRepository(private val plushieDao: PlushieDao) {

    val currentUser: Flow<UserProfileEntity?> = plushieDao.getCurrentUser()
    val allUsers: Flow<List<UserProfileEntity>> = plushieDao.getAllUsers()
    val rawPosts: Flow<List<PostEntity>> = plushieDao.getAllPosts()
    val likedPosts: Flow<List<PostEntity>> = plushieDao.getLikedPosts()
    val stories: Flow<List<StoryEntity>> = plushieDao.getAllStories()
    val reels: Flow<List<ReelEntity>> = plushieDao.getAllReels()
    val chatThreads: Flow<List<ChatThreadEntity>> = plushieDao.getAllChatThreads()
    val affinities: Flow<List<UserAffinityEntity>> = plushieDao.getAllAffinities()
    val nightAiMessages: Flow<List<NightAiMessageEntity>> = plushieDao.getAllNightAiMessages()
    val allArtistTracks: Flow<List<ArtistTrackEntity>> = plushieDao.getAllArtistTracks()
    val allArtistAlbums: Flow<List<ArtistAlbumEntity>> = plushieDao.getAllArtistAlbums()

    fun getAlbumsByArtist(artistId: String): Flow<List<ArtistAlbumEntity>> =
        plushieDao.getAlbumsByArtist(artistId)

    fun getTracksByArtist(artistId: String): Flow<List<ArtistTrackEntity>> =
        plushieDao.getTracksByArtist(artistId)

    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>> =
        plushieDao.getCommentsForPost(postId)

    fun getMessagesForThread(threadId: String): Flow<List<MessageEntity>> =
        plushieDao.getMessagesForThread(threadId)

    fun getPostsByAuthor(authorId: String): Flow<List<PostEntity>> =
        plushieDao.getPostsByAuthor(authorId)

    // Dynamic Discovery Algorithm combining user affinities, breed matching, tags, recency & engagement
    val algorithmicFeed: Flow<List<PostEntity>> = combine(rawPosts, affinities) { posts, affinityList ->
        val affinityMap = affinityList.associate { it.affinityKey to it.interactionScore }
        val now = System.currentTimeMillis()

        posts.map { post ->
            val hoursOld = max(0.1, (now - post.timestamp).toDouble() / (1000.0 * 3600.0))
            val recencyScore = max(0.0, 35.0 - (hoursOld * 1.2))

            val breedWeight = affinityMap["breed:${post.plushieBreed}"] ?: 1.0
            val breedBonus = breedWeight * 5.0

            var tagBonus = 0.0
            val postTagList = post.tags.split(",").map { it.trim() }
            for (tag in postTagList) {
                val weight = affinityMap["tag:$tag"] ?: 0.0
                tagBonus += weight * 3.0
            }

            val engagementScore = (post.likeCount * 0.3) + (post.commentCount * 0.7)
            val verifiedBonus = if (post.isAuthorVerified) 12.0 else 0.0

            val computedScore = recencyScore + breedBonus + tagBonus + engagementScore + verifiedBonus
            post.copy(algorithmicScore = computedScore)
        }.sortedByDescending { it.algorithmicScore }
    }

    suspend fun getBreakdownForPost(post: PostEntity): AlgorithmScoreBreakdown {
        val affinityList = affinities.firstOrNull() ?: emptyList()
        val affinityMap = affinityList.associate { it.affinityKey to it.interactionScore }
        val now = System.currentTimeMillis()

        val hoursOld = max(0.1, (now - post.timestamp).toDouble() / (1000.0 * 3600.0))
        val recencyBonus = max(0.0, 35.0 - (hoursOld * 1.2))

        val breedWeight = affinityMap["breed:${post.plushieBreed}"] ?: 0.0
        val breedBonus = breedWeight * 5.0

        val matchedTags = mutableListOf<String>()
        var tagBonus = 0.0
        val postTagList = post.tags.split(",").map { it.trim() }
        for (tag in postTagList) {
            val weight = affinityMap["tag:$tag"] ?: 0.0
            if (weight > 0) {
                matchedTags.add(tag)
                tagBonus += weight * 3.0
            }
        }

        val engagementBonus = (post.likeCount * 0.3) + (post.commentCount * 0.7)
        val verifiedBonus = if (post.isAuthorVerified) 12.0 else 0.0
        val total = recencyBonus + breedBonus + tagBonus + engagementBonus + verifiedBonus

        val explanations = mutableListOf<String>()
        if (breedBonus > 0) {
            explanations.add("+${breedBonus.toInt()} pts: High affinity for ${post.plushieBreed} plushies based on your recent interactions")
        }
        if (matchedTags.isNotEmpty()) {
            explanations.add("+${tagBonus.toInt()} pts: Matches comfort tags you follow (${matchedTags.joinToString(", ")})")
        }
        if (recencyBonus > 20) {
            explanations.add("+${recencyBonus.toInt()} pts: Fresh companion update shared within last few hours")
        }
        if (verifiedBonus > 0) {
            explanations.add("+12 pts: Verified emotional support companion badge")
        }
        if (engagementBonus > 15) {
            explanations.add("+${engagementBonus.toInt()} pts: High community resonance & heartwarming hugs")
        }

        return AlgorithmScoreBreakdown(
            totalScore = total,
            breedBonus = breedBonus,
            matchedBreed = post.plushieBreed,
            tagBonus = tagBonus,
            matchedTags = matchedTags,
            engagementBonus = engagementBonus,
            recencyBonus = recencyBonus,
            verifiedBonus = verifiedBonus,
            explanation = explanations
        )
    }

    suspend fun createPost(
        caption: String,
        photoUri: String,
        filterName: String,
        brightness: Float,
        contrast: Float,
        tags: String,
        mood: String,
        soundAlbumTitle: String = "",
        soundArtistName: String = "",
        soundAudioUri: String = ""
    ): Long {
        val user = plushieDao.getCurrentUserSync() ?: return -1
        val newPost = PostEntity(
            authorId = user.userId,
            authorName = user.displayName,
            plushieName = user.plushieName,
            plushieBreed = user.plushieBreed,
            authorAvatarUri = user.profileAvatarUri,
            isAuthorVerified = user.isVerified,
            isAuthorArtist = user.isArtist,
            photoUri = photoUri,
            filterName = filterName,
            brightness = brightness,
            contrast = contrast,
            caption = caption,
            tags = tags,
            plushieMood = mood,
            soundAlbumTitle = soundAlbumTitle,
            soundArtistName = soundArtistName,
            soundAudioUri = soundAudioUri,
            likeCount = 1,
            isLiked = true,
            commentCount = 0,
            timestamp = System.currentTimeMillis()
        )
        val id = plushieDao.insertPost(newPost)
        // Boost user affinity for own tags and breed
        boostAffinity("breed:${user.plushieBreed}", 2.0)
        tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
            boostAffinity("tag:$tag", 1.5)
        }
        return id
    }

    suspend fun toggleLikePost(post: PostEntity) {
        val newLiked = !post.isLiked
        val delta = if (newLiked) 1 else -1
        plushieDao.updateLikeStatus(post.id, newLiked, delta)

        if (newLiked) {
            boostAffinity("breed:${post.plushieBreed}", 1.5)
            post.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                boostAffinity("tag:$tag", 1.0)
            }
        }
    }

    suspend fun addComment(postId: Long, text: String) {
        val user = plushieDao.getCurrentUserSync() ?: return
        val comment = CommentEntity(
            postId = postId,
            authorName = user.displayName,
            authorAvatarUri = user.profileAvatarUri,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        plushieDao.insertComment(comment)
        plushieDao.incrementCommentCount(postId)
    }

    suspend fun createStory(caption: String, mediaUri: String) {
        val user = plushieDao.getCurrentUserSync() ?: return
        val story = StoryEntity(
            authorName = user.displayName,
            authorAvatarUri = user.profileAvatarUri,
            plushieName = user.plushieName,
            mediaUri = mediaUri,
            caption = caption,
            timestamp = System.currentTimeMillis(),
            isViewed = false
        )
        plushieDao.insertStory(story)
    }

    suspend fun markStoryViewed(storyId: Long) {
        plushieDao.markStoryViewed(storyId)
    }

    suspend fun createReel(
        caption: String,
        mediaUri: String,
        audioTrack: String,
        tags: String
    ) {
        val user = plushieDao.getCurrentUserSync() ?: return
        val reel = ReelEntity(
            authorName = user.displayName,
            plushieName = user.plushieName,
            plushieBreed = user.plushieBreed,
            authorAvatarUri = user.profileAvatarUri,
            isAuthorVerified = user.isVerified,
            mediaUri = mediaUri,
            caption = caption,
            audioTrack = audioTrack,
            tags = tags,
            likeCount = 1,
            isLiked = true,
            commentCount = 0,
            timestamp = System.currentTimeMillis()
        )
        plushieDao.insertReel(reel)
    }

    suspend fun toggleLikeReel(reel: ReelEntity) {
        val newLiked = !reel.isLiked
        val delta = if (newLiked) 1 else -1
        plushieDao.updateReelLike(reel.id, newLiked, delta)
    }

    suspend fun sendMessage(
        threadId: String,
        recipientId: String,
        text: String,
        stickerType: String = "NONE"
    ) {
        val user = plushieDao.getCurrentUserSync() ?: return
        val now = System.currentTimeMillis()

        val message = MessageEntity(
            threadId = threadId,
            senderId = user.userId,
            senderName = user.displayName,
            recipientId = recipientId,
            text = text,
            stickerType = stickerType,
            timestamp = now,
            isRead = true
        )
        plushieDao.insertMessage(message)
        plushieDao.updateThreadLastMessage(threadId, text.ifEmpty { "Sticker sent" }, now)
    }

    suspend fun markThreadRead(threadId: String) {
        plushieDao.markThreadRead(threadId)
    }

    suspend fun updateProfile(
        displayName: String,
        plushieName: String,
        plushieBreed: String,
        bio: String,
        comfortRole: String,
        avatarUri: String
    ) {
        val currentUser = plushieDao.getCurrentUserSync() ?: return
        val updated = currentUser.copy(
            displayName = displayName,
            plushieName = plushieName,
            plushieBreed = plushieBreed,
            bio = bio,
            comfortRole = comfortRole,
            profileAvatarUri = avatarUri
        )
        plushieDao.updateUser(updated)
    }

    suspend fun applyForVerification(badgeType: String) {
        val currentUser = plushieDao.getCurrentUserSync() ?: return
        val updated = currentUser.copy(
            isVerified = true,
            verificationBadgeType = badgeType
        )
        plushieDao.updateUser(updated)
    }

    suspend fun switchGoogleAccount(
        accountName: String,
        email: String,
        plushieName: String,
        breed: String
    ) {
        val now = System.currentTimeMillis()
        val newUser = UserProfileEntity(
            userId = "user_${email.hashCode()}",
            displayName = "$accountName & $plushieName",
            plushieName = plushieName,
            plushieBreed = breed,
            bio = "Certified companion parent. Loving and cherishing my plushie dog daily. 🐾✨",
            comfortRole = "Emotional Support & Sensory Grounding",
            profileAvatarUri = "drawable://plushie_hero_dog",
            isVerified = true,
            verificationBadgeType = "CERTIFIED_COMPANION",
            joinedDate = now,
            followerCount = 45,
            followingCount = 60,
            isCurrentUser = true
        )
        plushieDao.insertUser(newUser)
    }

    suspend fun sendNightAiMessage(userText: String) {
        val user = plushieDao.getCurrentUserSync()
        val pName = user?.plushieName ?: "Companion"
        val pBreed = user?.plushieBreed ?: "Golden Retriever"

        // 1. Insert user message
        val userMsg = NightAiMessageEntity(
            sender = "USER",
            text = userText,
            timestamp = System.currentTimeMillis()
        )
        plushieDao.insertNightAiMessage(userMsg)

        // 2. Query Gemini Plush AI
        val aiResponse = com.example.ai.PlushAiService.generatePlushAiResponse(userText, pName, pBreed)

        // 3. Insert AI response
        val aiMsg = NightAiMessageEntity(
            sender = "PLUSH_AI",
            text = aiResponse.replyText,
            hasMusicTrack = aiResponse.hasMusicTrack,
            musicTrackTitle = aiResponse.musicTrackTitle,
            musicTrackPreset = aiResponse.musicTrackPreset,
            timestamp = System.currentTimeMillis()
        )
        plushieDao.insertNightAiMessage(aiMsg)
    }

    suspend fun generateNightAiMusic(preset: String, title: String) {
        val user = plushieDao.getCurrentUserSync()
        val pName = user?.plushieName ?: "Companion"
        val now = System.currentTimeMillis()

        val promptText = "Generate soothing bedtime music: $title"
        plushieDao.insertNightAiMessage(
            NightAiMessageEntity(sender = "USER", text = promptText, timestamp = now)
        )

        val aiText = "Here is a soothing bedtime track synthesized for you and $pName by Plush AI ✨. Designed to slow your breathing, calm sensory overload, and anchor you into peaceful sleep."
        plushieDao.insertNightAiMessage(
            NightAiMessageEntity(
                sender = "PLUSH_AI",
                text = aiText,
                hasMusicTrack = true,
                musicTrackTitle = title,
                musicTrackPreset = preset,
                timestamp = now + 100
            )
        )
    }

    suspend fun clearNightAiHistory() {
        plushieDao.clearNightAiMessages()
    }

    suspend fun uploadArtistTrack(
        title: String,
        audioUri: String,
        category: String,
        durationText: String
    ): Result<Long> {
        val user = plushieDao.getCurrentUserSync() ?: return Result.failure(Exception("User not authenticated"))
        if (!user.isArtist) {
            // Auto-enable artist mode so any user can upload MP3 audio immediately
            plushieDao.updateUser(user.copy(isArtist = true))
        }
        val track = ArtistTrackEntity(
            artistId = user.userId,
            artistName = user.displayName,
            title = title,
            audioUri = audioUri,
            category = category,
            durationText = durationText,
            timestamp = System.currentTimeMillis()
        )
        val id = plushieDao.insertArtistTrack(track)
        return Result.success(id)
    }

    suspend fun deleteArtistTrack(trackId: Long) {
        plushieDao.deleteArtistTrack(trackId)
    }

    suspend fun incrementTrackPlay(trackId: Long) {
        plushieDao.incrementTrackPlayCount(trackId)
    }

    suspend fun toggleTrackLike(trackId: Long, isLiked: Boolean) {
        plushieDao.updateTrackLike(trackId, isLiked)
    }

    suspend fun updateArtistProfile(
        isArtist: Boolean,
        genre: String,
        artistBio: String
    ) {
        val currentUser = plushieDao.getCurrentUserSync() ?: return
        val updated = currentUser.copy(
            isArtist = isArtist,
            artistGenre = genre,
            artistBio = artistBio
        )
        plushieDao.updateUser(updated)
    }

    suspend fun uploadArtistAlbum(
        title: String,
        description: String,
        genre: String,
        audioUri: String,
        coverUri: String
    ): Result<Long> {
        val user = plushieDao.getCurrentUserSync() ?: return Result.failure(Exception("User not authenticated"))
        if (!user.isArtist) {
            return Result.failure(Exception("Only verified Artist profiles can upload music albums."))
        }
        val album = ArtistAlbumEntity(
            artistId = user.userId,
            artistName = user.displayName,
            title = title,
            description = description,
            genre = genre,
            audioUri = audioUri,
            coverUri = coverUri.ifBlank { user.profileAvatarUri },
            timestamp = System.currentTimeMillis()
        )
        val id = plushieDao.insertArtistAlbum(album)
        return Result.success(id)
    }

    suspend fun deleteArtistAlbum(albumId: Long) {
        plushieDao.deleteArtistAlbum(albumId)
    }

    private suspend fun boostAffinity(key: String, delta: Double) {
        val current = plushieDao.getAffinity(key)
        val newScore = (current?.interactionScore ?: 0.0) + delta
        plushieDao.insertOrUpdateAffinity(
            UserAffinityEntity(affinityKey = key, interactionScore = newScore, lastUpdated = System.currentTimeMillis())
        )
    }
}
