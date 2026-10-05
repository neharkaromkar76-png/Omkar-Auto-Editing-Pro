package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.view.TextureView
import android.widget.FrameLayout
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.MediaMetadata
import com.example.ui.theme.AmberZoom
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSlate950
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldCut
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate850
import kotlin.math.abs

@Composable
fun VideoPreviewCanvas(
  metadata: MediaMetadata?,
  currentScale: Double,
  currentTimeMs: Long,
  currentFrame: Long,
  isPlaying: Boolean,
  onTogglePlayPause: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val mediaPlayer = remember { MediaPlayer() }
  val uriString = metadata?.uri ?: ""

  DisposableEffect(uriString) {
    if (uriString.isNotBlank()) {
      try {
        mediaPlayer.reset()
        val uri = Uri.parse(uriString)
        if (uri.scheme == "file") {
          mediaPlayer.setDataSource(uri.path ?: "")
        } else {
          mediaPlayer.setDataSource(context, uri)
        }
        mediaPlayer.isLooping = true
        mediaPlayer.prepareAsync()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    onDispose {
      try {
        mediaPlayer.release()
      } catch (_: Exception) {}
    }
  }

  LaunchedEffect(isPlaying) {
    try {
      if (isPlaying && !mediaPlayer.isPlaying) {
        mediaPlayer.start()
      } else if (!isPlaying && mediaPlayer.isPlaying) {
        mediaPlayer.pause()
      }
    } catch (_: Exception) {}
  }

  LaunchedEffect(currentTimeMs) {
    try {
      if (abs(mediaPlayer.currentPosition - currentTimeMs) > 150) {
        mediaPlayer.seekTo(currentTimeMs.toInt())
      }
    } catch (_: Exception) {}
  }

  val isZoomedOut = currentScale < 0.98
  val scalePercent = (currentScale * 100).toInt()

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .background(DeepSlate950)
      .border(1.dp, Slate700, RoundedCornerShape(18.dp)),
    contentAlignment = Alignment.Center
  ) {
    // 9:16 Vertical Video Aspect Container
    Box(
      modifier = Modifier
        .fillMaxSize(0.92f)
        .aspectRatio(9f / 16f)
        .clip(RoundedCornerShape(14.dp))
        .background(Color.Black)
        .border(1.5.dp, if (isZoomedOut) AmberZoom else Slate700.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onTogglePlayPause
        ),
      contentAlignment = Alignment.Center
    ) {
      // Hardware Video Layer with dynamic keyframe zoom
      Box(
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer {
            scaleX = currentScale.toFloat()
            scaleY = currentScale.toFloat()
          }
      ) {
        AndroidView(
          modifier = Modifier.fillMaxSize().testTag("video_player_surface"),
          factory = { ctx ->
            TextureView(ctx).apply {
              surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
                  try {
                    mediaPlayer.setSurface(android.view.Surface(surface))
                  } catch (e: Exception) {
                    e.printStackTrace()
                  }
                }
                override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {}
                override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean = true
                override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {}
              }
            }
          }
        )
      }

      // Play/Pause Center Indicator
      AnimatedVisibility(
        visible = !isPlaying,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.Center)
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .shadow(8.dp, CircleShape)
            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play Video",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
          )
        }
      }

      // HUD Top Overlays: Live Scale Badge & Timecode
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.TopCenter)
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Dynamic Zoom Scale Badge
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isZoomedOut) AmberZoom else EmeraldCut,
          shadowElevation = 4.dp
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = if (isZoomedOut) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
              contentDescription = null,
              tint = Color.Black,
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = if (isZoomedOut) "${String.format("%.2f", currentScale)}x WIDE" else "1.00x NORMAL",
              color = Color.Black,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Timecode and Frame Index HUD
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color.Black.copy(alpha = 0.75f),
          border = androidx.compose.foundation.BorderStroke(0.5.dp, Slate700)
        ) {
          val totalSec = currentTimeMs / 1000.0
          val mins = (totalSec / 60).toInt()
          val secs = totalSec % 60
          Text(
            text = String.format("%02d:%06.3f | F:%d", mins, secs, currentFrame),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      // HUD Bottom Info Bar: Format & Calibration
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color.Black.copy(alpha = 0.7f)
        ) {
          Text(
            text = "9:16 VERTICAL",
            color = CyberCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color.Black.copy(alpha = 0.7f)
        ) {
          Text(
            text = "CALIBRATED CUBIC EASE",
            color = Slate700.copy(alpha = 0.9f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          )
        }
      }
    }
  }
}
