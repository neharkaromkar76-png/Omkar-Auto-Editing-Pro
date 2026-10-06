package com.example.ui.components

import android.net.Uri
import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.engine.KeyframeEngine
import com.example.model.EditTimeline
import com.example.model.MediaMetadata
import com.example.model.ZoomKeyframe
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkVoid
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlassHighlight
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs

@OptIn(UnstableApi::class)
@Composable
fun VideoPreviewCanvas(
  metadata: MediaMetadata?,
  timeline: EditTimeline? = null,
  keyframes: List<ZoomKeyframe> = emptyList(),
  currentScale: Double = 1.0,
  currentTimeMs: Long,
  currentFrame: Long,
  isPlaying: Boolean,
  onTogglePlayPause: () -> Unit,
  onPositionUpdate: (Long) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val uriString = metadata?.uri ?: ""
  var isFullscreenMode by remember { mutableStateOf(false) }
  var showDebugStatus by remember { mutableStateOf(true) }

  // Hardware-accelerated single ExoPlayer instance per active preview
  val exoPlayer = remember(context) {
    ExoPlayer.Builder(context).build().apply {
      repeatMode = Player.REPEAT_MODE_ALL
    }
  }

  DisposableEffect(exoPlayer) {
    onDispose {
      try {
        exoPlayer.stop()
        exoPlayer.release()
      } catch (_: Exception) {}
    }
  }

  // Set media source whenever URI changes
  LaunchedEffect(uriString) {
    if (uriString.isNotBlank()) {
      try {
        val mediaItem = MediaItem.fromUri(Uri.parse(uriString))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  // Play/Pause state synchronization
  LaunchedEffect(isPlaying) {
    try {
      if (isPlaying && !exoPlayer.isPlaying) {
        exoPlayer.play()
      } else if (!isPlaying && exoPlayer.isPlaying) {
        exoPlayer.pause()
      }
    } catch (_: Exception) {}
  }

  // Deliberate user seek handling (when paused or during user scrubbing)
  LaunchedEffect(currentTimeMs) {
    try {
      if (!isPlaying && abs(exoPlayer.currentPosition - currentTimeMs) > 100) {
        exoPlayer.seekTo(currentTimeMs)
      }
    } catch (_: Exception) {}
  }

  // Hardware-timed position reporting loop while playing (smooth ~60fps UI playhead & zoom synchronization)
  LaunchedEffect(isPlaying) {
    while (isPlaying) {
      val pos = exoPlayer.currentPosition
      onPositionUpdate(pos)
      delay(16) // Smooth 60fps frame updates
    }
  }

  // Consume the canonical timeline & keyframes list directly in the playback controller
  // applying 1.00x -> 0.70x -> 1.00x at detected speech boundary timestamps with smooth ease-out
  val activeScale = remember(currentTimeMs, timeline, keyframes, currentScale) {
    if (timeline != null && timeline.zoomEvents.isNotEmpty()) {
      val timeSec = currentTimeMs / 1000.0
      val fps = metadata?.previewFps ?: 24.0
      KeyframeEngine.getScaleAtTime(timeSec, fps, timeline.zoomEvents, timeline.styleProfile.normalScale)
    } else if (keyframes.isNotEmpty()) {
      val timeSec = currentTimeMs / 1000.0
      KeyframeEngine.getScaleFromKeyframes(timeSec, keyframes, normalScale = 1.00)
    } else {
      currentScale
    }
  }

  val isZoomedOut = activeScale < 0.98

  // Outer 3D Cinema Stage
  Box(
    modifier = modifier
      .shadow(
        elevation = 20.dp,
        shape = RoundedCornerShape(24.dp),
        spotColor = if (isZoomedOut) AmberGold.copy(alpha = 0.5f) else NeonPurple.copy(alpha = 0.35f),
        ambientColor = Color.Black
      )
      .clip(RoundedCornerShape(24.dp))
      .background(DarkVoid)
      .border(
        width = 1.dp,
        brush = Brush.linearGradient(
          listOf(
            if (isZoomedOut) GoldHighlight.copy(alpha = 0.6f) else ElectricBlueGlow.copy(alpha = 0.4f),
            NeonPurpleGlow.copy(alpha = 0.3f),
            Color.Transparent
          )
        ),
        shape = RoundedCornerShape(24.dp)
      )
      .padding(8.dp),
    contentAlignment = Alignment.Center
  ) {
    // 9:16 Vertical Video Bezel with soft reflection
    Box(
      modifier = Modifier
        .fillMaxSize(if (isFullscreenMode) 1f else 0.95f)
        .aspectRatio(9f / 16f)
        .shadow(16.dp, RoundedCornerShape(18.dp), spotColor = Color.Black)
        .clip(RoundedCornerShape(18.dp))
        .background(Color.Black)
        .border(
          width = 1.5.dp,
          brush = Brush.verticalGradient(
            listOf(
              if (isZoomedOut) GoldHighlight else ElectricBlueGlow.copy(alpha = 0.6f),
              DarkSurface
            )
          ),
          shape = RoundedCornerShape(18.dp)
        )
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onTogglePlayPause
        ),
      contentAlignment = Alignment.Center
    ) {
      // Hardware Video Layer with dynamic keyframe zoom applied directly on GPU compositor
      // Using TextureView surface allows Compose graphicsLayer scaleX & scaleY to physically zoom the video
      Box(
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer {
            scaleX = activeScale.toFloat()
            scaleY = activeScale.toFloat()
          }
      ) {
        AndroidView(
          modifier = Modifier
            .fillMaxSize()
            .testTag("video_player_surface"),
          factory = { ctx ->
            val view = LayoutInflater.from(ctx).inflate(R.layout.view_exo_player, null) as PlayerView
            view.player = exoPlayer
            view
          },
          update = { playerView ->
            if (playerView.player != exoPlayer) {
              playerView.player = exoPlayer
            }
          }
        )
      }

      // Play/Pause Center Indicator (Elevated 3D circle with micro-fade)
      AnimatedVisibility(
        visible = !isPlaying,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.Center)
      ) {
        Box(
          modifier = Modifier
            .size(60.dp)
            .shadow(12.dp, CircleShape, spotColor = NeonPurpleGlow)
            .background(DarkSurface.copy(alpha = 0.85f), CircleShape)
            .border(
              1.dp,
              Brush.linearGradient(listOf(Color.White.copy(alpha = 0.7f), Color.Transparent)),
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play Video",
            tint = Color.White,
            modifier = Modifier.size(34.dp)
          )
        }
      }

      // Live Telemetry Overlay in Top-Left (Timestamp, Frame, Real-Time Zoom Scale)
      Box(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(12.dp)
          .shadow(8.dp, RoundedCornerShape(10.dp), spotColor = Color.Black)
          .clip(RoundedCornerShape(10.dp))
          .background(DarkVoid.copy(alpha = 0.78f))
          .border(0.5.dp, if (isZoomedOut) GoldHighlight else GlassHighlight, RoundedCornerShape(10.dp))
          .padding(horizontal = 10.dp, vertical = 6.dp)
          .testTag("live_telemetry_hud")
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .background(if (isPlaying) EmeraldGlow else Slate500, CircleShape)
            )
            val sec = currentTimeMs / 1000
            val millis = (currentTimeMs % 1000) / 10
            Text(
              text = String.format("%02d:%02d.%02d", sec / 60, sec % 60, millis),
              color = Color.White,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "F#$currentFrame",
              color = Slate400,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "•",
              color = Slate500,
              fontSize = 10.sp
            )
            Text(
              text = "ZOOM ${String.format(Locale.US, "%.2f", activeScale)}x",
              color = if (isZoomedOut) GoldHighlight else CyanHighlight,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.ExtraBold
            )
          }
        }
      }

      // Zoom-Out Wide Indicator Badge in Top-Right
      AnimatedVisibility(
        visible = isZoomedOut,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(12.dp)
      ) {
        Box(
          modifier = Modifier
            .shadow(10.dp, RoundedCornerShape(12.dp), spotColor = AmberGold)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(AmberGold, GoldHighlight)))
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("wide_zoom_badge")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ZoomOut,
              contentDescription = null,
              tint = Color.Black,
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = "0.70x WIDE CUT",
              color = Color.Black,
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 0.5.sp
            )
          }
        }
      }

      // Requirement 11: AUTO EDIT STATUS Debug Information Panel
      AnimatedVisibility(
        visible = showDebugStatus,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(start = 12.dp, bottom = 48.dp)
      ) {
        Box(
          modifier = Modifier
            .shadow(12.dp, RoundedCornerShape(10.dp), spotColor = Color.Black)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkVoid.copy(alpha = 0.88f))
            .border(1.dp, NeonPurpleGlow.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("auto_edit_debug_panel")
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = "AUTO EDIT STATUS",
              color = CyberCyan,
              fontSize = 9.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 0.6.sp
            )
            val fpsText = metadata?.fps?.let { String.format(Locale.US, "%.1f", it) } ?: "24.0"
            val durText = metadata?.durationSeconds?.let { String.format(Locale.US, "%.1fs", it) } ?: "0.0s"
            val audioText = if (metadata?.hasAudio == true) "YES" else "NO"
            val wordCount = timeline?.segments?.sumOf { it.words.size } ?: 0
            val bCount = timeline?.boundaries?.size ?: 0
            val zoomCount = timeline?.zoomEvents?.size ?: 0
            val kCount = timeline?.zoomEvents?.sumOf { it.keyframes.size } ?: 0

            Text(
              text = "Media: $fpsText FPS | $durText | Aud: $audioText",
              color = Slate400,
              fontSize = 8.5.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "AI: Words: $wordCount | Bounds: $bCount",
              color = Slate400,
              fontSize = 8.5.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Timeline: Zooms: $zoomCount | Keyframes: $kCount",
              color = Slate400,
              fontSize = 8.5.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Preview: Active Zoom: ${String.format(Locale.US, "%.2f", activeScale)}x",
              color = if (isZoomedOut) GoldHighlight else EmeraldGlow,
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      // Bottom Right Controls (Fullscreen & Debug HUD toggle)
      Row(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        IconButton(
          onClick = { showDebugStatus = !showDebugStatus },
          modifier = Modifier
            .size(34.dp)
            .background(DarkSurface.copy(alpha = 0.7f), CircleShape)
            .border(0.5.dp, GlassHighlight, CircleShape)
            .testTag("debug_hud_toggle_btn")
        ) {
          Icon(
            imageVector = Icons.Default.BugReport,
            contentDescription = "Toggle Debug Status",
            tint = if (showDebugStatus) CyberCyan else Slate400,
            modifier = Modifier.size(16.dp)
          )
        }

        IconButton(
          onClick = { isFullscreenMode = !isFullscreenMode },
          modifier = Modifier
            .size(34.dp)
            .background(DarkSurface.copy(alpha = 0.7f), CircleShape)
            .border(0.5.dp, GlassHighlight, CircleShape)
            .testTag("fullscreen_toggle_btn")
        ) {
          Icon(
            imageVector = if (isFullscreenMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
            contentDescription = "Toggle Fullscreen",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}
