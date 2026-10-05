package com.example.model

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class SpeechWord(
  val word: String,
  val startTime: Double,
  val endTime: Double,
  val confidence: Double = 0.95
)

@JsonClass(generateAdapter = true)
data class SpeechSegment(
  val id: String = UUID.randomUUID().toString(),
  val text: String,
  val startTime: Double,
  val endTime: Double,
  val words: List<SpeechWord> = emptyList(),
  val confidence: Double = 0.95,
  val speakerId: String? = null
)

@JsonClass(generateAdapter = true)
data class BoundaryScoreDetails(
  val pauseScore: Double = 0.9,
  val punctuationScore: Double = 0.9,
  val semanticCompletionScore: Double = 0.9,
  val rhythmScore: Double = 0.85,
  val speechBoundaryScore: Double = 0.9
)

@JsonClass(generateAdapter = true)
data class SpeechBoundary(
  val id: String = UUID.randomUUID().toString(),
  val time: Double,
  val frame: Long,
  val boundaryConfidence: Double,
  val scoreDetails: BoundaryScoreDetails = BoundaryScoreDetails(),
  val reason: String = "Natural sentence completion",
  val isApproved: Boolean = true
)
