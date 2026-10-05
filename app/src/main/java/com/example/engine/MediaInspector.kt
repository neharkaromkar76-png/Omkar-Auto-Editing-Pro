package com.example.engine

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.model.MediaMetadata
import java.io.File

object MediaInspector {

  fun inspect(context: Context, uri: Uri): MediaMetadata {
    val retriever = MediaMetadataRetriever()
    var durationMs = 0L
    var width = 1080
    var height = 1920
    var rotation = 0
    var hasAudio = false
    var audioChannels = 2
    var audioSampleRate = 44100
    var bitrate = 0L
    var fps = 24.02421
    var displayName = "Video"
    var fileSizeBytes = 0L

    try {
      if (uri.scheme == "file") {
        val file = File(uri.path ?: "")
        if (file.exists()) {
          fileSizeBytes = file.length()
          displayName = file.name
          retriever.setDataSource(file.absolutePath)
        } else {
          retriever.setDataSource(context, uri)
        }
      } else {
        retriever.setDataSource(context, uri)
        displayName = uri.lastPathSegment ?: "Imported Video"
      }

      val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
      if (!durStr.isNullOrEmpty()) {
        durationMs = durStr.toLongOrNull() ?: 0L
      }

      val wStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
      val hStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
      if (!wStr.isNullOrEmpty() && !hStr.isNullOrEmpty()) {
        width = wStr.toIntOrNull() ?: 1080
        height = hStr.toIntOrNull() ?: 1920
      }

      val rotStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
      if (!rotStr.isNullOrEmpty()) {
        rotation = rotStr.toIntOrNull() ?: 0
      }

      val brStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
      if (!brStr.isNullOrEmpty()) {
        bitrate = brStr.toLongOrNull() ?: 0L
      }

      val audioStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
      hasAudio = audioStr.equals("yes", ignoreCase = true)
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      try {
        retriever.release()
      } catch (_: Exception) {}
    }

    // Inspect tracks & FPS with MediaExtractor
    val extractor = MediaExtractor()
    try {
      if (uri.scheme == "file") {
        extractor.setDataSource(uri.path ?: "")
      } else {
        extractor.setDataSource(context, uri, null)
      }

      val trackCount = extractor.trackCount
      for (i in 0 until trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
        if (mime.startsWith("video/")) {
          if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
            val f = format.getInteger(MediaFormat.KEY_FRAME_RATE)
            if (f > 0) fps = f.toDouble()
          }
          if (format.containsKey(MediaFormat.KEY_WIDTH)) {
            width = format.getInteger(MediaFormat.KEY_WIDTH)
          }
          if (format.containsKey(MediaFormat.KEY_HEIGHT)) {
            height = format.getInteger(MediaFormat.KEY_HEIGHT)
          }
        } else if (mime.startsWith("audio/")) {
          hasAudio = true
          if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
            audioChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
          }
          if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
            audioSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      try {
        extractor.release()
      } catch (_: Exception) {}
    }

    val durSec = durationMs / 1000.0

    // Normalize anomalous or excessively high frame rate (e.g. 121 fps)
    val normalizedFps = when {
      fps <= 0.0 -> 24.024
      fps >= 100.0 -> 30.0
      fps > 60.0 -> 60.0
      else -> fps
    }

    return MediaMetadata(
      uri = uri.toString(),
      displayName = displayName,
      durationMs = durationMs,
      durationSeconds = durSec,
      fps = normalizedFps,
      width = width,
      height = height,
      rotation = rotation,
      hasAudio = hasAudio,
      audioChannels = audioChannels,
      audioSampleRate = audioSampleRate,
      bitrate = bitrate,
      fileSizeBytes = fileSizeBytes
    )
  }
}
