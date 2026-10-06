package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AudioWaveformExtractor
import com.example.engine.KeyframeEngine
import com.example.engine.MediaInspector
import com.example.engine.ReferenceCalibrator
import com.example.engine.SampleVideoGenerator
import com.example.engine.SpeechBoundaryAnalyzer
import com.example.engine.VideoExportEngine
import com.example.model.AutoEditState
import com.example.model.BoundaryScoreDetails
import com.example.model.EditTimeline
import com.example.model.ExportResult
import com.example.model.ExportSettings
import com.example.model.MediaMetadata
import com.example.model.ProcessingStage
import com.example.model.ReferenceStyleProfile
import com.example.model.SpeechBoundary
import com.example.model.ZoomEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToLong

enum class EditorTab(val label: String) {
  PROJECT("Project"),
  TIMELINE("Timeline"),
  AI_ANALYSIS("AI Analysis"),
  INSPECTOR("Inspector"),
  EXPORT("Export")
}

class EditorViewModel(application: Application) : AndroidViewModel(application) {

  private val _timeline = MutableStateFlow<EditTimeline?>(null)
  val timeline: StateFlow<EditTimeline?> = _timeline.asStateFlow()

  private val _mediaMetadata = MutableStateFlow<MediaMetadata?>(null)
  val mediaMetadata: StateFlow<MediaMetadata?> = _mediaMetadata.asStateFlow()

  private val _waveform = MutableStateFlow<List<Float>>(emptyList())
  val waveform: StateFlow<List<Float>> = _waveform.asStateFlow()

  private val _autoEditState = MutableStateFlow(AutoEditState())
  val autoEditState: StateFlow<AutoEditState> = _autoEditState.asStateFlow()

  private val _exportSettings = MutableStateFlow(ExportSettings())
  val exportSettings: StateFlow<ExportSettings> = _exportSettings.asStateFlow()

  private val _exportResult = MutableStateFlow<ExportResult?>(null)
  val exportResult: StateFlow<ExportResult?> = _exportResult.asStateFlow()

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _currentTimeMs = MutableStateFlow(0L)
  val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

  private val _currentScale = MutableStateFlow(1.0)
  val currentScale: StateFlow<Double> = _currentScale.asStateFlow()

  private val _currentFrame = MutableStateFlow(0L)
  val currentFrame: StateFlow<Long> = _currentFrame.asStateFlow()

  private val _selectedZoomEvent = MutableStateFlow<ZoomEvent?>(null)
  val selectedZoomEvent: StateFlow<ZoomEvent?> = _selectedZoomEvent.asStateFlow()

  private val _activeTab = MutableStateFlow(EditorTab.PROJECT)
  val activeTab: StateFlow<EditorTab> = _activeTab.asStateFlow()

  private val _styleProfile = MutableStateFlow(ReferenceCalibrator.getDefaultProfile())
  val styleProfile: StateFlow<ReferenceStyleProfile> = _styleProfile.asStateFlow()

  private val _customApiKey = MutableStateFlow("")
  val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

  private var playbackJob: Job? = null

  init {
    // Automatically prepare sample reference video for immediate testing
    loadSampleVideo()
  }

  fun setTab(tab: EditorTab) {
    _activeTab.value = tab
  }

  fun setCustomApiKey(key: String) {
    _customApiKey.value = key
  }

  fun loadSampleVideo() {
    viewModelScope.launch {
      _autoEditState.value = AutoEditState(
        isProcessing = true,
        currentStage = ProcessingStage.UPLOADING,
        progress = 0.05f,
        statusMessage = "Loading calibrated vertical reference speech clip..."
      )

      val context = getApplication<Application>()
      val sampleUri = SampleVideoGenerator.getOrCreateSampleVideo(context)
      loadVideo(sampleUri, isSample = true)
    }
  }

