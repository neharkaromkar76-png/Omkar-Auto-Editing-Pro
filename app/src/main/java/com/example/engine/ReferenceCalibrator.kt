package com.example.engine

import com.example.model.ReferenceStyleProfile

object ReferenceCalibrator {

  val REFERENCE_TIMESTAMPS = listOf(
    2.081, 3.039, 4.495, 6.535, 8.034, 11.530, 12.862, 14.694,
    16.234, 20.604, 21.811, 24.850, 25.599, 28.346, 29.845, 31.135,
    32.301, 33.300, 34.299, 37.837, 38.919, 43.581, 45.662, 47.535
  )

  const val CALIBRATED_DURATION = 48.576
  const val CALIBRATED_FPS = 24.02421
  const val CALIBRATED_NORMAL_SCALE = 1.00
  const val CALIBRATED_WIDE_SCALE = 0.70

  fun getDefaultProfile(): ReferenceStyleProfile {
    return ReferenceStyleProfile(
      fps = CALIBRATED_FPS,
      normalScale = CALIBRATED_NORMAL_SCALE,
      wideScale = CALIBRATED_WIDE_SCALE,
      keyframeOffsets = listOf(-1, 0, 2, 4, 6, 8, 10),
      scales = listOf(1.00, 0.70, 0.75, 0.82, 0.90, 0.96, 1.00),
      interpolation = "cubic-ease-out",
      centerX = 0.50,
      centerY = 0.50
    )
  }

  data class CalibrationReport(
    val sampleDuration: Double,
    val sampleFps: Double,
    val detectedEventCount: Int,
    val averageInterval: Double,
    val minimumInterval: Double,
    val estimatedWideScale: Double,
    val zoomRecoveryFrames: Int,
    val curveType: String
  )

  fun getCalibrationReport(profile: ReferenceStyleProfile = getDefaultProfile()): CalibrationReport {
    val diffs = mutableListOf<Double>()
    for (i in 0 until REFERENCE_TIMESTAMPS.size - 1) {
      diffs.add(REFERENCE_TIMESTAMPS[i + 1] - REFERENCE_TIMESTAMPS[i])
    }
    val avgInterval = if (diffs.isNotEmpty()) diffs.average() else 1.95
    val minInterval = diffs.minOrNull() ?: 0.749

    return CalibrationReport(
      sampleDuration = CALIBRATED_DURATION,
      sampleFps = profile.fps,
      detectedEventCount = REFERENCE_TIMESTAMPS.size,
      averageInterval = avgInterval,
      minimumInterval = minInterval,
      estimatedWideScale = profile.wideScale,
      zoomRecoveryFrames = 10,
      curveType = "cubic-ease-out"
    )
  }
}
