package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaMetadata(
  val uri: String = "",
  val displayName: String = "Video",
  val durationMs: Long = 0L,
  val durationSeconds: Double = 0.0,
  val fps: Double = 24.02421,
  val width: Int = 1080,
  val height: Int = 1920,
  val rotation: Int = 0,
  val hasAudio: Boolean = true,
  val audioChannels: Int = 2,
  val audioSampleRate: Int = 44100,
  val bitrate: Long = 0L,
  val fileSizeBytes: Long = 0L
) {
  val isVertical: Boolean
    get() {
      val effectiveWidth = if (rotation == 90 || rotation == 270) height else width
      val effectiveHeight = if (rotation == 90 || rotation == 270) width else height
      return effectiveHeight >= effectiveWidth
    }

  val totalFrames: Long
    get() = (durationSeconds * fps).toLong().coerceAtLeast(1L)
}
