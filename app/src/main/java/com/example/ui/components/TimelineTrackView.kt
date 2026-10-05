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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.example.ui.theme.AmberZoom
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSlate950
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldCut
import com.example.ui.theme.KeyframeDiamond
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
      .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
      .background(TimelineBackground)
      .border(1.dp, Slate800, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
  ) {
    // Timeline Header Bar: Transport Controls & Quick Split Actions
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(DeepSlate950)
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Play/Pause Button
        Surface(
          shape = CircleShape,
          color = ElectricIndigo,
          modifier = Modifier.size(36.dp)
        ) {
          IconButton(
            onClick = onTogglePlayPause,
            modifier = Modifier.testTag("play_pause_button")
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Add Split at Playhead Button
        Button(
          onClick = onAddSplitAtPlayhead,
          colors = ButtonDefaults.buttonColors(
            containerColor = Slate800,
            contentColor = AmberZoom
          ),
          shape = RoundedCornerShape(8.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.testTag("add_split_button")
        ) {
          Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(Modifier.width(4.dp))
          Text("Split at Playhead", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Delete Split Button if selected
        if (selectedZoomEvent != null) {
          Button(
            onClick = onDeleteSelectedSplit,
            colors = ButtonDefaults.buttonColors(
              containerColor = Slate850,
              contentColor = Color.Red.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.testTag("delete_split_button")
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text("Delete Split", fontSize = 11.sp)
          }
        }
      }

      // Timecode and Splits summary
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        val totalSec = currentTimeMs / 1000.0
        val mins = (totalSec / 60).toInt()
        val secs = totalSec % 60
        Text(
          text = String.format("%02d:%05.2f", mins, secs),
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Slate850
        ) {
          Text(
            text = "${timeline?.zoomEvents?.size ?: 0} ZOOMS",
            color = AmberZoom,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          )
        }
      }
    }

    // Scrollable Multi-Track Timeline Canvas
    val scrollState = rememberScrollState()

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(175.dp)
        .horizontalScroll(scrollState)
        .testTag("timeline_scroll_area")
    ) {
      Box(
        modifier = Modifier
          .width(totalWidthDp)
          .fillMaxHeight()
          .pointerInput(durationMs) {
            detectTapGestures { offset ->
              val clickProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
              onSeekTo((clickProgress * durationMs).toLong())
            }
          }
          .pointerInput(durationMs) {
            detectDragGestures { change, _ ->
              val dragProgress = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
              onSeekTo((dragProgress * durationMs).toLong())
            }
          }
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Track 1: Timecode Ruler (Height: 22dp)
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(22.dp)
              .background(DeepSlate950)
          ) {
            val totalSeconds = durationSec.toInt()
            val step = if (totalSeconds > 60) 5 else if (totalSeconds > 25) 2 else 1
            for (sec in 0..totalSeconds step step) {
              val x = (sec / durationSec.toFloat()) * size.width
              drawLine(
                color = Slate700,
                start = Offset(x, size.height - 8f),
                end = Offset(x, size.height),
                strokeWidth = 1.5f
              )
            }
          }

          // Track 2: Video Track (Height: 34dp)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(34.dp)
              .background(Slate850)
              .border(0.5.dp, Slate700)
          ) {
            Row(
              modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "VIDEO [9:16 VERTICAL]",
                color = Slate400,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Track 3: Audio Waveform Track (Height: 46dp)
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
              .background(Slate900Background())
              .border(0.5.dp, Slate800)
          ) {
            val bars = waveform
            if (bars.isNotEmpty()) {
              val barWidth = size.width / bars.size.toFloat()
              val midY = size.height / 2f
              val currentX = progress * size.width

              for (i in bars.indices) {
                val energy = bars[i]
                val barH = (energy * midY * 0.9f).coerceAtLeast(2f)
                val x = i * barWidth
                val color = if (x <= currentX) WaveformActive else WaveformInactive

                drawRoundRect(
                  color = color,
                  topLeft = Offset(x + 1f, midY - barH),
                  size = Size(barWidth - 2f, barH * 2f),
                  cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
              }
            }
          }

          // Track 4: Speech Segments Track (Height: 32dp)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(32.dp)
              .background(DeepSlate950)
              .border(0.5.dp, Slate800)
          ) {
            val segments = timeline?.segments ?: emptyList()
            for (seg in segments) {
              val startPct = (seg.startTime / durationSec).toFloat().coerceIn(0f, 1f)
              val endPct = (seg.endTime / durationSec).toFloat().coerceIn(0f, 1f)
              val spanPct = (endPct - startPct).coerceAtLeast(0.01f)

              Box(
                modifier = Modifier
                  .fillMaxHeight()
                  .fillMaxWidth(spanPct)
                  .offset { IntOffset((startPct * totalWidthDp.toPx()).toInt(), 0) }
                  .padding(vertical = 3.dp, horizontal = 2.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .background(Slate800)
                  .border(0.8.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.CenterStart
              ) {
                Text(
                  text = seg.text,
                  color = Color.White,
                  fontSize = 9.sp,
                  maxLines = 1,
                  modifier = Modifier.padding(horizontal = 4.dp)
                )
              }
            }
          }

          // Track 5: Zoom Keyframe Curve & Split Markers Track (Height: 41dp)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(41.dp)
              .background(Slate850)
          ) {
            val zoomEvents = timeline?.zoomEvents ?: emptyList()
            for (event in zoomEvents) {
              val isSelected = selectedZoomEvent?.id == event.id
              val eventPct = (event.boundaryTime / durationSec).toFloat().coerceIn(0f, 1f)

              // Split marker badge
              Box(
                modifier = Modifier
                  .offset { IntOffset((eventPct * totalWidthDp.toPx()).toInt() - 25, 2) }
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSelected) AmberZoom else EmeraldCut)
                  .clickable { onSelectZoomEvent(event) }
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "${String.format("%.2f", event.wideScale)}x",
                  color = Color.Black,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        // Split Markers Overlay Lines across the entire timeline
        val zoomEvents = timeline?.zoomEvents ?: emptyList()
        for (event in zoomEvents) {
          val isSelected = selectedZoomEvent?.id == event.id
          val eventPct = (event.boundaryTime / durationSec).toFloat().coerceIn(0f, 1f)

          Box(
            modifier = Modifier
              .fillMaxHeight()
              .width(if (isSelected) 2.5.dp else 1.5.dp)
              .offset { IntOffset((eventPct * totalWidthDp.toPx()).toInt(), 0) }
              .background(if (isSelected) AmberZoom else EmeraldCut.copy(alpha = 0.8f))
          )
        }

        // Master Red Playhead Line & Scrubber Handle
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .width(2.dp)
            .offset { IntOffset((progress * totalWidthDp.toPx()).toInt(), 0) }
            .background(PlayheadRed)
        ) {
          // Playhead Triangle Top Head
          Canvas(
            modifier = Modifier
              .size(14.dp)
              .offset(x = (-6).dp, y = 0.dp)
          ) {
            val path = Path().apply {
              moveTo(0f, 0f)
              lineTo(size.width, 0f)
              lineTo(size.width / 2f, size.height)
              close()
            }
            drawPath(path, PlayheadRed)
          }
        }
      }
    }
  }
}

@Composable
private fun Slate900Background(): Color = Color(0xFF0F172A)
