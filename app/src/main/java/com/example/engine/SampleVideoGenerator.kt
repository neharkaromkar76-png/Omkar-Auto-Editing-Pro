package com.example.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.sin

object SampleVideoGenerator {

  /**
   * Generates a real vertical 9:16 MP4 video sample with speech timestamps
   * and clean visual composition for testing and initial demonstration.
   */
  suspend fun getOrCreateSampleVideo(context: Context): Uri = withContext(Dispatchers.IO) {
    val sampleFile = File(context.filesDir, "sample_speech_reference.mp4")
    if (sampleFile.exists() && sampleFile.length() > 50000) {
      return@withContext Uri.fromFile(sampleFile)
    }

    val width = 540
    val height = 960
    val fps = 24
    val durationSeconds = 12
    val totalFrames = fps * durationSeconds
    val bitRate = 2000000

    val mime = MediaFormat.MIMETYPE_VIDEO_AVC
    val format = MediaFormat.createVideoFormat(mime, width, height).apply {
      setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
      setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
      setInteger(MediaFormat.KEY_FRAME_RATE, fps)
      setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
    }

    var encoder: MediaCodec? = null
    var muxer: MediaMuxer? = null

    try {
      encoder = MediaCodec.createEncoderByType(mime)
      encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
      val inputSurface = encoder.createInputSurface()
      encoder.start()

      muxer = MediaMuxer(sampleFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
      var trackIndex = -1
      var muxerStarted = false

      val bufferInfo = MediaCodec.BufferInfo()
      val paint = Paint(Paint.ANTI_ALIAS_FLAG)
      val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 34f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
      }
      val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(200, 210, 230)
        textSize = 22f
        textAlign = Paint.Align.CENTER
      }

      val sentences = listOf(
        "Here is how modern creators edit videos.",
        "Every speech cut captures the audience.",
        "Notice the 0.70x wide-frame transition.",
        "Smooth cubic ease-out zoom returns to normal."
      )

      for (frameIndex in 0 until totalFrames) {
        val timeSec = frameIndex.toDouble() / fps
        val sentenceIdx = ((timeSec / durationSeconds) * sentences.size).toInt().coerceIn(0, sentences.size - 1)
        val currentSentence = sentences[sentenceIdx]

        // Render frame to input surface
        val canvas: Canvas? = inputSurface.lockHardwareCanvas()
        if (canvas != null) {
          // Dynamic deep blue-violet cinematic background
          canvas.drawColor(Color.rgb(15, 23, 42))

          // Studio vertical card
          paint.color = Color.rgb(30, 41, 59)
          val cardRect = RectF(40f, 100f, (width - 40).toFloat(), (height - 100).toFloat())
          canvas.drawRoundRect(cardRect, 32f, 32f, paint)

          // Center presenter avatar placeholder
          paint.color = Color.rgb(99, 102, 241)
          val avatarY = height * 0.38f
          canvas.drawCircle(width / 2f, avatarY, 110f, paint)

          // Inner facial silhouette
          paint.color = Color.WHITE
          canvas.drawCircle(width / 2f, avatarY - 20f, 40f, paint)
          canvas.drawRoundRect(
            RectF(width / 2f - 60f, avatarY + 25f, width / 2f + 60f, avatarY + 80f),
            30f, 30f, paint
          )

          // Header badge
          paint.color = Color.rgb(16, 185, 129)
          canvas.drawRoundRect(
            RectF(width / 2f - 140f, 140f, width / 2f + 140f, 190f),
            25f, 25f, paint
          )
          val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
          }
          canvas.drawText("AI SPEECH CALIBRATION", width / 2f, 174f, badgePaint)

          // Subtitle card with current sentence
          paint.color = Color.rgb(15, 23, 42)
          val subRect = RectF(60f, height * 0.65f, (width - 60).toFloat(), height * 0.78f)
          canvas.drawRoundRect(subRect, 20f, 20f, paint)

          canvas.drawText(currentSentence, width / 2f, height * 0.72f, textPaint)
          canvas.drawText(
            "Time: ${String.format("%.2f", timeSec)}s | Frame: $frameIndex",
            width / 2f,
            height * 0.84f,
            subTextPaint
          )

          // Waveform bar graphics at bottom
          paint.color = Color.rgb(129, 140, 248)
          for (b in 0 until 24) {
            val bx = 80f + (b * 16f)
            val barHeight = (20f + 35f * sin((frameIndex * 0.2) + (b * 0.5))).toFloat().coerceAtLeast(6f)
            canvas.drawRoundRect(
              RectF(bx, height * 0.89f - barHeight, bx + 10f, height * 0.89f + barHeight),
              5f, 5f, paint
            )
          }

          inputSurface.unlockCanvasAndPost(canvas)
        }

        // Drain encoder
        while (true) {
          val outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 0)
          if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            trackIndex = muxer.addTrack(encoder.outputFormat)
            muxer.start()
            muxerStarted = true
          } else if (outputIndex >= 0) {
            val encodedData = encoder.getOutputBuffer(outputIndex)
            if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
              encodedData.position(bufferInfo.offset)
              encodedData.limit(bufferInfo.offset + bufferInfo.size)
              muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
            }
            encoder.releaseOutputBuffer(outputIndex, false)
          } else {
            break
          }
        }
      }

      encoder.signalEndOfInputStream()

      // Drain remaining EOS
      var eos = false
      while (!eos) {
        val outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
        if (outputIndex >= 0) {
          if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            eos = true
          }
          val encodedData = encoder.getOutputBuffer(outputIndex)
          if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
            encodedData.position(bufferInfo.offset)
            encodedData.limit(bufferInfo.offset + bufferInfo.size)
            muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
          }
          encoder.releaseOutputBuffer(outputIndex, false)
        } else if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
          break
        }
      }

    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      try {
        encoder?.stop()
        encoder?.release()
      } catch (_: Exception) {}
      try {
        muxer?.stop()
        muxer?.release()
      } catch (_: Exception) {}
    }

    return@withContext Uri.fromFile(sampleFile)
  }
}
