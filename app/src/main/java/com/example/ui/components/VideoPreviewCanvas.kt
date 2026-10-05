package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.view.TextureView
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
import com.example.model.MediaMetadata
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
  var isFullscreenMode by remember { mutableStateOf(false) }

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
          modifier = Modifier
            .fillMaxSize()
            .testTag("video_player_surface"),
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

      // HUD Top Overlays: Live Scale Badge, Timecode & Fullscreen button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.TopCenter)
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Dynamic Zoom Scale Badge (e.g. 0.70x WIDE / 1.00x NORMAL)
        Box(
          modifier = Modifier
            .shadow(6.dp, RoundedCornerShape(10.dp), spotColor = if (isZoomedOut) AmberGold else EmeraldGreen)
            .clip(RoundedCornerShape(10.dp))
            .background(
              if (isZoomedOut) {
                Brush.horizontalGradient(listOf(AmberGold, GoldHighlight))
              } else {
                Brush.horizontalGradient(listOf(EmeraldGreen, EmeraldGlow))
              }
            )
            .border(0.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(
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
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Timecode & Frame Index Counter + Fullscreen Toggle
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(DarkVoid.copy(alpha = 0.85f))
              .border(0.5.dp, Slate700, RoundedCornerShape(10.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            val totalSec = currentTimeMs / 1000.0
            val mins = (totalSec / 60).toInt()
            val secs = totalSec % 60
            Text(
              text = String.format("%02d:%06.3f | F:%d", mins, secs, currentFrame),
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(DarkVoid.copy(alpha = 0.85f))
              .border(0.5.dp, Slate700, CircleShape)
              .clickable { isFullscreenMode = !isFullscreenMode },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isFullscreenMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
              contentDescription = "Fullscreen",
              tint = CyanHighlight,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // HUD Bottom Info Bar: Format & Calibration
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkVoid.copy(alpha = 0.75f))
            .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
          Text(
            text = "9:16 VERTICAL",
            color = CyanHighlight,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkVoid.copy(alpha = 0.75f))
            .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
          Text(
            text = "SPEECH ZOOM CADENCE",
            color = Slate400,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
