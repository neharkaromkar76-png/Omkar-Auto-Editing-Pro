package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.AmberZoom
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSlate950
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldCut
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.SurfaceCard

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
      .background(DeepSlate950)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Surface(shape = CircleShape, color = EmeraldCut, modifier = Modifier.size(32.dp)) {
        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(6.dp))
      }
      Column {
        Text("FRAME-ACCURATE MP4 EXPORT", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text("Hardware H.264 Encoder + Pristine AAC Master Audio", color = Slate400, fontSize = 11.sp)
      }
    }

    // Resolution Choice
    Card(
      colors = CardDefaults.cardColors(containerColor = SurfaceCard),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text("OUTPUT RESOLUTION", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("Original", "1080x1920", "720x1280").forEach { res ->
            val isSelected = settings.resolution == res
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) ElectricIndigo else Slate850)
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

    // Codec & Quality Specs
    Card(
      colors = CardDefaults.cardColors(containerColor = SurfaceCard),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text("EXPORT PROFILE SPECIFICATIONS", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          SpecItem("Container", settings.format)
          SpecItem("Video Codec", settings.videoCodec)
          SpecItem("Audio Codec", settings.audioCodec)
          SpecItem("Quality", settings.quality)
        }

        Text(
          text = "• Audio Preservation: Master audio timeline copied with 0 drift.\n• Horizontal Adaption: Center foreground scaled with blurred background.\n• Deterministic Easing: Cubic ease-out applied per frame.",
          color = Slate500,
          fontSize = 11.sp,
          lineHeight = 16.sp
        )
      }
    }

    // Export In Progress Card
    if (autoEditState.isProcessing) {
      Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AmberZoom)
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("RENDERING MP4 IN PROGRESS...", color = AmberZoom, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("${(autoEditState.progress * 100).toInt()}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }

          LinearProgressIndicator(
            progress = { autoEditState.progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = AmberZoom,
            trackColor = Slate700
          )

          Text(autoEditState.statusMessage, color = Slate400, fontSize = 12.sp)
        }
      }
    }

    // Export Finished Result & Validation Report
    if (exportResult != null) {
      Card(
        colors = CardDefaults.cardColors(
          containerColor = if (exportResult.success) SurfaceCard else Color.Red.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (exportResult.success) EmeraldCut else Color.Red
        )
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = if (exportResult.success) Icons.Default.Verified else Icons.Default.Error,
              contentDescription = null,
              tint = if (exportResult.success) EmeraldCut else Color.Red,
              modifier = Modifier.size(24.dp)
            )
            Text(
              text = if (exportResult.success) "EXPORT PASSED VALIDATION" else "EXPORT FAILED",
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }

          if (exportResult.validationReport != null) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = DeepSlate950,
              border = androidx.compose.foundation.BorderStroke(0.5.dp, Slate800)
            ) {
              Text(
                text = exportResult.validationReport,
                color = CyberCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          if (exportResult.success && exportResult.outputPath != null) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = {
                  try {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                      type = "video/mp4"
                      putExtra(Intent.EXTRA_STREAM, Uri.parse(exportResult.outputUri))
                      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Exported Video"))
                  } catch (e: Exception) {
                    e.printStackTrace()
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldCut, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("share_video_button")
              ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share Video", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // Primary Action: Render and Export MP4
    Button(
      onClick = onStartExport,
      enabled = !autoEditState.isProcessing && timeline != null,
      colors = ButtonDefaults.buttonColors(
        containerColor = EmeraldCut,
        contentColor = Color.Black
      ),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .testTag("render_export_button")
    ) {
      Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(Modifier.width(8.dp))
      Text(
        text = "RENDER & EXPORT FINAL MP4",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
      )
    }
  }
}

@Composable
private fun SpecItem(label: String, value: String) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Slate850)
      .padding(horizontal = 8.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(label, color = Slate500, fontSize = 9.sp)
    Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
  }
}
