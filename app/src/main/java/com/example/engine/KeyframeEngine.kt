package com.example.engine

import com.example.model.ReferenceStyleProfile
import com.example.model.SpeechBoundary
import com.example.model.ZoomEvent
import com.example.model.ZoomKeyframe
import kotlin.math.pow
import kotlin.math.roundToLong

object KeyframeEngine {

  /**
   * Smooth cubic ease-out interpolation function:
   * f(t) = 1 - (1 - t)^3
   */
  fun cubicEaseOut(t: Double): Double {
    val clamped = t.coerceIn(0.0, 1.0)
    return 1.0 - (1.0 - clamped).pow(3)
  }

  /**
   * Creates a calibrated ZoomEvent for a speech boundary according to
   * the exact frame-based keyframe model.
   *
   * Offsets (calculated using actual video FPS):
   * T - 1 frame -> 1.00x
   * T           -> wideScale (0.70x)
   * T + 2 frames -> 0.75x
   * T + 4 frames -> 0.82x
   * T + 6 frames -> 0.90x
   * T + 8 frames -> 0.96x
   * T + 10 frames -> 1.00x
   */
  fun createZoomEvent(
    boundary: SpeechBoundary,
    fps: Double,
    styleProfile: ReferenceStyleProfile = ReferenceStyleProfile(),
    customWideScale: Double? = null,
    durationFrames: Int = 10,
    posX: Double = 0.50,
    posY: Double = 0.50
  ): ZoomEvent {
    val actualFps = fps.coerceIn(12.0, 120.0)
    val frameDuration = 1.0 / actualFps
    val boundaryTime = boundary.time
    val boundaryFrame = (boundaryTime * actualFps).roundToLong()
    val wide = customWideScale ?: styleProfile.wideScale

    // Scaled proportions for intermediate keyframes if wideScale changes
    val scaleRange = 1.00 - wide
    val keyframes = listOf(
      ZoomKeyframe(
        offsetFrames = -1,
        frame = boundaryFrame - 1,
        timestamp = boundaryTime - frameDuration,
        scale = 1.00,
        positionX = posX,
        positionY = posY
      ),
      ZoomKeyframe(
        offsetFrames = 0,
        frame = boundaryFrame,
        timestamp = boundaryTime,
        scale = wide, // 0.70x
        positionX = posX,
        positionY = posY
      ),
      ZoomKeyframe(
        offsetFrames = 2,
        frame = boundaryFrame + 2,
        timestamp = boundaryTime + (2 * frameDuration),
        scale = wide + (scaleRange * (0.05 / 0.30)), // ~0.75x
        positionX = posX,
        positionY = posY
      ),
      ZoomKeyframe(
        offsetFrames = 4,
        frame = boundaryFrame + 4,
        timestamp = boundaryTime + (4 * frameDuration),
        scale = wide + (scaleRange * (0.12 / 0.30)), // ~0.82x
        positionX = posX,
        positionY = posY
      ),
      ZoomKeyframe(
        offsetFrames = 6,
        frame = boundaryFrame + 6,
        timestamp = boundaryTime + (6 * frameDuration),
        scale = wide + (scaleRange * (0.20 / 0.30)), // ~0.90x
        positionX = posX,
        positionY = posY
      ),
      ZoomKeyframe(
        offsetFrames = 8,
        frame = boundaryFrame + 8,
        timestamp = boundaryTime + (8 * frameDuration),
        scale = wide + (scaleRange * (0.26 / 0.30)), // ~0.96x
        positionX = posX,
        positionY = posY
      ),
      ZoomKeyframe(
        offsetFrames = durationFrames,
        frame = boundaryFrame + durationFrames,
        timestamp = boundaryTime + (durationFrames * frameDuration),
        scale = 1.00, // 1.00x recovery
        positionX = posX,
        positionY = posY
      )
    )

    return ZoomEvent(
      boundaryFrame = boundaryFrame,
      boundaryTime = boundaryTime,
      normalScale = styleProfile.normalScale,
      wideScale = wide,
      durationFrames = durationFrames,
      positionX = posX,
      positionY = posY,
      keyframes = keyframes,
      interpolation = "cubic-ease-out",
      confidence = boundary.boundaryConfidence
    )
  }

