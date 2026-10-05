package com.example.model

enum class ProcessingStage(val stepNumber: Int, val title: String, val baseProgress: Float) {
  UPLOADING(1, "Uploading video", 0.08f),
  INSPECTING(2, "Inspecting media tracks & FPS", 0.16f),
  EXTRACTING_AUDIO(3, "Extracting audio & computing waveform", 0.26f),
  TRANSCRIBING(4, "Transcribing speech rhythm", 0.38f),
  DETECTING_TIMESTAMPS(5, "Detecting word timestamps & pauses", 0.52f),
  FINDING_BOUNDARIES(6, "Finding natural sentence & phrase boundaries", 0.68f),
  CALCULATING_CONFIDENCE(7, "Calculating boundary confidence scores", 0.78f),
  BUILDING_TIMELINE(8, "Building canonical edit timeline", 0.88f),
  GENERATING_KEYFRAMES(9, "Generating frame-accurate zoom curves", 0.94f),
  RENDERING_PREVIEW(10, "Rendering preview timeline", 1.00f),
  RENDERING_EXPORT(11, "Rendering frame-accurate MP4 export", 0.50f),
  EXPORT_COMPLETE(12, "Export complete & verified", 1.00f)
}

data class AutoEditState(
  val isProcessing: Boolean = false,
  val currentStage: ProcessingStage? = null,
  val progress: Float = 0f,
  val statusMessage: String = "Ready",
  val error: String? = null
)

data class ExportSettings(
  val resolution: String = "Original",
  val fps: Double = 0.0, // 0.0 means source FPS
  val format: String = "MP4",
  val videoCodec: String = "H.264",
  val audioCodec: String = "AAC",
  val quality: String = "High",
  val blurredBackgroundForHorizontal: Boolean = true
)

data class ExportResult(
  val success: Boolean,
  val outputUri: String? = null,
  val outputPath: String? = null,
  val durationSeconds: Double = 0.0,
  val fileSizeBytes: Long = 0L,
  val validationReport: String? = null,
  val error: String? = null
)
