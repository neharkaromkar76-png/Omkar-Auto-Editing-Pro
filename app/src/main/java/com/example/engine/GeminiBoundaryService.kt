package com.example.engine

import android.util.Log
import com.example.BuildConfig
import com.example.model.BoundaryScoreDetails
import com.example.model.SpeechBoundary
import com.example.model.SpeechWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

object GeminiBoundaryService {

  private const val TAG = "GeminiBoundaryService"

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .writeTimeout(15, TimeUnit.SECONDS)
    .build()

  suspend fun analyzeBoundariesWithGemini(
    transcript: String,
    words: List<SpeechWord>,
    videoDuration: Double,
    fps: Double,
    customApiKey: String? = null
  ): List<SpeechBoundary>? = withContext(Dispatchers.IO) {
    val apiKey = when {
      !customApiKey.isNullOrBlank() -> customApiKey.trim()
      BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
      else -> return@withContext null
    }

    val prompt = """
      You are a world-class professional film and vertical video editor specializing in dynamic speech-driven zoom cuts.
      Below is the transcribed speech with word timestamps for a video of duration $videoDuration seconds.
      
      TASK:
      Determine the EXACT timestamps where a professional human video editor would create a split to insert a 0.70x wide-frame zoom-in cut.
      
      RULES:
      1. Create a boundary ONLY when the speaker has naturally completed a sentence or meaningful phrase.
      2. Analyze: sentence completion, phrase completion, punctuation, pauses, speaker cadence, and thought conclusion.
      3. NEVER split in the middle of a sentence, middle of a word, or immediately after a filler word ('uh', 'um', 'like').
      4. DO NOT use fixed timestamps (e.g. 2s, 3s, 5s). Timestamps must be based on the actual speech word times provided.
      5. Closely spaced boundaries (e.g. 0.5s - 1.5s apart) ARE PERMITTED when natural speech cadence requires it.
      6. Provide scores (0.0 to 1.0) for: pauseScore, punctuationScore, semanticCompletionScore, rhythmScore, speechBoundaryScore, and combined boundaryConfidence.
      
      TRANSCRIPT WITH WORD TIMESTAMPS:
      ${words.take(120).joinToString("\n") { "[${String.format("%.3f", it.startTime)}s - ${String.format("%.3f", it.endTime)}s]: ${it.word}" }}
      
      Respond strictly with a JSON array of objects with fields:
      - "time": (number, exact timestamp in seconds)
      - "confidence": (number between 0.0 and 1.0)
      - "reason": (short explanation of sentence completion)
      - "pauseScore": (number)
      - "punctuationScore": (number)
      - "semanticCompletionScore": (number)
      - "rhythmScore": (number)
      - "speechBoundaryScore": (number)
    """.trimIndent()

    val models = listOf("gemini-3.5-flash", "gemini-3.1-flash-lite-preview")

    for (model in models) {
      try {
        val requestBodyJson = JSONObject().apply {
          put("contents", JSONArray().apply {
            put(JSONObject().apply {
              put("parts", JSONArray().apply {
                put(JSONObject().apply {
                  put("text", prompt)
                })
              })
            })
          })
          put("generationConfig", JSONObject().apply {
            put("responseMimeType", "application/json")
            put("temperature", 0.2)
          })
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val request = Request.Builder()
          .url(url)
          .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
          .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
          Log.w(TAG, "Gemini model $model returned HTTP ${response.code}")
          continue
        }

        val responseString = response.body?.string() ?: continue
        val jsonResponse = JSONObject(responseString)
        val candidates = jsonResponse.optJSONArray("candidates") ?: continue
        if (candidates.length() == 0) continue

        val content = candidates.getJSONObject(0).optJSONObject("content") ?: continue
        val parts = content.optJSONArray("parts") ?: continue
        if (parts.length() == 0) continue

        val text = parts.getJSONObject(0).optString("text")
        val jsonArray = JSONArray(text)
        val boundaries = mutableListOf<SpeechBoundary>()

        for (i in 0 until jsonArray.length()) {
          val obj = jsonArray.getJSONObject(i)
          val time = obj.optDouble("time", -1.0)
          if (time <= 0.35 || time >= videoDuration - 0.35 || time.isNaN() || time.isInfinite()) continue

          val confidence = obj.optDouble("confidence", 0.90).coerceIn(0.1, 1.0)
          val reason = obj.optString("reason", "Semantic sentence completion")
          val pauseScore = obj.optDouble("pauseScore", 0.90)
          val punctScore = obj.optDouble("punctuationScore", 0.95)
          val semScore = obj.optDouble("semanticCompletionScore", 0.95)
          val rhythmScore = obj.optDouble("rhythmScore", 0.88)
          val speechScore = obj.optDouble("speechBoundaryScore", 0.92)

          val frame = (time * fps).roundToLong()
          boundaries.add(
            SpeechBoundary(
              id = UUID.randomUUID().toString(),
              time = time,
              frame = frame,
              boundaryConfidence = confidence,
              scoreDetails = BoundaryScoreDetails(
                pauseScore = pauseScore,
                punctuationScore = punctScore,
                semanticCompletionScore = semScore,
                rhythmScore = rhythmScore,
                speechBoundaryScore = speechScore
              ),
              reason = reason,
              isApproved = true // All parsed boundaries are approved for editing events
            )
          )
        }

        if (boundaries.isNotEmpty()) {
          Log.i(TAG, "Gemini model $model successfully returned ${boundaries.size} semantic boundaries")
          return@withContext boundaries.sortedBy { it.time }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error invoking Gemini $model: ${e.message}")
      }
    }

    null
  }
}
