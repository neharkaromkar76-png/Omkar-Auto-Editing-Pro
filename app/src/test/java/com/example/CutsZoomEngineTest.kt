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

  @Test
  fun testKeyframeGenerationWithActualFpsNon24() {
    val fps = 30.0
    val frameDuration = 1.0 / fps // 0.03333s
    val boundaryTime = 3.500
    val boundary = SpeechBoundary(
      time = boundaryTime,
      frame = (boundaryTime * fps).roundToLong(),
      boundaryConfidence = 0.95
    )

    val zoomEvent = KeyframeEngine.createZoomEvent(boundary, fps)
    assertEquals(7, zoomEvent.keyframes.size)

    val kfMap = zoomEvent.keyframes.associateBy { it.offsetFrames }

    // Verify keyframe timestamps match boundaryTime +/- frameDuration multiples
    assertEquals(boundaryTime - frameDuration, kfMap[-1]?.timestamp ?: 0.0, 0.0001)
    assertEquals(boundaryTime, kfMap[0]?.timestamp ?: 0.0, 0.0001)
    assertEquals(boundaryTime + (2 * frameDuration), kfMap[2]?.timestamp ?: 0.0, 0.0001)
    assertEquals(boundaryTime + (10 * frameDuration), kfMap[10]?.timestamp ?: 0.0, 0.0001)

    // Verify scale values: 1.00 -> 0.70 -> 1.00
    assertEquals(1.00, kfMap[-1]?.scale ?: 0.0, 0.001)
    assertEquals(0.70, kfMap[0]?.scale ?: 0.0, 0.001)
    assertEquals(1.00, kfMap[10]?.scale ?: 0.0, 0.001)

    // Verify continuous scale calculation during playback
    assertEquals(1.00, KeyframeEngine.getScaleAtTime(boundaryTime - 0.1, fps, listOf(zoomEvent)), 0.001)
    assertEquals(0.70, KeyframeEngine.getScaleAtTime(boundaryTime, fps, listOf(zoomEvent)), 0.001)
    assertEquals(1.00, KeyframeEngine.getScaleAtTime(boundaryTime + 0.40, fps, listOf(zoomEvent)), 0.001)
  }

  @Test
  fun testCloselySpacedBoundariesHandling() {
    val fps = 30.0
    val b1 = SpeechBoundary(time = 2.0, frame = 60L, boundaryConfidence = 0.95)
    val b2 = SpeechBoundary(time = 2.5, frame = 75L, boundaryConfidence = 0.93)

    val event1 = KeyframeEngine.createZoomEvent(b1, fps)
    val event2 = KeyframeEngine.createZoomEvent(b2, fps)
    val events = listOf(event1, event2)

    // At 2.0s: zooms out to 0.70x
    assertEquals(0.70, KeyframeEngine.getScaleAtTime(2.0, fps, events), 0.001)
    // Between 2.0 and 2.5: recovers partially
    val midScale = KeyframeEngine.getScaleAtTime(2.3, fps, events)
    assertTrue("Scale at 2.3s should be recovering", midScale > 0.70)
    // At 2.5s: second boundary cuts to 0.70x again
    assertEquals(0.70, KeyframeEngine.getScaleAtTime(2.5, fps, events), 0.001)
  }

  @Test
  fun testSilentAudioSttReturnsEmpty() {
    val metadata = MediaMetadata(
      durationMs = 5000L,
      durationSeconds = 5.0,
      hasAudio = false
    )
    val (words, _) = SpeechBoundaryAnalyzer.transcribeVideoSpeech(metadata, emptyList())
    assertTrue("No words should be returned when audio is missing", words.isEmpty())
  }

  @Test
  fun testEndToEndAutoSplitAndZoomPipeline() = runBlocking {
    val metadata = MediaMetadata(
      displayName = "sample_speech_reference.mp4",
      durationMs = 12000L,
      durationSeconds = 12.0,
      fps = 24.0,
      width = 540,
      height = 960,
      hasAudio = true
    )

    // 1. Audio Waveform
    val waveform = List(100) { if (it % 25 in 5..20) 0.45f else 0.05f }

    // 2. STT Transcription
    val (words, segments) = SpeechBoundaryAnalyzer.transcribeVideoSpeech(metadata, waveform)
    assertTrue("STT must return words", words.isNotEmpty())
    assertTrue("STT must return segments", segments.isNotEmpty())

    // 3. Sentence / Phrase Boundary Detection
    val boundaries = SpeechBoundaryAnalyzer.detectBoundaries(
      words = words,
      segments = segments,
      metadata = metadata,
      styleProfile = ReferenceStyleProfile()
    )
    assertTrue("Boundaries must be detected", boundaries.isNotEmpty())

    // 4. Zoom Keyframe Timeline Generation
    val timeline = SpeechBoundaryAnalyzer.buildTimeline(
      metadata = metadata,
      words = words,
      segments = segments,
      boundaries = boundaries,
      styleProfile = ReferenceStyleProfile()
    )

    val keyframeCount = timeline.zoomEvents.sumOf { it.keyframes.size }
    assertTrue("Zoom events must be created", timeline.zoomEvents.isNotEmpty())
    assertTrue("Keyframes must be created", keyframeCount > 0)

    // Output debug information (Requirement 9 & 14)
    println("==================================================")
    println("VIDEO FPS: ${metadata.fps}")
    println("VIDEO DURATION: ${metadata.durationSeconds}s")
    println("WORD COUNT: ${words.size}")
    println("BOUNDARY COUNT: ${boundaries.size}")
    println("BOUNDARIES: ${boundaries.map { String.format(java.util.Locale.US, "%.2f", it.time) }}")
    println("KEYFRAME COUNT: $keyframeCount")
    println("==================================================")

    // 5. Test Preview Zoom Transitions (1.00x -> 0.70x -> 1.00x)
    for (event in timeline.zoomEvents) {
      val t = event.boundaryTime
      val frameDur = 1.0 / metadata.fps

      val scaleBefore = KeyframeEngine.getScaleAtTime(t - frameDur, metadata.fps, timeline.zoomEvents)
      val scaleAtCut = KeyframeEngine.getScaleAtTime(t, metadata.fps, timeline.zoomEvents)
      val scaleAfter = KeyframeEngine.getScaleAtTime(t + (12 * frameDur), metadata.fps, timeline.zoomEvents)

      assertEquals("Before cut scale should be 1.00x", 1.00, scaleBefore, 0.001)
      assertEquals("At cut scale should be 0.70x wide", 0.70, scaleAtCut, 0.001)
      assertEquals("After recovery scale should return to 1.00x", 1.00, scaleAfter, 0.001)
    }
  }
}
