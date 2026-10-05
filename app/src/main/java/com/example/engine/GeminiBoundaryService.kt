package com.example.engine

import com.example.BuildConfig
import com.example.model.BoundaryScoreDetails
import com.example.model.SpeechBoundary
import com.example.model.SpeechSegment
import com.example.model.SpeechWord
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

object GeminiBoundaryService {

  private val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
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
      4. DO NOT split after every word or at fixed time intervals.
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

      val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
      val request = Request.Builder()
        .url(url)
        .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      if (!response.isSuccessful) {
        return@withContext null
      }

      val responseString = response.body?.string() ?: return@withContext null
      val jsonResponse = JSONObject(responseString)
      val candidates = jsonResponse.optJSONArray("candidates") ?: return@withContext null
      if (candidates.length() == 0) return@withContext null

      val content = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext null
      val parts = content.optJSONArray("parts") ?: return@withContext null
      if (parts.length() == 0) return@withContext null

      val text = parts.getJSONObject(0).optString("text")
      val jsonArray = JSONArray(text)
      val boundaries = mutableListOf<SpeechBoundary>()

      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val time = obj.optDouble("time", -1.0)
        if (time <= 0.0 || time >= videoDuration) continue

        val confidence = obj.optDouble("confidence", 0.90).coerceIn(0.1, 1.0)
        val reason = obj.optString("reason", "Semantic phrase boundary")
        val pauseScore = obj.optDouble("pauseScore", 0.90)
        val punctScore = obj.optDouble("punctuationScore", 0.95)
        val semScore = obj.optDouble("semanticCompletionScore", 0.95)
        val rhythmScore = obj.optDouble("rhythmScore", 0.88)
        val speechScore = obj.optDouble("speechBoundaryScore", 0.92)

        val frame = (time * fps).roundToLong()
        boundaries.add(
          SpeechBoundary(
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
            isApproved = confidence >= 0.70
          )
        )
      }

      boundaries.sortedBy { it.time }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}
