package com.example.ai

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PlushieMp3Player {

    private var mediaPlayer: MediaPlayer? = null
    private val _currentlyPlayingTrackId = MutableStateFlow<Long?>(null)
    val currentlyPlayingTrackId: StateFlow<Long?> = _currentlyPlayingTrackId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun playTrack(context: Context, trackId: Long, uriString: String, onCompletion: () -> Unit = {}) {
        if (_currentlyPlayingTrackId.value == trackId && _isPlaying.value) {
            pause()
            return
        }

        if (_currentlyPlayingTrackId.value == trackId && mediaPlayer != null) {
            mediaPlayer?.start()
            _isPlaying.value = true
            return
        }

        stop()

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.parse(uriString))
                prepareAsync()
                setOnPreparedListener { mp ->
                    mp.start()
                    _currentlyPlayingTrackId.value = trackId
                    _isPlaying.value = true
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentlyPlayingTrackId.value = null
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    _currentlyPlayingTrackId.value = null
                    true
                }
            }
            mediaPlayer = player
        } catch (_: Exception) {
            _isPlaying.value = false
            _currentlyPlayingTrackId.value = null
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
            _isPlaying.value = false
        } catch (_: Exception) {}
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
        _currentlyPlayingTrackId.value = null
    }

    fun isTrackPlaying(trackId: Long): Boolean {
        return _currentlyPlayingTrackId.value == trackId && _isPlaying.value
    }
}
