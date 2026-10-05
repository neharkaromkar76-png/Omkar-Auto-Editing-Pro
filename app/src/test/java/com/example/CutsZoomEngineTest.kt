package com.example

import com.example.engine.KeyframeEngine
import com.example.engine.ReferenceCalibrator
import com.example.engine.SpeechBoundaryAnalyzer
import com.example.model.BoundaryScoreDetails
import com.example.model.MediaMetadata
import com.example.model.ReferenceStyleProfile
import com.example.model.SpeechBoundary
import com.example.model.SpeechWord
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToLong

class CutsZoomEngineTest {

  @Test
  fun testCalibratedKeyframeProfileValues() {
    val fps = 24.02421
    val boundaryTime = 4.495
    val boundaryFrame = (boundaryTime * fps).roundToLong()
    assertEquals(108L, boundaryFrame)

    val boundary = SpeechBoundary(
      time = boundaryTime,
      frame = boundaryFrame,
      boundaryConfidence = 0.96
    )

    val zoomEvent = KeyframeEngine.createZoomEvent(
      boundary = boundary,
      fps = fps,
      styleProfile = ReferenceCalibrator.getDefaultProfile()
    )

    assertEquals(7, zoomEvent.keyframes.size)

    val kfMap = zoomEvent.keyframes.associateBy { it.offsetFrames }

    // Check calibrated offsets & scales
    assertEquals(1.00, kfMap[-1]?.scale ?: 0.0, 0.001)
    assertEquals(0.70, kfMap[0]?.scale ?: 0.0, 0.001)
    assertEquals(0.75, kfMap[2]?.scale ?: 0.0, 0.01)
    assertEquals(0.82, kfMap[4]?.scale ?: 0.0, 0.01)
    assertEquals(0.90, kfMap[6]?.scale ?: 0.0, 0.01)
    assertEquals(0.96, kfMap[8]?.scale ?: 0.0, 0.01)
    assertEquals(1.00, kfMap[10]?.scale ?: 0.0, 0.001)
  }

  @Test
  fun testCubicEaseOutInterpolation() {
    // f(t) = 1 - (1 - t)^3
    assertEquals(0.0, KeyframeEngine.cubicEaseOut(0.0), 0.0001)
    assertEquals(1.0, KeyframeEngine.cubicEaseOut(1.0), 0.0001)

    // Intermediate points must be strictly monotonic and concave down
    val half = KeyframeEngine.cubicEaseOut(0.5)
    assertTrue("Cubic ease-out should be faster than linear at 0.5", half > 0.5)
    assertEquals(0.875, half, 0.001) // 1 - 0.5^3 = 1 - 0.125 = 0.875
  }

  @Test
  fun testScaleAtFrameCalculation() {
    val fps = 24.0
    val boundary = SpeechBoundary(
      time = 2.0,
      frame = 48L,
      boundaryConfidence = 0.95
    )
    val event = KeyframeEngine.createZoomEvent(boundary, fps)

    // Before boundary
    assertEquals(1.00, KeyframeEngine.getScaleAtFrame(40L, listOf(event)), 0.001)
    assertEquals(1.00, KeyframeEngine.getScaleAtFrame(47L, listOf(event)), 0.001)

    // At exact boundary frame: wideScale 0.70
    assertEquals(0.70, KeyframeEngine.getScaleAtFrame(48L, listOf(event)), 0.001)

    // In between (e.g. frame 49): between 0.70 and 0.75
    val scaleAt49 = KeyframeEngine.getScaleAtFrame(49L, listOf(event))
    assertTrue(scaleAt49 in 0.70..0.76)

    // At frame 58 (F + 10): back to 1.00
    assertEquals(1.00, KeyframeEngine.getScaleAtFrame(58L, listOf(event)), 0.001)

    // After recovery: continues at 1.00
    assertEquals(1.00, KeyframeEngine.getScaleAtFrame(65L, listOf(event)), 0.001)
  }

  @Test
  fun testReferenceCalibratorDefaults() {
    val report = ReferenceCalibrator.getCalibrationReport()
    assertEquals(48.576, report.sampleDuration, 0.001)
    assertEquals(24, report.detectedEventCount)
    assertEquals(0.70, report.estimatedWideScale, 0.001)
    assertEquals(10, report.zoomRecoveryFrames)
  }

  @Test
  fun testSpeechBoundaryCadenceAndScoring() = runBlocking {
    val metadata = MediaMetadata(
      durationMs = 10000L,
      durationSeconds = 10.0,
      fps = 24.0,
      width = 1080,
      height = 1920
    )

    val waveform = List(100) { 0.5f }
    val (words, segments) = SpeechBoundaryAnalyzer.transcribeVideoSpeech(metadata, waveform)

    assertTrue("Speech words should be transcribed", words.isNotEmpty())
    assertTrue("Speech segments should be created", segments.isNotEmpty())

    val boundaries = SpeechBoundaryAnalyzer.detectBoundaries(
      words = words,
      segments = segments,
      metadata = metadata,
      styleProfile = ReferenceStyleProfile()
    )

    assertTrue("Boundaries should be detected", boundaries.isNotEmpty())

    val first = boundaries.first()
    assertTrue("Confidence should be scored", first.boundaryConfidence >= 0.70)
    assertNotNull(first.scoreDetails)
    assertTrue("Frame must be positive", first.frame > 0)
  }

  @Test
  fun testSafeExportFileNameFormat() {
    val name = com.example.engine.ExportStorageManager.generateSafeExportFileName("CutsZoom_Edit")
    assertTrue("Filename should start with CutsZoom_Edit", name.startsWith("CutsZoom_Edit_"))
    assertTrue("Filename should end with .mp4", name.endsWith(".mp4"))
    // Ensure no forbidden characters for filenames
    val illegalChars = listOf("/", "\\", ":", "*", "?", "\"", "<", ">", "|")
    illegalChars.forEach { char ->
      assertTrue("Filename should not contain $char", !name.contains(char))
    }
  }
}
