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
   * Offsets:
   * F - 1 -> 1.00
   * F     -> wideScale (0.70)
   * F + 2 -> 0.75
   * F + 4 -> 0.82
   * F + 6 -> 0.90
   * F + 8 -> 0.96
   * F + 10 -> 1.00
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
    val boundaryFrame = boundary.frame
    val wide = customWideScale ?: styleProfile.wideScale

    // Scaled proportions for intermediate keyframes if wideScale changes
    val scaleRange = 1.00 - wide
    val keyframes = listOf(
      ZoomKeyframe(
        offsetFrames = -1,
        frame = boundaryFrame - 1,
        timestamp = (boundaryFrame - 1) / fps,
        scale = 1.00,
        positionX = posX,
        positionY = posY
      ),
      ZoomKeyframe(
        offsetFrames = 0,
        frame = boundaryFrame,
        timestamp = boundaryFrame / fps,
        scale = wide,
        positionX = posX,
        positionY = posY
      ),
      ZoomKeyframe(
        offsetFrames = 2,
        frame = boundaryFrame + 2,
        timestamp = (boundaryFrame + 2) / fps,
        scale = wide + (scaleRange * (0.05 / 0.30)), // ~0.75 default
        positionX = posX,
        positionY = posY
      ),
      ZoomKeyframe(
        offsetFrames = 4,
        frame = boundaryFrame + 4,
        timestamp = (boundaryFrame + 4) / fps,
        scale = wide + (scaleRange * (0.12 / 0.30)), // ~0.82 default
        positionX = posX,
        positionY = posY
      ),
      ZoomKeyframe(
        offsetFrames = 6,
        frame = boundaryFrame + 6,
        timestamp = (boundaryFrame + 6) / fps,
        scale = wide + (scaleRange * (0.20 / 0.30)), // ~0.90 default
        positionX = posX,
        positionY = posY
      ),
      ZoomKeyframe(
        offsetFrames = 8,
        frame = boundaryFrame + 8,
        timestamp = (boundaryFrame + 8) / fps,
        scale = wide + (scaleRange * (0.26 / 0.30)), // ~0.96 default
        positionX = posX,
        positionY = posY
      ),
      ZoomKeyframe(
        offsetFrames = durationFrames,
        frame = boundaryFrame + durationFrames,
        timestamp = (boundaryFrame + durationFrames) / fps,
        scale = 1.00,
        positionX = posX,
        positionY = posY
      )
    )

    return ZoomEvent(
      boundaryFrame = boundaryFrame,
      boundaryTime = boundary.time,
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
   * Deterministically calculates the scale factor at any given frame index.
   */
  fun getScaleAtFrame(
    frame: Long,
    zoomEvents: List<ZoomEvent>,
    normalScale: Double = 1.00
  ): Double {
    if (zoomEvents.isEmpty()) return normalScale

    // Find any zoom event that is active at this frame
    // A zoom event is active from (boundaryFrame - 1) to (boundaryFrame + durationFrames)
    // If multiple overlap, the most recently triggered boundary takes precedence
    val activeEvent = zoomEvents
      .filter { event ->
        val start = event.boundaryFrame - 1
        val end = event.boundaryFrame + event.durationFrames
        frame in start..end
      }
      .maxByOrNull { it.boundaryFrame }

    if (activeEvent == null) return normalScale

    val keyframes = activeEvent.keyframes.sortedBy { it.frame }
    if (keyframes.isEmpty()) return normalScale

    if (frame <= keyframes.first().frame) return keyframes.first().scale
    if (frame >= keyframes.last().frame) return keyframes.last().scale

    // Find bounding keyframes
    for (i in 0 until keyframes.size - 1) {
      val k1 = keyframes[i]
      val k2 = keyframes[i + 1]

      if (frame in k1.frame..k2.frame) {
        val span = (k2.frame - k1.frame).toDouble()
        if (span <= 0.0) return k1.scale
        val u = (frame - k1.frame).toDouble() / span
        val factor = cubicEaseOut(u)
        return k1.scale + (k2.scale - k1.scale) * factor
      }
    }

    return normalScale
  }

  /**
   * Calculates scale at continuous timestamp in seconds.
   */
  fun getScaleAtTime(
    timeSeconds: Double,
    fps: Double,
    zoomEvents: List<ZoomEvent>,
    normalScale: Double = 1.00
  ): Double {
    val frame = (timeSeconds * fps).roundToLong()
    return getScaleAtFrame(frame, zoomEvents, normalScale)
  }
}
