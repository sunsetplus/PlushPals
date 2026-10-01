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

data class PlushAiResponse(
    val replyText: String,
    val hasMusicTrack: Boolean = false,
    val musicTrackTitle: String = "",
    val musicTrackPreset: String = "LULLABY_432HZ",
    val searchSources: List<String> = emptyList(),
    val modelUsed: String = "gemini-3.5-flash"
)

object PlushAiService {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // 1. Conversational Chat powered by Gemini 3.5 Flash
    suspend fun generatePlushAiResponse(
        userMessage: String,
        companionName: String,
        companionBreed: String,
        enableSearchGrounding: Boolean = true
    ): PlushAiResponse = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        val isMusic = isMusicQuery(userMessage)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalPlushAiResponse(userMessage, companionName, companionBreed, isMusic)
        }

        try {
            val model = "gemini-3.5-flash"
            val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"

            val systemPrompt = """
                You are 'Plush AI', the gentle, comforting AI companion powered by Google Gemini for people with emotional support dog plushies.
                The user has a beloved dog plushie named $companionName ($companionBreed).
                You specialize in deep sensory grounding, bedtime relaxation, anxiety/panic relief, positive self-talk, and plushie care.
                Keep responses warm, compassionate, supportive, and concise (2-3 short, relaxing paragraphs).
                If the user asks for sleep music, bedtime lullaby, or soothing sounds, acknowledge that a calming frequency has been prepared for them.
            """.trimIndent()

            val contentsArray = JSONArray().apply {
                put(
                    JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", userMessage))
                        })
                    }
                )
            }

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 600)
                })

                if (enableSearchGrounding) {
                    put("tools", JSONArray().apply {
                        put(JSONObject().apply {
                            put("googleSearch", JSONObject())
                        })
                    })
                }
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (response.isSuccessful && responseString.isNotBlank()) {
                val json = JSONObject(responseString)
                val candidate = json.optJSONArray("candidates")?.optJSONObject(0)
                val contentObj = candidate?.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")

                val replyBuilder = StringBuilder()
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.optJSONObject(i)
                        val text = part?.optString("text")
                        if (!text.isNullOrBlank()) {
                            replyBuilder.append(text)
                        }
                    }
                }

                // Search Grounding Citations
                val sourcesList = mutableListOf<String>()
                val groundingMetadata = candidate?.optJSONObject("groundingMetadata")
                val searchChunks = groundingMetadata?.optJSONArray("groundingChunks")
                if (searchChunks != null) {
                    for (j in 0 until searchChunks.length()) {
                        val webObj = searchChunks.optJSONObject(j)?.optJSONObject("web")
                        val uri = webObj?.optString("uri")
                        val title = webObj?.optString("title")
                        if (!uri.isNullOrBlank()) {
                            sourcesList.add(if (!title.isNullOrBlank()) "$title ($uri)" else uri)
                        }
                    }
                }

                val finalReply = if (replyBuilder.isNotBlank()) {
                    replyBuilder.toString().trim()
                } else {
                    "Plush AI is here with you and $companionName. Breathe deeply, feel your plushie's soft fur, and rest peacefully tonight. 🐾🌙"
                }

                val preset = when {
                    userMessage.contains("rain", true) -> "RAIN_HEARTBEAT"
                    userMessage.contains("chim", true) -> "FIREPLACE_CHIMES"
                    userMessage.contains("dream", true) -> "DREAM_HARMONY"
                    else -> "LULLABY_432HZ"
                }

                PlushAiResponse(
                    replyText = finalReply,
                    hasMusicTrack = isMusic,
                    musicTrackTitle = if (isMusic) "432Hz Serene Bedtime Frequency for $companionName" else "",
                    musicTrackPreset = preset,
                    searchSources = sourcesList,
                    modelUsed = "gemini-3.5-flash"
                )
            } else {
                generateLocalPlushAiResponse(userMessage, companionName, companionBreed, isMusic)
            }
        } catch (_: Exception) {
            generateLocalPlushAiResponse(userMessage, companionName, companionBreed, isMusic)
        }
    }

    // 2. AI Caption Helper for Companion Moments (powered by Gemini 3.5 Flash)
    suspend fun generateCaptionWithPlushAi(
        companionName: String,
        companionBreed: String,
        mood: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Spending gentle quiet moments with my sweet $companionName the $companionBreed. Grounded, loved, and snug. 🐾💛 #EmotionalSupportPlushie #ComfortDog"
        }

        try {
            val endpoint = "$BASE_URL/gemini-3.5-flash:generateContent?key=$apiKey"
            val prompt = "Write a short, heartwarming caption (1-2 sentences) and 2-3 hashtags for a photo of an emotional support plush dog named $companionName ($companionBreed), feeling $mood. No quotes."

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(endpoint).post(body).build()
            val response = okHttpClient.newCall(request).execute()
            val resStr = response.body?.string().orEmpty()

            if (response.isSuccessful && resStr.isNotBlank()) {
                val candidate = JSONObject(resStr).optJSONArray("candidates")?.optJSONObject(0)
                val text = candidate?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            }
            "Spending gentle quiet moments with my sweet $companionName the $companionBreed. Grounded, loved, and snug. 🐾💛 #EmotionalSupportPlushie #ComfortDog"
        } catch (_: Exception) {
            "Spending gentle quiet moments with my sweet $companionName the $companionBreed. Grounded, loved, and snug. 🐾💛 #EmotionalSupportPlushie #ComfortDog"
        }
    }

    private fun isMusicQuery(msg: String): Boolean {
        val lower = msg.lowercase()
        return lower.contains("music") || lower.contains("lullaby") ||
                lower.contains("sound") || lower.contains("song") ||
                lower.contains("sleep") || lower.contains("432hz") ||
                lower.contains("binaural") || lower.contains("frequency")
    }

    private fun generateLocalPlushAiResponse(
        userMessage: String,
        companionName: String,
        companionBreed: String,
        isMusic: Boolean
    ): PlushAiResponse {
        val lower = userMessage.lowercase()
        val reply = when {
            lower.contains("anxious") || lower.contains("panic") || lower.contains("scared") || lower.contains("overwhelm") -> {
                "Take a slow breath in for 4 seconds, hold for 4, and release for 6. Press $companionName gently against your chest. Feel the soft fabric under your fingertips. You are safe in this moment, and your fluffy companion is right here anchoring you. 💛🐾"
            }
            lower.contains("sleep") || lower.contains("insomnia") || lower.contains("bedtime") || isMusic -> {
                "Softening into sleep is about letting go of today's noise. Place $companionName by your pillow, let your shoulders drop from your ears, and breathe with the gentle rhythm of the night. Sleep gently; tomorrow is a fresh page. 🌙💤"
            }
            lower.contains("who are you") || lower.contains("plush ai") || lower.contains("gemini") -> {
                "I am Plush AI, your empathetic companion powered by Google Gemini! I'm here to support you and $companionName with anxiety relief, calming bedtime lullabies, sensory grounding, and unconditional warmth. 🧸✨"
            }
            else -> {
                "I hear you. Whatever thoughts are running through your mind, you don't have to carry them alone. Wrap both hands around $companionName's paws, feel the grounding weight of your plushie, and give yourself grace. You are doing so much better than you realize. 🐶💛"
            }
        }

        return PlushAiResponse(
            replyText = reply,
            hasMusicTrack = isMusic,
            musicTrackTitle = if (isMusic) "432Hz Serene Bedtime Frequency for $companionName" else "",
            musicTrackPreset = "LULLABY_432HZ",
            modelUsed = "gemini-3.5-flash (Plush AI)"
        )
    }
}
