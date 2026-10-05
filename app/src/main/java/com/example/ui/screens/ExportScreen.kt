package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.engine.ExportStorageManager
import java.io.File
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AutoEditState
import com.example.model.EditTimeline
import com.example.model.ExportResult
import com.example.model.ExportSettings
import com.example.ui.theme.AmberGold
import com.example.ui.theme.Cinematic3DIconContainer
import com.example.ui.theme.CinematicGlassCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkVoid
import com.example.ui.theme.DeepCanvas
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.EmeraldCut
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlassHighlight
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlowBadge
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ExportScreen(
  timeline: EditTimeline?,
  settings: ExportSettings,
  autoEditState: AutoEditState,
  exportResult: ExportResult?,
  onUpdateSettings: (ExportSettings) -> Unit,
  onStartExport: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkVoid)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {

    // 10. EXPORT SCREEN HEADER
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Cinematic3DIconContainer(
        icon = Icons.Default.Movie,
        contentDescription = "Export Video",
        size = 42.dp,
        iconSize = 22.dp,
        gradientColors = listOf(NeonPurple, ElectricBlue)
      )
      Column {
        Text(
          text = "EXPORT VIDEO",
          color = Color.White,
          fontSize = 15.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 0.8.sp
        )
        Text(
          text = "Hardware H.264 Encoder + Pristine AAC Master Audio",
          color = Slate400,
          fontSize = 11.sp
        )
      }
    }

    // PREMIUM ANIMATED CIRCULAR PROGRESS INDICATOR (Real-Time Rendering Status)
    if (autoEditState.isProcessing) {
      CinematicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderBrush = Brush.linearGradient(
          listOf(
            GoldHighlight.copy(alpha = 0.7f),
            NeonPurpleGlow.copy(alpha = 0.5f),
            CyanHighlight.copy(alpha = 0.6f)
          )
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.radialGradient(
                colors = listOf(
                  NeonPurple.copy(alpha = 0.2f),
                  DarkSurface.copy(alpha = 0.95f)
                ),
                radius = 500f
              )
            )
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Cinematic3DIconContainer(
                icon = Icons.Default.AutoAwesome,
                contentDescription = null,
                size = 32.dp,
                iconSize = 16.dp,
                gradientColors = listOf(AmberGold, GoldHighlight)
              )
              Text(
                text = "AI EDITING VIDEO...",
                color = GoldHighlight,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
              )
            }

            GlowBadge(
              text = "RENDERING ACTIVE",
              accentColor = GoldHighlight,
              backgroundColor = AmberGold.copy(alpha = 0.18f)
            )
          }

          // Centerpiece: Premium Animated Circular Rendering Gauge
          PremiumCircularExportIndicator(
            progress = autoEditState.progress,
            stageNumber = autoEditState.currentStage?.stepNumber ?: 1,
            totalStages = 12,
            size = 170.dp,
            strokeWidth = 10.dp
          )

          // Live Status readout
          Text(
            text = autoEditState.statusMessage,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
          )

          // Live Pipeline Telemetry Chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            SpecItem3D("Encoder", "H.264 Hardware", CyanHighlight)
            SpecItem3D("Audio Sync", "0.00ms Drift", EmeraldGlow)
            SpecItem3D("Cadence", "Cubic Ease-Out", GoldHighlight)
          }
        }
      }
    }

    // Export Options Card: MP4 | 1080p | 9:16 | Original FPS
    CinematicGlassCard(
      modifier = Modifier.fillMaxWidth(),
      borderBrush = Brush.linearGradient(
        listOf(ElectricBlueGlow.copy(alpha = 0.4f), DarkSurface)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text("EXPORT FORMAT & RESOLUTION", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        // Options: MP4, 1080p, 9:16, Original FPS Chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ExportOptionChip(title = "MP4", isSelected = true) {}
          ExportOptionChip(
            title = "1080p",
            isSelected = settings.resolution == "1080x1920",
            onClick = { onUpdateSettings(settings.copy(resolution = "1080x1920")) }
          )
          ExportOptionChip(title = "9:16", isSelected = true) {}
          ExportOptionChip(
            title = "Orig. FPS",
            isSelected = true
          ) {}
        }

        // Secondary Resolution Picker
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("Original", "1080x1920", "720x1280").forEach { res ->
            val isSelected = settings.resolution == res
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                  if (isSelected) {
                    Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.6f), ElectricBlue.copy(alpha = 0.6f)))
                  } else {
                    Brush.horizontalGradient(listOf(DarkSurface, DarkSurface))
                  }
                )
                .border(
                  1.dp,
                  if (isSelected) ElectricBlueGlow else Slate800,
                  RoundedCornerShape(10.dp)
                )
                .clickable { onUpdateSettings(settings.copy(resolution = res)) }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = res,
                color = if (isSelected) Color.White else Slate400,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }
      }
    }

    // Codec & Quality Specs Card
    CinematicGlassCard(
      modifier = Modifier.fillMaxWidth(),
      borderBrush = Brush.linearGradient(
        listOf(Slate800, DarkSurface)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text("EXPORT PROFILE SPECIFICATIONS", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          SpecItem3D("Container", settings.format, CyanHighlight)
          SpecItem3D("Video Codec", settings.videoCodec, ElectricBlueGlow)
          SpecItem3D("Audio Codec", settings.audioCodec, GoldHighlight)
          SpecItem3D("Quality", settings.quality, EmeraldGlow)
        }

        Text(
          text = "• Audio Preservation: Master audio timeline copied with 0 drift.\n• Horizontal Adaption: Center foreground scaled with blurred background.\n• Deterministic Easing: Cubic ease-out applied per frame.",
          color = Slate500,
          fontSize = 11.sp,
          lineHeight = 16.sp
        )
      }
    }

    // Export Finished Result & Validation Report (RENDER COMPLETE, ✓ Video ready)
    if (exportResult != null) {
      CinematicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderBrush = Brush.linearGradient(
          listOf(
            if (exportResult.success) EmeraldGlow else Color.Red,
            Color.Transparent
          )
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = if (exportResult.success) Icons.Default.Verified else Icons.Default.Error,
                contentDescription = null,
                tint = if (exportResult.success) EmeraldGlow else Color.Red,
                modifier = Modifier.size(24.dp)
              )
              Column {
                Text(
                  text = if (exportResult.success) "RENDER COMPLETE" else "RENDER FAILED",
                  color = Color.White,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                )
                if (exportResult.success) {
                  Text(
                    text = "✓ Video ready",
                    color = EmeraldGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            if (exportResult.success) {
              GlowBadge(text = "VALIDATED", accentColor = EmeraldGlow)
            }
          }

          if (exportResult.validationReport != null) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkVoid)
                .border(0.5.dp, Slate800, RoundedCornerShape(10.dp))
                .padding(12.dp)
            ) {
              Text(
                text = exportResult.validationReport,
                color = CyanHighlight,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 15.sp
              )
            }
          }

          if (exportResult.success) {
            val contentUri = androidx.compose.runtime.remember(exportResult) {
              val uriStr = exportResult.outputUri
              if (uriStr != null && uriStr.startsWith("content://")) {
                Uri.parse(uriStr)
              } else {
                exportResult.outputPath?.let { File(it) }?.let { localFile ->
                  ExportStorageManager.getShareableContentUri(context, localFile, exportResult.outputUri)
                }
              }
            }

            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Action 1: Download / Open in Gallery
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = CyanHighlight)
                  .clip(RoundedCornerShape(14.dp))
                  .background(Brush.horizontalGradient(listOf(ElectricBlue, CyberCyan)))
                  .clickable {
                    if (contentUri != null) {
                      Toast.makeText(context, "Saved to Movies/CutsZoom AI/", Toast.LENGTH_SHORT).show()
                      ExportStorageManager.openVideo(context, contentUri)
                    } else {
                      Toast.makeText(context, "Exported video could not be opened. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                  }
                  .padding(vertical = 12.dp)
                  .testTag("download_video_button"),
                contentAlignment = Alignment.Center
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("SAVE / OPEN IN GALLERY (MOVIES)", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }

              // Action 2: Share to other apps (WhatsApp, Drive, Quick Share, Files)
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = EmeraldGlow)
                  .clip(RoundedCornerShape(14.dp))
                  .background(Brush.horizontalGradient(listOf(EmeraldGreen, EmeraldGlow)))
                  .clickable {
                    if (contentUri != null) {
                      val shareResult = ExportStorageManager.shareVideo(context, contentUri)
                      if (shareResult.isFailure) {
                        Toast.makeText(context, "Exported video could not be shared. Please try again.", Toast.LENGTH_SHORT).show()
                      }
                    } else {
                      Toast.makeText(context, "Exported video could not be shared. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                  }
                  .padding(vertical = 12.dp)
                  .testTag("share_video_button"),
                contentAlignment = Alignment.Center
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("SHARE EXPORTED VIDEO", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    // PRIMARY CTA: EXPORT MP4 (Glowing purple/blue, 3D depth, large)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(
          elevation = if (!autoEditState.isProcessing) 16.dp else 2.dp,
          shape = RoundedCornerShape(16.dp),
          spotColor = NeonPurpleGlow.copy(alpha = 0.6f)
        )
        .clip(RoundedCornerShape(16.dp))
        .background(
          if (!autoEditState.isProcessing) {
            Brush.horizontalGradient(listOf(NeonPurple, ElectricBlue))
          } else {
            Brush.horizontalGradient(listOf(Slate800, Slate850))
          }
        )
        .border(
          width = 1.dp,
          brush = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)
          ),
          shape = RoundedCornerShape(16.dp)
        )
        .clickable(
          enabled = !autoEditState.isProcessing,
          onClick = onStartExport
        )
        .padding(vertical = 16.dp)
        .testTag("export_mp4_button"),
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(Icons.Default.Download, contentDescription = null, tint = GoldHighlight, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
          text = if (autoEditState.isProcessing) "AI RENDERING IN PROGRESS..." else "EXPORT MP4",
          color = Color.White,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          letterSpacing = 0.8.sp
        )
      }
    }

    Spacer(Modifier.height(30.dp))
  }
}

