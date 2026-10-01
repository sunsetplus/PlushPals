package com.example.ai

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.*

object PlushieAudioSynthesizer {

    private const val SAMPLE_RATE = 22050
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var synthesisJob: Job? = null
    private var currentPreset: String? = null

    fun isPlayingTrack(preset: String): Boolean {
        return isPlaying && currentPreset == preset
    }

    fun isAnyPlaying(): Boolean {
        return isPlaying
    }

    fun getCurrentPlayingPreset(): String? = currentPreset

    @Synchronized
    fun stop() {
        isPlaying = false
        currentPreset = null
        synthesisJob?.cancel()
        synthesisJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    fun playPreset(preset: String, onStateChanged: () -> Unit) {
        if (isPlaying && currentPreset == preset) {
            stop()
            onStateChanged()
            return
        }

        stop()
        isPlaying = true
        currentPreset = preset
        onStateChanged()

        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = max(minBufferSize, SAMPLE_RATE * 2)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        synthesisJob = CoroutineScope(Dispatchers.Default).launch {
            val chunkSamples = SAMPLE_RATE / 4 // 250ms chunks
            val shortBuffer = ShortArray(chunkSamples)
            var sampleIndex: Long = 0

            // Pentatonic frequencies around 432 Hz: A4(432), C#5(544.29), E5(647.27), F#5(726.53), B4(484.9)
            val notes = doubleArrayOf(216.0, 272.14, 323.63, 432.0, 544.29, 647.27)

            while (isActive && isPlaying) {
                for (i in 0 until chunkSamples) {
                    val t = (sampleIndex + i).toDouble() / SAMPLE_RATE.toDouble()
                    var sampleVal = 0.0

                    when (preset) {
                        "RAIN_HEARTBEAT" -> {
                            // Rain noise + rhythmic 60 BPM heartbeat (1 Hz)
                            val rainNoise = (Math.random() * 2.0 - 1.0) * 0.12
                            // Heartbeat: lub-dub pulse every 1.0 second
                            val cycleT = t % 1.0
                            var heartVal = 0.0
                            if (cycleT < 0.12) {
                                val heartPhase = cycleT / 0.12 * Math.PI
                                heartVal += sin(2.0 * Math.PI * 48.0 * t) * sin(heartPhase) * 0.6
                            } else if (cycleT in 0.22..0.32) {
                                val heartPhase = (cycleT - 0.22) / 0.10 * Math.PI
                                heartVal += sin(2.0 * Math.PI * 52.0 * t) * sin(heartPhase) * 0.45
                            }
                            sampleVal = rainNoise + heartVal
                        }
                        "FIREPLACE_CHIMES" -> {
                            // Soft warm drone + slow chime melody
                            val drone = (sin(2.0 * Math.PI * 108.0 * t) + sin(2.0 * Math.PI * 162.0 * t) * 0.5) * 0.25
                            // Chime note changing every 2.5 seconds
                            val noteIdx = ((t / 2.5).toInt()) % notes.size
                            val freq = notes[noteIdx]
                            val chimePhase = (t % 2.5) / 2.5
                            val chimeEnv = max(0.0, (1.0 - chimePhase) * exp(-chimePhase * 2.5))
                            val chime = sin(2.0 * Math.PI * freq * t) * chimeEnv * 0.45
                            sampleVal = drone + chime
                        }
                        "DREAM_HARMONY" -> {
                            // Swelling celestial warm pads: 432Hz & 648Hz with gentle LFO tremolo
                            val lfo = (sin(2.0 * Math.PI * 0.15 * t) + 1.0) * 0.5
                            val pad1 = sin(2.0 * Math.PI * 216.0 * t) * 0.3
                            val pad2 = sin(2.0 * Math.PI * 324.0 * t) * 0.25 * lfo
                            val pad3 = sin(2.0 * Math.PI * 432.0 * t) * 0.2 * (1.0 - lfo)
                            sampleVal = pad1 + pad2 + pad3
                        }
                        else -> { // "LULLABY_432HZ"
                            // 432Hz sleep chord with breathing swell
                            val breathLfo = (sin(2.0 * Math.PI * 0.12 * t) + 1.0) * 0.5 // 7.2 breaths/min
                            val fundamental = sin(2.0 * Math.PI * 432.0 * t) * 0.35
                            val subHarmonic = sin(2.0 * Math.PI * 216.0 * t) * 0.4
                            val fifth = sin(2.0 * Math.PI * 648.0 * t) * 0.15
                            sampleVal = (fundamental + subHarmonic + fifth) * (0.6 + 0.4 * breathLfo)
                        }
                    }

                    // Soft limiter
                    val clamped = max(-1.0, min(1.0, sampleVal))
                    shortBuffer[i] = (clamped * Short.MAX_VALUE * 0.85).toInt().toShort()
                }

                sampleIndex += chunkSamples
                audioTrack?.write(shortBuffer, 0, chunkSamples)
            }
        }
    }
}