  /**
   * Evaluates scale for a single ZoomEvent at continuous timestamp in seconds.
   */
  fun evaluateEventScaleAt(event: ZoomEvent, timeSeconds: Double): Double {
    val keyframes = event.keyframes.sortedBy { it.timestamp }
    if (keyframes.isEmpty()) return event.normalScale

    val start = keyframes.first().timestamp
    val end = keyframes.last().timestamp
    if (timeSeconds < start || timeSeconds > end) return event.normalScale

    if (timeSeconds <= start) return keyframes.first().scale
    if (timeSeconds >= end) return keyframes.last().scale

    for (i in 0 until keyframes.size - 1) {
      val k1 = keyframes[i]
      val k2 = keyframes[i + 1]
      if (timeSeconds in k1.timestamp..k2.timestamp) {
        val span = k2.timestamp - k1.timestamp
        if (span <= 0.0) return k1.scale
        val u = (timeSeconds - k1.timestamp) / span
        val factor = cubicEaseOut(u)
        return k1.scale + (k2.scale - k1.scale) * factor
      }
    }
    return event.normalScale
  }

  /**
   * Deterministically calculates the scale factor at any given frame index.
   */
  fun getScaleAtFrame(
    frame: Long,
    zoomEvents: List<ZoomEvent>,
    normalScale: Double = 1.00
  ): Double {
    if (zoomEvents.isEmpty()) return normalScale

    // Find any zoom events active at this frame
    val activeEvents = zoomEvents.filter { event ->
      val start = event.boundaryFrame - 1
      val end = event.boundaryFrame + event.durationFrames
      frame in start..end
    }

    if (activeEvents.isEmpty()) return normalScale

    // In case of closely spaced boundaries, take the deepest zoom (lowest scale) to honor cuts
    return activeEvents.minOfOrNull { event ->
      val keyframes = event.keyframes.sortedBy { it.frame }
      if (keyframes.isEmpty()) return@minOfOrNull normalScale
      if (frame <= keyframes.first().frame) return@minOfOrNull keyframes.first().scale
      if (frame >= keyframes.last().frame) return@minOfOrNull keyframes.last().scale

      for (i in 0 until keyframes.size - 1) {
        val k1 = keyframes[i]
        val k2 = keyframes[i + 1]
        if (frame in k1.frame..k2.frame) {
          val span = (k2.frame - k1.frame).toDouble()
          if (span <= 0.0) return@minOfOrNull k1.scale
          val u = (frame - k1.frame).toDouble() / span
          val factor = cubicEaseOut(u)
          return@minOfOrNull k1.scale + (k2.scale - k1.scale) * factor
        }
      }
      normalScale
    } ?: normalScale
  }

  /**
   * Calculates continuous scale at precise timestamp in seconds.
   * Handles closely spaced boundaries smoothly without jumping.
   */
  fun getScaleAtTime(
    timeSeconds: Double,
    fps: Double,
    zoomEvents: List<ZoomEvent>,
    normalScale: Double = 1.00
  ): Double {
    if (zoomEvents.isEmpty()) return normalScale

    val actualFps = fps.coerceIn(12.0, 120.0)
    val frameDuration = 1.0 / actualFps

    val activeEvents = zoomEvents.filter { event ->
      val start = event.boundaryTime - frameDuration
      val end = event.boundaryTime + (event.durationFrames * frameDuration)
      timeSeconds in start..end
    }

    if (activeEvents.isEmpty()) return normalScale

    // For closely spaced cuts, take minimum scale (maximum zoom effect)
    return activeEvents.minOfOrNull { evaluateEventScaleAt(it, timeSeconds) } ?: normalScale
  }

  /**
   * Directly consumes the flat list of generated ZoomKeyframes and calculates
   * the exact ease-out scale at continuous timestamp in seconds.
   *
   * Transforms 1.00x -> 0.70x at the speech boundary, followed by
   * smooth cubic ease-out recovery back to 1.00x.
   * Resolves closely spaced and overlapping events cleanly by taking the active minimum scale.
   */
  fun getScaleFromKeyframes(
    timeSeconds: Double,
    keyframes: List<ZoomKeyframe>,
    normalScale: Double = 1.00
  ): Double {
    if (keyframes.isEmpty()) return normalScale

    // Group keyframes by boundary or evaluate across consecutive intervals
    var minScale = normalScale
    var foundActive = false

    val sorted = keyframes.sortedBy { it.timestamp }
    for (i in 0 until sorted.size - 1) {
      val k1 = sorted[i]
      val k2 = sorted[i + 1]

      val span = k2.timestamp - k1.timestamp
      if (span in 0.0001..0.60 && timeSeconds in k1.timestamp..k2.timestamp) {
        val u = (timeSeconds - k1.timestamp) / span
        val factor = cubicEaseOut(u)
        val s = k1.scale + (k2.scale - k1.scale) * factor
        if (s < minScale) {
          minScale = s
          foundActive = true
        }
      }
    }

    return if (foundActive) minScale else normalScale
  }
}
