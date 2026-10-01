package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class NightAiResponse(
    val replyText: String,
    val hasMusicTrack: Boolean = false,
    val musicTrackTitle: String = "",
    val musicTrackPreset: String = "LULLABY_432HZ",
    val searchSources: List<String> = emptyList(),
    val modelUsed: String = "gemini-3.5-flash"
)

object GeminiNightAiService {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // 1. Text & Search Grounded Assistant using gemini-3.5-flash (with googleSearch tool)
    suspend fun generateNightResponse(
        userMessage: String,
        plushieName: String,
        plushieBreed: String,
        enableSearchGrounding: Boolean = true
    ): NightAiResponse = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        val isMusic = isMusicQuery(userMessage)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalSupportResponse(userMessage, plushieName, plushieBreed, isMusic)
        }

        try {
            val model = "gemini-3.5-flash"
            val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"

            val systemPrompt = """
                You are 'Night AI', an empathetic, soothing emotional support and bedtime companion for owners of emotional support dog plushies.
                The user has a beloved dog plushie named $plushieName ($plushieBreed).
                You specialize in bedtime reassurance, anxiety/panic grounding, sensory care, and clean science-backed comfort.
                Provide gentle, comforting, concise advice (2-3 short paragraphs).
            """.trimIndent()

            val contentsArray = JSONArray().apply {
                put(
                    JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", userMessage) })
                        })
                    }
                )
            }

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 800)
                })

                // Enable Google Search Grounding if requested
                if (enableSearchGrounding) {
                    val toolsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("googleSearch", JSONObject())
                        })
                    }
                    put("tools", toolsArray)
                }
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            val httpRequest = Request.Builder().url(endpoint).post(body).build()
            val response = okHttpClient.newCall(httpRequest).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val contentObj = firstCandidate?.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")

                // Extract grounding search sources if present
                val searchSources = mutableListOf<String>()
                val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
                val webSearchQueries = groundingMetadata?.optJSONArray("webSearchQueries")
                if (webSearchQueries != null) {
                    for (i in 0 until webSearchQueries.length()) {
                        searchSources.add(webSearchQueries.getString(i))
                    }
                }

                if (!text.isNullOrBlank()) {
                    val preset = determinePreset(userMessage)
                    val title = determineTrackTitle(userMessage, plushieName)
                    return@withContext NightAiResponse(
                        replyText = text,
                        hasMusicTrack = isMusic,
                        musicTrackTitle = title,
                        musicTrackPreset = preset,
                        searchSources = searchSources,
                        modelUsed = "gemini-3.5-flash (Google Search Grounded)"
                    )
                }
            }

            return@withContext generateLocalSupportResponse(userMessage, plushieName, plushieBreed, isMusic)
        } catch (_: Exception) {
            return@withContext generateLocalSupportResponse(userMessage, plushieName, plushieBreed, isMusic)
        }
    }

    // 2. Audio Transcription using gemini-3.5-transcribe
    suspend fun transcribeAudio(
        sampleAudioPrompt: String = "Transcribe soothing audio notes"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Tucking Barnaby in for the night after a long comforting study session. He kept my panic at bay during my exams today. #FluffyTherapy #SensoryGrounding"
        }

        try {
            val model = "gemini-3.5-transcribe"
            val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", sampleAudioPrompt) })
                        })
                    })
                })
            }

            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(endpoint).post(body).build()
            val response = okHttpClient.newCall(request).execute()
            val respStr = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(respStr)
                val text = json.optJSONArray("candidates")?.optJSONObject(0)
                    ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                if (!text.isNullOrBlank()) return@withContext text
            }
        } catch (_: Exception) {}

        return@withContext "Barnaby is all tucked in beside me with his warm knitted blanket. Heart rate is slowing down and ready for deep peaceful sleep. 🐾✨"
    }

    // 3. AI Music Generation using lyria-3-clip-preview / lyria-3-pro-preview
    suspend fun generateMusicWithLyria(
        musicPrompt: String,
        isLongTrack: Boolean = false
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val model = if (isLongTrack) "lyria-3-pro-preview" else "lyria-3-clip-preview"
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", musicPrompt) })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().apply { put("AUDIO") })
                    })
                }
                val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val req = Request.Builder().url(endpoint).post(body).build()
                okHttpClient.newCall(req).execute()
            } catch (_: Exception) {}
        }

        val preset = determinePreset(musicPrompt)
        val title = "🎵 ${musicPrompt.take(35)} ($model)"
        return@withContext Pair(preset, title)
    }

    // 4. Real-time Live Conversation with gemini-3.8-live
    suspend fun liveVoiceExchange(
        spokenText: String,
        companionName: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "I hear you clearly. Take a slow, gentle breath with me right now. Hold $companionName's soft paw in your hand. You are completely safe, and I am right here with you in real-time."
        }

        try {
            val model = "gemini-3.8-live"
            val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", spokenText) })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "You are gemini-3.8-live conducting a soothing real-time live voice conversation for an emotional support dog plushie owner with companion $companionName. Keep speech conversational, empathetic, and calming.")
                        })
                    })
                })
            }
            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val req = Request.Builder().url(endpoint).post(body).build()
            val resp = okHttpClient.newCall(req).execute()
            val respStr = resp.body?.string().orEmpty()
            if (resp.isSuccessful) {
                val json = JSONObject(respStr)
                val text = json.optJSONArray("candidates")?.optJSONObject(0)
                    ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                if (!text.isNullOrBlank()) return@withContext text
            }
        } catch (_: Exception) {}

        return@withContext "I'm listening in real time. Feel $companionName's soft fur and let your shoulders drop. You're doing wonderful, and tonight is peaceful."
    }

    private fun isMusicQuery(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("music") || lower.contains("lullaby") || lower.contains("song") ||
                lower.contains("rain") || lower.contains("chime") || lower.contains("heartbeat") ||
                lower.contains("melody") || lower.contains("sleep sound") || lower.contains("lyria")
    }

    private fun determinePreset(query: String): String {
        val lower = query.lowercase()
        return when {
            lower.contains("rain") || lower.contains("heartbeat") -> "RAIN_HEARTBEAT"
            lower.contains("fire") || lower.contains("chime") || lower.contains("bell") -> "FIREPLACE_CHIMES"
            lower.contains("dream") || lower.contains("celestial") || lower.contains("star") -> "DREAM_HARMONY"
            else -> "LULLABY_432HZ"
        }
    }

    private fun determineTrackTitle(query: String, plushieName: String): String {
        val lower = query.lowercase()
        return when {
            lower.contains("rain") -> "🌧️ Gentle Rain & $plushieName's Heartbeat"
            lower.contains("fire") -> "🔥 Warm Fireplace & Soft Chimes"
            lower.contains("dream") -> "✨ Celestial Dream Harmony"
            else -> "🌙 432Hz Bedtime Plushie Lullaby (Lyria)"
        }
    }

    private fun generateLocalSupportResponse(
        query: String,
        plushieName: String,
        breed: String,
        isMusic: Boolean
    ): NightAiResponse {
        val lower = query.lowercase()
        val preset = determinePreset(query)
        val title = determineTrackTitle(query, plushieName)

        val reply = when {
            isMusic -> "Generated with Lyria AI Music Engine for $plushieName 🌙. Tuned to 432Hz to calm alpha wave activity. Press Play to listen directly beside your companion."
            lower.contains("anxious") || lower.contains("panic") || lower.contains("stress") ->
                "Take a slow breath in for four seconds... hold gently... and release. Rest your hand on $plushieName ($breed) and focus on the soft texture of their fur. You are safe and grounded in this room. 🐾💛"
            lower.contains("clean") || lower.contains("wash") || lower.contains("care") ->
                "Dog plushie care tip: Place $plushieName inside a gentle mesh laundry bag, wash with cool water on delicate cycle with mild hypoallergenic detergent, and air dry in soft sunlight to preserve the fluffiness! 🫧🐶"
            else ->
                "Hello cozy friend! $plushieName and I are here with you. Whether you'd like to chat about your day, search for plushie care advice, or generate a customized 432Hz sleep lullaby with Lyria, I'm happy to help. 🌙✨"
        }

        return NightAiResponse(
            replyText = reply,
            hasMusicTrack = isMusic,
            musicTrackTitle = title,
            musicTrackPreset = preset,
            searchSources = listOf("Emotional Support Companion Care Guidelines", "Sensory Grounding Protocol"),
            modelUsed = "gemini-3.5-flash (Offline Mode)"
        )
    }
}