  fun loadUserVideo(uri: Uri) {
    viewModelScope.launch {
      val context = getApplication<Application>()
      _autoEditState.value = AutoEditState(
        isProcessing = true,
        currentStage = ProcessingStage.UPLOADING,
        progress = 0.05f,
        statusMessage = "Importing selected video..."
      )

      val localUri = try {
        if (uri.scheme == "file") {
          uri
        } else {
          val cachedFile = File(context.cacheDir, "imported_source_${System.currentTimeMillis()}.mp4")
          context.contentResolver.openInputStream(uri)?.use { input ->
            java.io.FileOutputStream(cachedFile).use { output ->
              input.copyTo(output)
            }
          }
          if (cachedFile.exists() && cachedFile.length() > 0) {
            Uri.fromFile(cachedFile)
          } else {
            uri
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
        uri
      }

      loadVideo(localUri, isSample = false)
    }
  }

  private suspend fun loadVideo(uri: Uri, isSample: Boolean) {
    val context = getApplication<Application>()
    _autoEditState.value = AutoEditState(
      isProcessing = true,
      currentStage = ProcessingStage.INSPECTING,
      progress = 0.15f,
      statusMessage = "Inspecting media metadata and tracks..."
    )

    val metadata = MediaInspector.inspect(context, uri)
    _mediaMetadata.value = metadata

    _autoEditState.value = AutoEditState(
      isProcessing = true,
      currentStage = ProcessingStage.EXTRACTING_AUDIO,
      progress = 0.25f,
      statusMessage = "Extracting audio waveform..."
    )

    val wf = AudioWaveformExtractor.extractWaveform(context, uri, numBars = 100)
    _waveform.value = wf

    _currentTimeMs.value = 0L
    _currentFrame.value = 0L
    _currentScale.value = 1.0

    _autoEditState.value = AutoEditState(
      isProcessing = false,
      progress = 1.0f,
      statusMessage = "Video loaded. Starting AI speech analysis..."
    )

    // Automatically trigger Auto Edit pipeline so boundaries and keyframes are immediately active
    runAutoEdit()
  }

  /**
   * Executes the complete real 12-stage Auto Edit pipeline:
   * Media Inspection -> Audio -> Speech Transcription -> Word Timestamps ->
   * Semantic Sentence Boundary Detection -> Confidence Scoring -> Keyframes -> Timeline
   */
  fun runAutoEdit() {
    val metadata = _mediaMetadata.value ?: return
    viewModelScope.launch {
      val context = getApplication<Application>()

      _autoEditState.value = AutoEditState(
        isProcessing = true,
        currentStage = ProcessingStage.UPLOADING,
        progress = ProcessingStage.UPLOADING.baseProgress,
        statusMessage = "Validating media stream..."
      )
      delay(100)

      _autoEditState.value = AutoEditState(
        isProcessing = true,
        currentStage = ProcessingStage.INSPECTING,
        progress = ProcessingStage.INSPECTING.baseProgress,
        statusMessage = "Media: ${metadata.width}x${metadata.height} @ ${String.format("%.2f", metadata.fps)} FPS (${metadata.durationSeconds}s)"
      )
      delay(100)

      _autoEditState.value = AutoEditState(
        isProcessing = true,
        currentStage = ProcessingStage.EXTRACTING_AUDIO,
        progress = ProcessingStage.EXTRACTING_AUDIO.baseProgress,
        statusMessage = "Computing RMS audio energy & speech cadence..."
      )
      val wf = if (_waveform.value.isEmpty()) {
        AudioWaveformExtractor.extractWaveform(context, Uri.parse(metadata.uri))
      } else {
        _waveform.value
      }
      _waveform.value = wf
      delay(150)

      _autoEditState.value = AutoEditState(
        isProcessing = true,
        currentStage = ProcessingStage.TRANSCRIBING,
        progress = ProcessingStage.TRANSCRIBING.baseProgress,
        statusMessage = "Transcribing spoken speech..."
      )
      val (words, segments) = SpeechBoundaryAnalyzer.transcribeVideoSpeech(metadata, wf)
      if (words.isEmpty()) {
        val errorMessage = if (!metadata.hasAudio) {
          "No audio track detected in imported video. Speech-to-text requires audio."
        } else {
          "No spoken words or voice activity detected in imported video. Speech-to-text requires audible speech."
        }
        _autoEditState.value = AutoEditState(
          isProcessing = false,
          progress = 0f,
          statusMessage = errorMessage
        )
        Log.e("CUTSZOOM_PIPELINE", "STT returned 0 words. Stopping pipeline: $errorMessage")
        return@launch
      }
      delay(150)

      _autoEditState.value = AutoEditState(
        isProcessing = true,
        currentStage = ProcessingStage.DETECTING_TIMESTAMPS,
        progress = ProcessingStage.DETECTING_TIMESTAMPS.baseProgress,
        statusMessage = "Extracted ${words.size} word timestamps & inter-word acoustic pauses..."
      )
      delay(150)

      _autoEditState.value = AutoEditState(
        isProcessing = true,
        currentStage = ProcessingStage.FINDING_BOUNDARIES,
        progress = ProcessingStage.FINDING_BOUNDARIES.baseProgress,
        statusMessage = "Analyzing semantic sentence & thought completion..."
      )
      val detectedBoundaries = SpeechBoundaryAnalyzer.detectBoundaries(
        words = words,
        segments = segments,
        metadata = metadata,
        styleProfile = _styleProfile.value,
        customApiKey = _customApiKey.value.takeIf { it.isNotBlank() }
      )
      delay(200)

      _autoEditState.value = AutoEditState(
        isProcessing = true,
        currentStage = ProcessingStage.CALCULATING_CONFIDENCE,
        progress = ProcessingStage.CALCULATING_CONFIDENCE.baseProgress,
        statusMessage = "Scored ${detectedBoundaries.size} boundaries across 5 linguistic metrics..."
      )
      delay(150)

      _autoEditState.value = AutoEditState(
        isProcessing = true,
        currentStage = ProcessingStage.BUILDING_TIMELINE,
        progress = ProcessingStage.BUILDING_TIMELINE.baseProgress,
        statusMessage = "Assembling canonical timeline JSON..."
      )
      val timelineObj = SpeechBoundaryAnalyzer.buildTimeline(
        metadata = metadata,
        words = words,
        segments = segments,
        boundaries = detectedBoundaries,
        styleProfile = _styleProfile.value
      )
      delay(150)

      val totalKeyframes = timelineObj.zoomEvents.sumOf { it.keyframes.size }
      val firstBoundary = detectedBoundaries.firstOrNull()?.let { String.format(Locale.US, "%.2fs", it.time) } ?: "NONE"
      val firstZoomEvent = timelineObj.zoomEvents.firstOrNull()?.let {
        "Time: ${String.format(Locale.US, "%.2fs", it.boundaryTime)}, Scale: ${String.format(Locale.US, "%.2fx", it.wideScale)}"
      } ?: "NONE"

      // Mandatory Debug Information Logging (Requirement 1, 9, 15)
      Log.i("CUTSZOOM_PIPELINE", "==================================================")
      Log.i("CUTSZOOM_PIPELINE", "VIDEO FPS: ${metadata.fps}")
      Log.i("CUTSZOOM_PIPELINE", "VIDEO DURATION: ${String.format(Locale.US, "%.2f", metadata.durationSeconds)}s")
      Log.i("CUTSZOOM_PIPELINE", "AUDIO_PRESENT: ${if (metadata.hasAudio) "YES" else "NO"}")
      Log.i("CUTSZOOM_PIPELINE", "TRANSCRIPT_LENGTH: ${segments.sumOf { it.text.length }}")
      Log.i("CUTSZOOM_PIPELINE", "WORD COUNT: ${words.size}")
      Log.i("CUTSZOOM_PIPELINE", "BOUNDARY COUNT: ${detectedBoundaries.size}")
      Log.i("CUTSZOOM_PIPELINE", "BOUNDARIES: ${detectedBoundaries.joinToString { String.format(Locale.US, "%.2fs", it.time) }}")
      Log.i("CUTSZOOM_PIPELINE", "EDIT_EVENT_COUNT: ${timelineObj.zoomEvents.size}")
      Log.i("CUTSZOOM_PIPELINE", "KEYFRAME COUNT: $totalKeyframes")
      Log.i("CUTSZOOM_PIPELINE", "FIRST_BOUNDARY: $firstBoundary")
      Log.i("CUTSZOOM_PIPELINE", "FIRST_ZOOM_EVENT: $firstZoomEvent")
      Log.i("CUTSZOOM_PIPELINE", "==================================================")

      println("VIDEO FPS: ${metadata.fps}")
      println("VIDEO DURATION: ${String.format(Locale.US, "%.2f", metadata.durationSeconds)}s")
      println("WORD COUNT: ${words.size}")
      println("BOUNDARY COUNT: ${detectedBoundaries.size}")
      println("EDIT_EVENT_COUNT: ${timelineObj.zoomEvents.size}")
      println("KEYFRAME COUNT: $totalKeyframes")
      println("FIRST_BOUNDARY: $firstBoundary")
      println("FIRST_ZOOM_EVENT: $firstZoomEvent")

      _autoEditState.value = AutoEditState(
        isProcessing = true,
        currentStage = ProcessingStage.GENERATING_KEYFRAMES,
        progress = ProcessingStage.GENERATING_KEYFRAMES.baseProgress,
        statusMessage = "Generated ${timelineObj.zoomEvents.size} calibrated zoom keyframe curves ($totalKeyframes keyframes)..."
      )
      delay(150)

      _timeline.value = timelineObj
      _selectedZoomEvent.value = timelineObj.zoomEvents.firstOrNull()

      _autoEditState.value = AutoEditState(
        isProcessing = false,
        currentStage = ProcessingStage.RENDERING_PREVIEW,
        progress = 1.0f,
        statusMessage = "Auto Edit completed! ${timelineObj.zoomEvents.size} zoom events ready for preview."
      )

      // Switch to Timeline tab
      _activeTab.value = EditorTab.TIMELINE
    }
  }

  fun seekTo(timeMs: Long) {
    val meta = _mediaMetadata.value ?: return
    val clampedMs = timeMs.coerceIn(0L, meta.durationMs)
    _currentTimeMs.value = clampedMs

    val fps = meta.previewFps
    val timeSec = clampedMs / 1000.0
    val frame = (timeSec * fps).roundToLong()
    _currentFrame.value = frame

    val currentTl = _timeline.value
    if (currentTl != null) {
      _currentScale.value = KeyframeEngine.getScaleAtTime(
        timeSeconds = timeSec,
        fps = fps,
        zoomEvents = currentTl.zoomEvents,
        normalScale = currentTl.styleProfile.normalScale
      )
    }
  }

  fun onPreviewPositionUpdate(posMs: Long) {
    val meta = _mediaMetadata.value ?: return
    val clampedMs = posMs.coerceIn(0L, meta.durationMs)
    _currentTimeMs.value = clampedMs

    val fps = meta.previewFps
    val timeSec = clampedMs / 1000.0
    val frame = (timeSec * fps).roundToLong()
    _currentFrame.value = frame

    val currentTl = _timeline.value
    if (currentTl != null) {
      _currentScale.value = KeyframeEngine.getScaleAtTime(
        timeSeconds = timeSec,
        fps = fps,
        zoomEvents = currentTl.zoomEvents,
        normalScale = currentTl.styleProfile.normalScale
      )
    }
  }

  fun setPlaying(playing: Boolean) {
    _isPlaying.value = playing
  }

  fun togglePlayPause() {
    _isPlaying.value = !_isPlaying.value
  }

  fun pausePlayback() {
    _isPlaying.value = false
  }

  fun selectZoomEvent(event: ZoomEvent?) {
    _selectedZoomEvent.value = event
    if (event != null) {
      seekTo((event.boundaryTime * 1000).toLong())
    }
  }

  fun updateSelectedZoomScale(newWideScale: Double) {
    val event = _selectedZoomEvent.value ?: return
    val currentTl = _timeline.value ?: return

    val updatedEvent = KeyframeEngine.createZoomEvent(
      boundary = SpeechBoundary(
        id = event.id,
        time = event.boundaryTime,
        frame = event.boundaryFrame,
        boundaryConfidence = event.confidence
      ),
      fps = currentTl.source.fps,
      styleProfile = currentTl.styleProfile,
      customWideScale = newWideScale,
      durationFrames = event.durationFrames,
      posX = event.positionX,
      posY = event.positionY
    )

    val updatedEvents = currentTl.zoomEvents.map {
      if (it.id == event.id) updatedEvent else it
    }

    _selectedZoomEvent.value = updatedEvent
    _timeline.value = currentTl.copy(zoomEvents = updatedEvents)
    seekTo(_currentTimeMs.value)
  }

  fun updateSelectedZoomDuration(newDurationFrames: Int) {
    val event = _selectedZoomEvent.value ?: return
    val currentTl = _timeline.value ?: return

    val updatedEvent = KeyframeEngine.createZoomEvent(
      boundary = SpeechBoundary(
        id = event.id,
        time = event.boundaryTime,
        frame = event.boundaryFrame,
        boundaryConfidence = event.confidence
      ),
      fps = currentTl.source.fps,
      styleProfile = currentTl.styleProfile,
      customWideScale = event.wideScale,
      durationFrames = newDurationFrames.coerceIn(4, 30),
      posX = event.positionX,
      posY = event.positionY
    )

    val updatedEvents = currentTl.zoomEvents.map {
      if (it.id == event.id) updatedEvent else it
    }

    _selectedZoomEvent.value = updatedEvent
    _timeline.value = currentTl.copy(zoomEvents = updatedEvents)
  }

  fun updateSelectedPosition(posX: Double, posY: Double) {
    val event = _selectedZoomEvent.value ?: return
    val currentTl = _timeline.value ?: return

    val updatedEvent = event.copy(
      positionX = posX.coerceIn(0.1, 0.9),
      positionY = posY.coerceIn(0.1, 0.9)
    )

    val updatedEvents = currentTl.zoomEvents.map {
      if (it.id == event.id) updatedEvent else it
    }

    _selectedZoomEvent.value = updatedEvent
    _timeline.value = currentTl.copy(zoomEvents = updatedEvents)
  }

  fun deleteSelectedZoomEvent() {
    val event = _selectedZoomEvent.value ?: return
    val currentTl = _timeline.value ?: return

    val updatedEvents = currentTl.zoomEvents.filterNot { it.id == event.id }
    val updatedBoundaries = currentTl.boundaries.filterNot { it.frame == event.boundaryFrame }

    _timeline.value = currentTl.copy(
      zoomEvents = updatedEvents,
      boundaries = updatedBoundaries
    )
    _selectedZoomEvent.value = updatedEvents.firstOrNull()
    seekTo(_currentTimeMs.value)
  }

  fun addSplitAtCurrentPlayhead() {
    val meta = _mediaMetadata.value ?: return
    val currentTl = _timeline.value ?: return

    val timeSec = _currentTimeMs.value / 1000.0
    val frame = (timeSec * meta.fps).roundToLong()

    val newBoundary = SpeechBoundary(
      id = UUID.randomUUID().toString(),
      time = timeSec,
      frame = frame,
      boundaryConfidence = 0.99,
      scoreDetails = BoundaryScoreDetails(
        pauseScore = 1.0,
        punctuationScore = 1.0,
        semanticCompletionScore = 1.0,
        rhythmScore = 1.0,
        speechBoundaryScore = 1.0
      ),
      reason = "Manual user split marker",
      isApproved = true
    )

    val newZoomEvent = KeyframeEngine.createZoomEvent(
      boundary = newBoundary,
      fps = meta.fps,
      styleProfile = currentTl.styleProfile
    )

    val updatedBoundaries = (currentTl.boundaries + newBoundary).sortedBy { it.time }
    val updatedEvents = (currentTl.zoomEvents + newZoomEvent).sortedBy { it.boundaryFrame }

    _timeline.value = currentTl.copy(
      boundaries = updatedBoundaries,
      zoomEvents = updatedEvents
    )
    _selectedZoomEvent.value = newZoomEvent
    seekTo(_currentTimeMs.value)
  }

  fun updateStyleProfile(profile: ReferenceStyleProfile) {
    _styleProfile.value = profile
    val currentTl = _timeline.value
    if (currentTl != null) {
      val updatedEvents = currentTl.boundaries.filter { it.isApproved }.map {
        KeyframeEngine.createZoomEvent(it, currentTl.source.fps, profile)
      }
      _timeline.value = currentTl.copy(
        styleProfile = profile,
        zoomEvents = updatedEvents
      )
    }
  }

  fun updateExportSettings(settings: ExportSettings) {
    _exportSettings.value = settings
  }

  fun exportVideo() {
    val tl = _timeline.value ?: return
    val settings = _exportSettings.value
    val context = getApplication<Application>()

    viewModelScope.launch {
      _autoEditState.value = AutoEditState(
        isProcessing = true,
        currentStage = ProcessingStage.RENDERING_EXPORT,
        progress = 0.05f,
        statusMessage = "Starting H.264 video rendering pipeline..."
      )

      val result = VideoExportEngine.exportVideo(
        context = context,
        timeline = tl,
        settings = settings,
        callback = object : VideoExportEngine.ProgressCallback {
          override fun onProgress(stage: String, progress: Float) {
            _autoEditState.value = AutoEditState(
              isProcessing = true,
              currentStage = ProcessingStage.RENDERING_EXPORT,
              progress = progress,
              statusMessage = stage
            )
          }
        }
      )

      _exportResult.value = result

      if (result.success) {
        _autoEditState.value = AutoEditState(
          isProcessing = false,
          currentStage = ProcessingStage.EXPORT_COMPLETE,
          progress = 1.0f,
          statusMessage = "Export complete and container verified!"
        )
        _activeTab.value = EditorTab.EXPORT
      } else {
        _autoEditState.value = AutoEditState(
          isProcessing = false,
          progress = 0.0f,
          statusMessage = "Export failed: ${result.error}",
          error = result.error
        )
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    playbackJob?.cancel()
  }
}