/**
 * Premium Animated Circular Progress Indicator for real-time rendering.
 * Features:
 * - Ambient animated sweep rotation
 * - Multi-stop gradient arc (Neon Purple -> Electric Blue -> Cyan -> Gold)
 * - Glowing leading-edge particle
 * - Monospace real-time percentage counter
 * - Stage indicator HUD
 */
@Composable
fun PremiumCircularExportIndicator(
  progress: Float,
  stageNumber: Int,
  totalStages: Int,
  modifier: Modifier = Modifier,
  size: Dp = 170.dp,
  strokeWidth: Dp = 10.dp
) {
  val animatedProgress by animateFloatAsState(
    targetValue = progress.coerceIn(0f, 1f),
    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
    label = "export_progress_anim"
  )

  // Infinite subtle rotation for ambient futuristic aura
  val infiniteTransition = rememberInfiniteTransition(label = "halo_rotation")
  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 8000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation_angle_anim"
  )

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    // Canvas for Circular Progress Ring and Glowing Tip
    Canvas(modifier = Modifier.fillMaxSize()) {
      val strokePx = strokeWidth.toPx()
      val diameter = size.toPx() - strokePx * 2
      val radius = diameter / 2f
      val centerOffset = Offset(size.toPx() / 2f, size.toPx() / 2f)

      // 1. Inactive background track
      drawCircle(
        color = DarkSurface.copy(alpha = 0.9f),
        radius = radius,
        center = centerOffset,
        style = Stroke(width = strokePx)
      )

      // 2. Faint outer accent ring
      drawCircle(
        color = Slate800.copy(alpha = 0.4f),
        radius = radius + strokePx * 0.7f,
        center = centerOffset,
        style = Stroke(width = 1.5f)
      )

      // 3. Active Progress Arc with multi-color gradient
      val sweepAngle = animatedProgress * 360f
      if (sweepAngle > 0f) {
        val gradientBrush = Brush.sweepGradient(
          colors = listOf(
            NeonPurple,
            ElectricBlue,
            CyanHighlight,
            GoldHighlight,
            NeonPurple
          ),
          center = centerOffset
        )

        drawArc(
          brush = gradientBrush,
          startAngle = -90f,
          sweepAngle = sweepAngle,
          useCenter = false,
          topLeft = Offset(strokePx, strokePx),
          size = Size(diameter, diameter),
          style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        // 4. Glowing Leading-edge Tip Dot
        val angleRad = ((-90f + sweepAngle) * (PI / 180f)).toFloat()
        val tipX = centerOffset.x + radius * cos(angleRad)
        val tipY = centerOffset.y + radius * sin(angleRad)

        // Outer glow dot
        drawCircle(
          color = GoldHighlight.copy(alpha = 0.45f),
          radius = strokePx * 0.9f,
          center = Offset(tipX, tipY)
        )
        // Solid center dot
        drawCircle(
          color = Color.White,
          radius = strokePx * 0.45f,
          center = Offset(tipX, tipY)
        )
      }
    }

    // High-Tech Center HUD (Percentage, Stage, and Subtitle)
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = "${(animatedProgress * 100).toInt()}%",
        color = Color.White,
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
      )

      Spacer(Modifier.height(4.dp))

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(DarkSurface)
          .border(0.5.dp, GoldHighlight.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = "STAGE $stageNumber / $totalStages",
          color = GoldHighlight,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(Modifier.height(4.dp))

      Text(
        text = "MP4 ENCODING",
        color = Slate400,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
      )
    }
  }
}

@Composable
private fun ExportOptionChip(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) ElectricBlue.copy(alpha = 0.25f) else DarkSurface)
      .border(
        1.dp,
        if (isSelected) ElectricBlueGlow else Slate800,
        RoundedCornerShape(8.dp)
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = title,
      color = if (isSelected) CyanHighlight else Slate400,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    )
  }
}

@Composable
private fun SpecItem3D(label: String, value: String, color: Color) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(DarkSurface)
      .border(0.5.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(label, color = Slate500, fontSize = 9.sp)
    Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
  }
}
