package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.database.PlushieDatabase
import com.example.data.local.model.*
import com.example.data.repository.AlgorithmScoreBreakdown
import com.example.data.repository.PlushieRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FeedFilterType {
    FOR_YOU, // Intelligent Discovery Algorithm
    RECENT,
    POPULAR,
    VERIFIED_ONLY
}

data class CreatePostDraft(
    val photoUri: String = "drawable://plushie_hero_dog",
    val filterName: String = "Original",
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val caption: String = "",
    val selectedTags: List<String> = listOf("#FluffyTherapy", "#AnxietyRelief"),
    val mood: String = "Cozy & Sleepy 💤",
    val soundAlbumTitle: String = "",
    val soundArtistName: String = "",
    val soundAudioUri: String = ""
)

class PlushieViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PlushieDatabase.getDatabase(application, viewModelScope)
    val repository = PlushieRepository(database.plushieDao())

    // Current logged-in user
    val currentUser = repository.currentUser.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    // Feed filter
    private val _feedFilter = MutableStateFlow(FeedFilterType.FOR_YOU)
    val feedFilter: StateFlow<FeedFilterType> = _feedFilter.asStateFlow()

    private val _breedFilter = MutableStateFlow("All")
    val breedFilter: StateFlow<String> = _breedFilter.asStateFlow()

    // Pull-to-refresh state for Home feed
    private val _isRefreshingFeed = MutableStateFlow(false)
    val isRefreshingFeed: StateFlow<Boolean> = _isRefreshingFeed.asStateFlow()

    fun refreshHomeFeed() {
        viewModelScope.launch {
            _isRefreshingFeed.value = true
            kotlinx.coroutines.delay(650) // Realistic tactile feedback
            _snackbarMessage.emit("✨ Home feed updated with latest companion moments!")
            _isRefreshingFeed.value = false
        }
    }

    // Combined filtered feed
    val feedPosts: StateFlow<List<PostEntity>> = combine(
        repository.algorithmicFeed,
        repository.rawPosts,
        _feedFilter,
        _breedFilter
    ) { algoPosts, rawPosts, filter, breed ->
        val baseList = when (filter) {
            FeedFilterType.FOR_YOU -> algoPosts
            FeedFilterType.RECENT -> rawPosts.sortedByDescending { it.timestamp }
            FeedFilterType.POPULAR -> rawPosts.sortedByDescending { it.likeCount + it.commentCount }
            FeedFilterType.VERIFIED_ONLY -> rawPosts.filter { it.isAuthorVerified }
        }
        if (breed == "All") {
            baseList
        } else {
            baseList.filter { it.plushieBreed.equals(breed, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stories: StateFlow<List<StoryEntity>> = repository.stories.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val reels: StateFlow<List<ReelEntity>> = repository.reels.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val chatThreads: StateFlow<List<ChatThreadEntity>> = repository.chatThreads.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val likedPosts: StateFlow<List<PostEntity>> = repository.likedPosts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Night AI state
    val nightAiMessages: StateFlow<List<NightAiMessageEntity>> = repository.nightAiMessages.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Real Artist Tracks (MP3s uploaded exclusively by Artists)
    val allArtistTracks: StateFlow<List<ArtistTrackEntity>> = repository.allArtistTracks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Music Albums (TikTok-style sound albums uploaded by Artists)
    val allArtistAlbums: StateFlow<List<ArtistAlbumEntity>> = repository.allArtistAlbums.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val currentPlayingMp3Id = com.example.ai.PlushieMp3Player.currentlyPlayingTrackId
    val isMp3Playing = com.example.ai.PlushieMp3Player.isPlaying

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _currentPlayingPreset = MutableStateFlow<String?>(null)
    val currentPlayingPreset: StateFlow<String?> = _currentPlayingPreset.asStateFlow()

    fun sendNightAiMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isAiThinking.value = true
            try {
                repository.sendNightAiMessage(text.trim())
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun generateNightAiMusic(preset: String, title: String) {
        viewModelScope.launch {
            _isAiThinking.value = true
            try {
                repository.generateNightAiMusic(preset, title)
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun togglePlayTrack(preset: String) {
        com.example.ai.PlushieAudioSynthesizer.playPreset(preset) {
            _currentPlayingPreset.value = com.example.ai.PlushieAudioSynthesizer.getCurrentPlayingPreset()
        }
        _currentPlayingPreset.value = com.example.ai.PlushieAudioSynthesizer.getCurrentPlayingPreset()
    }

    fun stopAudio() {
        com.example.ai.PlushieAudioSynthesizer.stop()
        _currentPlayingPreset.value = null
    }

    fun clearNightAiHistory() {
        viewModelScope.launch {
            repository.clearNightAiHistory()
            _snackbarMessage.emit("Plush AI memory gently refreshed ✨")
        }
    }

    fun generateCaptionWithPlushAi(mood: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value
            val pName = user?.plushieName ?: "Barnaby"
            val pBreed = user?.plushieBreed ?: "Golden Retriever"
            _snackbarMessage.emit("Plush AI is crafting a caption... ✨")
            val caption = com.example.ai.PlushAiService.generateCaptionWithPlushAi(pName, pBreed, mood)
            onResult(caption)
        }
    }

    fun playArtistMp3Track(context: android.content.Context, track: ArtistTrackEntity) {
        com.example.ai.PlushieMp3Player.playTrack(context, track.id, track.audioUri)
        viewModelScope.launch {
            repository.incrementTrackPlay(track.id)
        }
    }

    fun pauseArtistMp3() {
        com.example.ai.PlushieMp3Player.pause()
    }

    fun uploadArtistTrack(title: String, audioUri: String, category: String, duration: String, onSuccess: () -> Unit) {
        if (title.isBlank() || audioUri.isBlank()) {
            viewModelScope.launch {
                _snackbarMessage.emit("Please select an MP3 audio file and enter a title 🎵")
            }
            return
        }
        viewModelScope.launch {
            val result = repository.uploadArtistTrack(title.trim(), audioUri, category, duration)
            if (result.isSuccess) {
                _snackbarMessage.emit("🎵 MP3 track published successfully!")
                onSuccess()
            } else {
                _snackbarMessage.emit(result.exceptionOrNull()?.message ?: "Upload failed. Artist profile required.")
            }
        }
    }

    fun deleteArtistTrack(trackId: Long) {
        viewModelScope.launch {
            repository.deleteArtistTrack(trackId)
            _snackbarMessage.emit("Track removed from your artist catalog 🗑️")
        }
    }

    fun updateArtistProfile(isArtist: Boolean, genre: String, bio: String) {
        viewModelScope.launch {
            repository.updateArtistProfile(isArtist, genre, bio)
            _snackbarMessage.emit(if (isArtist) "✨ Artist Mode activated! You can now upload MP3 tracks." else "Switched to standard companion profile.")
        }
    }

    // Story viewer dialog
    private val _activeStory = MutableStateFlow<StoryEntity?>(null)
    val activeStory: StateFlow<StoryEntity?> = _activeStory.asStateFlow()

    // Comments bottom sheet
    private val _activeCommentsPost = MutableStateFlow<PostEntity?>(null)
    val activeCommentsPost: StateFlow<PostEntity?> = _activeCommentsPost.asStateFlow()

    val currentPostComments: StateFlow<List<CommentEntity>> = _activeCommentsPost.flatMapLatest { post ->
        if (post != null) repository.getCommentsForPost(post.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Algorithm transparency dialog
    private val _algorithmBreakdown = MutableStateFlow<Pair<PostEntity, AlgorithmScoreBreakdown>?>(null)
    val algorithmBreakdown: StateFlow<Pair<PostEntity, AlgorithmScoreBreakdown>?> = _algorithmBreakdown.asStateFlow()

    // Active Chat Thread
    private val _activeThread = MutableStateFlow<ChatThreadEntity?>(null)
    val activeThread: StateFlow<ChatThreadEntity?> = _activeThread.asStateFlow()

    val activeThreadMessages: StateFlow<List<MessageEntity>> = _activeThread.flatMapLatest { thread ->
        if (thread != null) repository.getMessagesForThread(thread.threadId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Create post draft
    private val _createDraft = MutableStateFlow(CreatePostDraft())
    val createDraft: StateFlow<CreatePostDraft> = _createDraft.asStateFlow()

    // Status snackbar
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    fun setFeedFilter(filter: FeedFilterType) {
        _feedFilter.value = filter
    }

    fun setBreedFilter(breed: String) {
        _breedFilter.value = breed
    }

    fun openStory(story: StoryEntity) {
        _activeStory.value = story
        viewModelScope.launch {
            repository.markStoryViewed(story.id)
        }
    }

    fun closeStory() {
        _activeStory.value = null
    }

    fun openComments(post: PostEntity) {
        _activeCommentsPost.value = post
    }

    fun closeComments() {
        _activeCommentsPost.value = null
    }

    fun addComment(text: String) {
        val post = _activeCommentsPost.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(post.id, text.trim())
            _snackbarMessage.emit("Heartwarming comment shared! 🐾")
        }
    }

    fun toggleLikePost(post: PostEntity) {
        viewModelScope.launch {
            repository.toggleLikePost(post)
        }
    }

    fun toggleLikeReel(reel: ReelEntity) {
        viewModelScope.launch {
            repository.toggleLikeReel(reel)
        }
    }

    fun showAlgorithmInsights(post: PostEntity) {
        viewModelScope.launch {
            val breakdown = repository.getBreakdownForPost(post)
            _algorithmBreakdown.value = Pair(post, breakdown)
        }
    }

    fun dismissAlgorithmInsights() {
        _algorithmBreakdown.value = null
    }

    // Direct Messaging
    fun openChat(thread: ChatThreadEntity) {
        _activeThread.value = thread
        viewModelScope.launch {
            repository.markThreadRead(thread.threadId)
        }
    }

    fun closeChat() {
        _activeThread.value = null
    }

    fun sendMessage(text: String, stickerType: String = "NONE") {
        val thread = _activeThread.value ?: return
        if (text.isBlank() && stickerType == "NONE") return
        viewModelScope.launch {
            repository.sendMessage(thread.threadId, thread.participantId, text.trim(), stickerType)
        }
    }

    // Photo Editing & Post Creation
    fun updateDraftPhoto(uri: String) {
        _createDraft.value = _createDraft.value.copy(photoUri = uri)
    }

    fun updateDraftFilter(filter: String) {
        _createDraft.value = _createDraft.value.copy(filterName = filter)
    }

    fun updateDraftBrightness(brightness: Float) {
        _createDraft.value = _createDraft.value.copy(brightness = brightness)
    }

    fun updateDraftContrast(contrast: Float) {
        _createDraft.value = _createDraft.value.copy(contrast = contrast)
    }

    fun updateDraftCaption(caption: String) {
        _createDraft.value = _createDraft.value.copy(caption = caption)
    }

    fun toggleDraftTag(tag: String) {
        val current = _createDraft.value.selectedTags.toMutableList()
        if (current.contains(tag)) {
            current.remove(tag)
        } else {
            current.add(tag)
        }
        _createDraft.value = _createDraft.value.copy(selectedTags = current)
    }

    fun updateDraftMood(mood: String) {
        _createDraft.value = _createDraft.value.copy(mood = mood)
    }

    fun publishPost(onSuccess: () -> Unit) {
        val draft = _createDraft.value
        if (draft.caption.isBlank()) {
            viewModelScope.launch {
                _snackbarMessage.emit("Please write a sweet caption for your plushie companion! 🐾")
            }
            return
        }
        viewModelScope.launch {
            val tagsStr = draft.selectedTags.joinToString(", ")
            repository.createPost(
                caption = draft.caption,
                photoUri = draft.photoUri,
                filterName = draft.filterName,
                brightness = draft.brightness,
                contrast = draft.contrast,
                tags = tagsStr,
                mood = draft.mood,
                soundAlbumTitle = draft.soundAlbumTitle,
                soundArtistName = draft.soundArtistName,
                soundAudioUri = draft.soundAudioUri
            )
            _createDraft.value = CreatePostDraft() // reset
            _snackbarMessage.emit("Plushie moment published to community feed! 🧸✨")
            onSuccess()
        }
    }

    fun selectPostSoundAlbum(album: ArtistAlbumEntity?) {
        _createDraft.value = _createDraft.value.copy(
            soundAlbumTitle = album?.title ?: "",
            soundArtistName = album?.artistName ?: "",
            soundAudioUri = album?.audioUri ?: ""
        )
    }

    fun uploadArtistAlbum(
        title: String,
        description: String,
        genre: String,
        audioUri: String,
        coverUri: String,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || audioUri.isBlank()) {
            viewModelScope.launch {
                _snackbarMessage.emit("Please enter album title and select audio track 🎵")
            }
            return
        }
        viewModelScope.launch {
            val res = repository.uploadArtistAlbum(title.trim(), description, genre, audioUri, coverUri)
            if (res.isSuccess) {
                _snackbarMessage.emit("🎵 Music Album '$title' published to sound catalog!")
                onSuccess()
            } else {
                _snackbarMessage.emit(res.exceptionOrNull()?.message ?: "Upload failed")
            }
        }
    }

    fun deleteArtistAlbum(albumId: Long) {
        viewModelScope.launch {
            repository.deleteArtistAlbum(albumId)
            _snackbarMessage.emit("Album deleted from catalog 🗑️")
        }
    }

    fun publishStory(caption: String, photoUri: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.createStory(caption, photoUri)
            _snackbarMessage.emit("New 24h plushie story shared! 🌟")
            onSuccess()
        }
    }

    fun publishReel(caption: String, photoUri: String, audioTrack: String, tags: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.createReel(caption, photoUri, audioTrack, tags)
            _snackbarMessage.emit("Plushie Reel published to video stream! 🎬✨")
            onSuccess()
        }
    }

    // Profile Management
    fun updateProfile(
        displayName: String,
        plushieName: String,
        breed: String,
        bio: String,
        comfortRole: String,
        avatarUri: String
    ) {
        viewModelScope.launch {
            repository.updateProfile(
                displayName = displayName,
                plushieName = plushieName,
                plushieBreed = breed,
                bio = bio,
                comfortRole = comfortRole,
                avatarUri = avatarUri
            )
            _snackbarMessage.emit("Profile & plushie companion details updated! 🐶")
        }
    }

    fun applyVerificationBadge(badgeType: String) {
        viewModelScope.launch {
            repository.applyForVerification(badgeType)
            _snackbarMessage.emit("🎉 Authenticity Badge Granted: Verified Emotional Support Companion!")
        }
    }

    fun switchGoogleAccount(accountName: String, email: String, plushieName: String, breed: String) {
        viewModelScope.launch {
            repository.switchGoogleAccount(accountName, email, plushieName, breed)
            _snackbarMessage.emit("Signed in with Google as $email 🐾")
        }
    }
}
