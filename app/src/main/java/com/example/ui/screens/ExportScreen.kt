package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.EmeraldCut
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlowBadge
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850

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

    // Export In Progress Card (AI EDITING VIDEO...)
    if (autoEditState.isProcessing) {
      CinematicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderBrush = Brush.linearGradient(
          listOf(GoldHighlight, AmberGold)
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
            Text("AI EDITING VIDEO...", color = GoldHighlight, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Text("${(autoEditState.progress * 100).toInt()}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          }

          LinearProgressIndicator(
            progress = { autoEditState.progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = GoldHighlight,
            trackColor = Slate800
          )

          Text(autoEditState.statusMessage, color = Slate400, fontSize = 11.sp)
        }
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

          if (exportResult.success && exportResult.outputPath != null) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = EmeraldGlow)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.horizontalGradient(listOf(EmeraldGreen, EmeraldGlow)))
                .clickable {
                  try {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                      type = "video/mp4"
                      putExtra(Intent.EXTRA_STREAM, Uri.parse(exportResult.outputPath))
                      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                  } catch (e: Exception) {
                    e.printStackTrace()
                  }
                }
                .padding(vertical = 12.dp),
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
