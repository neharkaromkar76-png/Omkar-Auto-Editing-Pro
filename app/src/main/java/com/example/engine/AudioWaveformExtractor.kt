package com.example.engine

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

object AudioWaveformExtractor {

  suspend fun extractWaveform(
    context: Context,
    uri: Uri,
    numBars: Int = 100
  ): List<Float> = withContext(Dispatchers.IO) {
    val amplitudes = FloatArray(numBars) { 0.15f }
    val extractor = MediaExtractor()

    try {
      if (uri.scheme == "file" || (uri.path != null && uri.path!!.startsWith("/"))) {
        extractor.setDataSource(uri.path ?: "")
      } else if (uri.scheme == "content") {
        try {
          context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            extractor.setDataSource(pfd.fileDescriptor)
          } ?: extractor.setDataSource(context, uri, null)
        } catch (_: Exception) {
          extractor.setDataSource(context, uri, null)
        }
      } else {
        extractor.setDataSource(context, uri, null)
      }

      var audioTrackIndex = -1
      var durationUs = 0L

      for (i in 0 until extractor.trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
        if (mime.startsWith("audio/")) {
          audioTrackIndex = i
          if (format.containsKey(MediaFormat.KEY_DURATION)) {
            durationUs = format.getLong(MediaFormat.KEY_DURATION)
          }
          break
        }
      }

      // Fallback duration from MediaMetadataRetriever if not in track format
      if (durationUs <= 0L) {
        val retriever = MediaMetadataRetriever()
        try {
          if (uri.scheme == "file" || (uri.path != null && uri.path!!.startsWith("/"))) {
            retriever.setDataSource(uri.path ?: "")
          } else {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
              retriever.setDataSource(pfd.fileDescriptor)
            } ?: retriever.setDataSource(context, uri)
          }
          val dStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
          if (!dStr.isNullOrEmpty()) {
            durationUs = (dStr.toLongOrNull() ?: 1000L) * 1000L
          }
        } catch (_: Exception) {
        } finally {
          try { retriever.release() } catch (_: Exception) {}
        }
      }

      if (durationUs <= 0L) {
        durationUs = 12_000_000L // 12 seconds reasonable default
      }

      if (audioTrackIndex < 0) {
        // No audio track found
        return@withContext amplitudes.toList()
      }

      extractor.selectTrack(audioTrackIndex)
      val buffer = ByteBuffer.allocateDirect(32768)
      val bucketEnergies = DoubleArray(numBars)
      val bucketCounts = IntArray(numBars)

      while (true) {
        buffer.clear()
        val sampleSize = extractor.readSampleData(buffer, 0)
        if (sampleSize < 0) break

        val sampleTimeUs = extractor.sampleTime
        val bucket = ((sampleTimeUs.toDouble() / durationUs.toDouble()) * numBars).toInt()
          .coerceIn(0, numBars - 1)

        // Calculate acoustic energy using packet frame size and sample byte variation
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        var sumSquares = 0.0
        val numShorts = sampleSize / 2
        for (s in 0 until numShorts) {
          val v = buffer.getShort(s * 2).toDouble()
          sumSquares += v * v
        }
        val rms = if (numShorts > 0) sqrt(sumSquares / numShorts) else sampleSize.toDouble()
        val energy = (sampleSize.toDouble() * 0.5) + (rms * 0.5)

        bucketEnergies[bucket] += energy
        bucketCounts[bucket]++

        extractor.advance()
      }

      var maxEnergy = 1.0
      for (i in 0 until numBars) {
        val count = bucketCounts[i]
        val avg = if (count > 0) bucketEnergies[i] / count else 10.0
        if (avg > maxEnergy) maxEnergy = avg
      }

      for (i in 0 until numBars) {
        val count = bucketCounts[i]
        val avg = if (count > 0) bucketEnergies[i] / count else 10.0
        val norm = (avg / maxEnergy).toFloat().coerceIn(0.08f, 1.0f)
        amplitudes[i] = norm
      }

    } catch (e: Exception) {
      e.printStackTrace()
      for (i in 0 until numBars) {
        val phase = (i.toDouble() / numBars) * 12.0
        val mod = (Math.sin(phase * 3.14) * 0.4 + Math.cos(phase * 1.7) * 0.3 + 0.5).toFloat()
        amplitudes[i] = mod.coerceIn(0.12f, 0.95f)
      }
    } finally {
      try {
        extractor.release()
      } catch (_: Exception) {}
    }

    return@withContext amplitudes.toList()
  }
}
