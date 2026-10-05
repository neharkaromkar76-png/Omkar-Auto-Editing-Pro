package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditTimeline
import com.example.model.SpeechBoundary
import com.example.model.ZoomEvent
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberZoom
import com.example.ui.theme.Cinematic3DIconContainer
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkVoid
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldCut
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassHighlight
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlowBadge
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.KeyframeDiamond
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.theme.PlayheadRed
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.TimelineBackground
import com.example.ui.theme.WaveformActive
import com.example.ui.theme.WaveformInactive
import kotlin.math.roundToLong

@Composable
fun TimelineTrackView(
  timeline: EditTimeline?,
  waveform: List<Float>,
  currentTimeMs: Long,
  currentFrame: Long,
  isPlaying: Boolean,
  selectedZoomEvent: ZoomEvent?,
  onTogglePlayPause: () -> Unit,
  onSeekTo: (Long) -> Unit,
  onSelectZoomEvent: (ZoomEvent?) -> Unit,
  onAddSplitAtPlayhead: () -> Unit,
  onDeleteSelectedSplit: () -> Unit,
  modifier: Modifier = Modifier
) {
  val durationSec = timeline?.source?.durationSeconds?.coerceAtLeast(1.0) ?: 10.0
  val durationMs = (durationSec * 1000).toLong()

  // Timeline width scaling: 80 pixels per second gives clean spacious editing
  val pixelsPerSec = 75f
  val totalWidthDp = ((durationSec * pixelsPerSec).toFloat()).coerceAtLeast(400f).dp

  val progress = (currentTimeMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .shadow(16.dp, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp), spotColor = NeonPurple.copy(alpha = 0.3f))
      .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
      .background(DarkVoid)
      .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
          listOf(ElectricBlueGlow.copy(alpha = 0.4f), DarkSurface)
        ),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
      )
  ) {
    // 1. Timeline Header Bar: 3D Transport Controls & Quick Split Actions
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkSurface)
        .padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // 3D Play/Pause Button
        Box(
          modifier = Modifier
            .size(38.dp)
            .shadow(8.dp, CircleShape, spotColor = ElectricBlueGlow)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(ElectricBlue, NeonPurple)))
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onTogglePlayPause)
            .testTag("play_pause_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }

        // Add Split at Playhead Button (3D Pill)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Slate850, Slate800)))
            .border(1.dp, GoldHighlight.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .clickable(onClick = onAddSplitAtPlayhead)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("add_split_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.ContentCut, contentDescription = null, tint = GoldHighlight, modifier = Modifier.size(13.dp))
            Text("Split at Playhead", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        // Delete Selected Split Button
        if (selectedZoomEvent != null) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(Color(0xFF7F1D1D).copy(alpha = 0.5f))
              .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
              .clickable(onClick = onDeleteSelectedSplit)
              .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(13.dp))
              Text("Delete Split", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Live Timecode & Frame Indicator
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        val totalSec = currentTimeMs / 1000.0
        val mins = (totalSec / 60).toInt()
        val secs = totalSec % 60
        Text(
          text = String.format("%02d:%05.2fs", mins, secs),
          color = CyanHighlight,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    // 2. Multi-Track Scrollable Timeline Canvas
    val horizontalScrollState = rememberScrollState()

    Row(modifier = Modifier.fillMaxWidth()) {
      // Fixed Left Track Labels: VIDEO, AUDIO, AI SPEECH BOUNDARIES, ZOOM KEYFRAMES
      Column(
        modifier = Modifier
          .width(88.dp)
          .background(DarkSurface.copy(alpha = 0.95f))
          .border(
            width = 1.dp,
            brush = Brush.horizontalGradient(listOf(Color.Transparent, Slate800)),
            shape = RoundedCornerShape(0.dp)
          )
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        TrackLabelHeader("RULER", 20)
        TrackLabelHeader("VIDEO", 36)
        TrackLabelHeader("AUDIO", 40)
        TrackLabelHeader("SPEECH", 34)
        TrackLabelHeader("ZOOM 0.70x", 48)
      }

      // Horizontal Scrollable Multi-Track Content
      Box(
        modifier = Modifier
          .weight(1f)
          .horizontalScroll(horizontalScrollState)
      ) {
        Box(
          modifier = Modifier
            .width(totalWidthDp)
            .pointerInput(durationMs) {
              detectTapGestures { offset ->
                val tappedProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                onSeekTo((tappedProgress * durationMs).toLong())
              }
            }
            .pointerInput(durationMs) {
              detectDragGestures { change, _ ->
                change.consume()
                val draggedProgress = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                onSeekTo((draggedProgress * durationMs).toLong())
              }
            }
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            // TRACK 0: Timecode Ruler
            TimecodeRuler(durationSec = durationSec, modifier = Modifier.height(20.dp).fillMaxWidth())

            // TRACK 1: VIDEO (Strip with source frames indication)
            VideoFramesTrack(durationSec = durationSec, modifier = Modifier.height(36.dp).fillMaxWidth())

            // TRACK 2: AUDIO (Real RMS audio waveform with active/inactive coloring)
            WaveformTrack(
              waveform = waveform,
              progress = progress,
              modifier = Modifier.height(40.dp).fillMaxWidth()
            )

            // TRACK 3: AI SPEECH BOUNDARIES (Spoken sentence blocks and glowing boundary markers)
            SpeechSegmentsTrack(
              timeline = timeline,
              durationSec = durationSec,
              modifier = Modifier.height(34.dp).fillMaxWidth(),
              onSelectEvent = onSelectZoomEvent
            )

            // TRACK 4: ZOOM KEYFRAMES (Visual cubic ease-out curve from 1.00x -> 0.70x -> 1.00x)
            ZoomKeyframeCurveTrack(
              timeline = timeline,
              durationSec = durationSec,
              selectedZoomEvent = selectedZoomEvent,
              modifier = Modifier.height(48.dp).fillMaxWidth(),
              onSelectEvent = onSelectZoomEvent
            )
          }

          // Draggable Glowing Red Playhead across all tracks
          val playheadOffset = (progress * totalWidthDp.value).dp
          Box(
            modifier = Modifier
              .offset { IntOffset(x = playheadOffset.roundToPx() - 7, y = 0) }
              .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
          ) {
            // Playhead Diamond Head
            Box(
              modifier = Modifier
                .size(14.dp)
                .shadow(6.dp, CircleShape, spotColor = PlayheadRed)
                .background(PlayheadRed, CircleShape)
                .border(1.dp, Color.White, CircleShape)
            )

            // Playhead Vertical Line
            Box(
              modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(PlayheadRed)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TrackLabelHeader(title: String, heightDp: Int) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(heightDp.dp)
      .padding(horizontal = 6.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Text(
      text = title,
      color = Slate400,
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      letterSpacing = 0.5.sp
    )
  }
}

/**
 * Timecode Ruler with tick marks every 0.5s and labels every 1s.
 */
@Composable
private fun TimecodeRuler(durationSec: Double, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val totalWidth = size.width
    val secondIntervalPx = totalWidth / durationSec.toFloat()

    var sec = 0.0
    while (sec <= durationSec) {
      val x = (sec * secondIntervalPx).toFloat()
      val isWholeSec = sec % 1.0 == 0.0

      drawLine(
        color = if (isWholeSec) Color.White.copy(alpha = 0.6f) else Slate700,
        start = Offset(x, if (isWholeSec) 0f else 8f),
        end = Offset(x, size.height),
        strokeWidth = if (isWholeSec) 1.5f else 1f
      )
      sec += 0.5
    }
  }
}

/**
 * Video frames track representation.
 */
@Composable
private fun VideoFramesTrack(durationSec: Double, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(Slate850)
      .border(0.5.dp, Slate700, RoundedCornerShape(6.dp))
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val frameWidth = 44f
      var x = 0f
      while (x < w) {
        drawLine(
          color = Slate700,
          start = Offset(x, 0f),
          end = Offset(x, h),
          strokeWidth = 1f
        )
        x += frameWidth
      }
    }
  }
}

/**
 * Real RMS Waveform Track.
 */
@Composable
private fun WaveformTrack(
  waveform: List<Float>,
  progress: Float,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(DarkSurface)
      .border(0.5.dp, Slate800, RoundedCornerShape(6.dp))
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val numBars = if (waveform.isNotEmpty()) waveform.size else 60
      val barWidth = (w / numBars.toFloat()) * 0.7f
      val spacing = (w / numBars.toFloat()) * 0.3f
      val playheadX = progress * w

      for (i in 0 until numBars) {
        val energy = if (i < waveform.size) waveform[i].coerceIn(0.08f, 1.0f) else 0.25f
        val barHeight = (energy * (h * 0.85f)).coerceAtLeast(4f)
        val x = i * (barWidth + spacing)
        val y = (h - barHeight) / 2f
        val isPlayed = x <= playheadX

        drawRoundRect(
          color = if (isPlayed) CyanHighlight else Slate700,
          topLeft = Offset(x, y),
          size = Size(barWidth, barHeight),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
      }
    }
  }
}

/**
 * AI Speech Segments Track showing spoken thought blocks.
 */
@Composable
private fun SpeechSegmentsTrack(
  timeline: EditTimeline?,
  durationSec: Double,
  modifier: Modifier = Modifier,
  onSelectEvent: (ZoomEvent?) -> Unit
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(Slate850)
      .border(0.5.dp, Slate800, RoundedCornerShape(6.dp))
  ) {
    val segments = timeline?.segments ?: emptyList()
    val boundaries = timeline?.boundaries ?: emptyList()

    // Render speech blocks
    segments.forEach { seg ->
      val leftRatio = (seg.startTime / durationSec).toFloat().coerceIn(0f, 1f)
      val rightRatio = (seg.endTime / durationSec).toFloat().coerceIn(0f, 1f)
      val widthRatio = (rightRatio - leftRatio).coerceAtLeast(0.02f)

      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(fraction = widthRatio)
          .offset { IntOffset(x = (leftRatio * 1000).toInt(), y = 0) }
          .padding(vertical = 3.dp, horizontal = 2.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(NeonPurple.copy(alpha = 0.35f))
          .border(0.5.dp, NeonPurpleGlow.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
          .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Text(
          text = seg.text,
          color = Color.White,
          fontSize = 9.sp,
          maxLines = 1,
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.Medium
        )
      }
    }

    // Glowing boundary split markers
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      boundaries.forEach { b ->
        val x = (b.time / durationSec).toFloat() * w

        // Glowing boundary line
        drawLine(
          color = GoldHighlight,
          start = Offset(x, 0f),
          end = Offset(x, h),
          strokeWidth = 2.5f
        )
      }
    }
  }
}

/**
 * Keyframe Track with visual cubic ease-out curve (1.00x -> 0.70x -> 1.00x).
 */
@Composable
private fun ZoomKeyframeCurveTrack(
  timeline: EditTimeline?,
  durationSec: Double,
  selectedZoomEvent: ZoomEvent?,
  modifier: Modifier = Modifier,
  onSelectEvent: (ZoomEvent?) -> Unit
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(DarkSurface)
      .border(0.5.dp, Slate800, RoundedCornerShape(6.dp))
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      // Normal baseline at 1.00x (top baseline)
      val normalY = h * 0.25f
      val wideY = h * 0.85f

      drawLine(
        color = Slate700,
        start = Offset(0f, normalY),
        end = Offset(w, normalY),
        strokeWidth = 1f
      )

      val zoomEvents = timeline?.zoomEvents ?: emptyList()
      val fps = timeline?.source?.fps ?: 24.0

      zoomEvents.forEach { event ->
        val startSec = event.boundaryFrame / fps
        val recoveryFrames = event.durationFrames
        val endSec = (event.boundaryFrame + recoveryFrames) / fps

        val startX = (startSec / durationSec).toFloat() * w
        val endX = (endSec / durationSec).toFloat() * w

        val isSelected = selectedZoomEvent?.boundaryFrame == event.boundaryFrame

        // Draw instant drop: (startX, normalY) -> (startX, wideY)
        drawLine(
          color = if (isSelected) GoldHighlight else CyanHighlight,
          start = Offset(startX, normalY),
          end = Offset(startX, wideY),
          strokeWidth = 2.5f
        )

        // Draw cubic ease-out curve recovery from wideY back to normalY
        val path = Path().apply {
          moveTo(startX, wideY)
          val steps = 10
          val dx = (endX - startX) / steps
          for (s in 1..steps) {
            val u = s / steps.toFloat()
            // Cubic ease-out: f(u) = 1 - (1 - u)^3
            val eased = 1f - (1f - u) * (1f - u) * (1f - u)
            val curY = wideY - (eased * (wideY - normalY))
            val curX = startX + (s * dx)
            lineTo(curX, curY)
          }
        }

        drawPath(
          path = path,
          color = if (isSelected) GoldHighlight else CyanHighlight,
          style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        // Diamond marker at the split moment
        drawCircle(
          color = if (isSelected) GoldHighlight else AmberGold,
          radius = 5f,
          center = Offset(startX, wideY)
        )
      }
    }

    // Keyframe text label
    Text(
      text = "1.00x → 0.70x → 0.75x → 0.82x → 0.90x → 0.96x → 1.00x",
      color = Slate500,
      fontSize = 8.sp,
      fontFamily = FontFamily.Monospace,
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(horizontal = 6.dp, vertical = 2.dp)
    )
  }
}
