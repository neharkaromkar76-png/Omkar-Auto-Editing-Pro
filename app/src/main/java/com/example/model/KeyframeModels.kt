package com.example.model

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class ZoomKeyframe(
  val offsetFrames: Int = 0,
  val frame: Long,
  val scale: Double,
  val timestamp: Double = 0.0,
  val positionX: Double = 0.50,
  val positionY: Double = 0.50,
  val interpolation: String = "cubic-ease-out"
)

@JsonClass(generateAdapter = true)
data class ZoomEvent(
  val id: String = UUID.randomUUID().toString(),
  val boundaryFrame: Long,
  val boundaryTime: Double,
  val normalScale: Double = 1.00,
  val wideScale: Double = 0.70,
  val durationFrames: Int = 10,
  val positionX: Double = 0.50,
  val positionY: Double = 0.50,
  val keyframes: List<ZoomKeyframe> = emptyList(),
  val interpolation: String = "cubic-ease-out",
  val confidence: Double = 0.95
)

@JsonClass(generateAdapter = true)
data class ReferenceStyleProfile(
  val fps: Double = 24.02421,
  val normalScale: Double = 1.00,
  val wideScale: Double = 0.70,
  val keyframeOffsets: List<Int> = listOf(-1, 0, 2, 4, 6, 8, 10),
  val scales: List<Double> = listOf(1.00, 0.70, 0.75, 0.82, 0.90, 0.96, 1.00),
  val interpolation: String = "cubic-ease-out",
  val centerX: Double = 0.50,
  val centerY: Double = 0.50
)

@JsonClass(generateAdapter = true)
data class SourceTimelineInfo(
  val duration: Double,
  val fps: Double,
  val width: Int,
  val height: Int
)

@JsonClass(generateAdapter = true)
data class BoundaryTimelineItem(
  val time: Double,
  val frame: Long,
  val confidence: Double
)

@JsonClass(generateAdapter = true)
data class ZoomEventTimelineItem(
  val boundaryFrame: Long,
  val keyframes: List<ZoomKeyframe>,
  val interpolation: String = "cubic-ease-out"
)

@JsonClass(generateAdapter = true)
data class EditTimelineJson(
  val source: SourceTimelineInfo,
  val boundaries: List<BoundaryTimelineItem>,
  val zoomEvents: List<ZoomEventTimelineItem>
)

@JsonClass(generateAdapter = true)
data class EditTimeline(
  val source: MediaMetadata,
  val boundaries: List<SpeechBoundary> = emptyList(),
  val zoomEvents: List<ZoomEvent> = emptyList(),
  val segments: List<SpeechSegment> = emptyList(),
  val styleProfile: ReferenceStyleProfile = ReferenceStyleProfile(),
  val createdAt: Long = System.currentTimeMillis()
)
