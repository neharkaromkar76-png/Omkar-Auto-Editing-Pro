package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ReferenceCalibrator
import com.example.model.MediaMetadata
import com.example.model.ReferenceStyleProfile
import com.example.ui.theme.AmberZoom
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSlate950
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldCut
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.SurfaceCard

@Composable
fun ProjectScreen(
  metadata: MediaMetadata?,
  styleProfile: ReferenceStyleProfile,
  customApiKey: String,
  onPickUserVideo: (Uri) -> Unit,
  onLoadSampleVideo: () -> Unit,
  onRunAutoEdit: () -> Unit,
  onSetCustomApiKey: (String) -> Unit,
  onUpdateStyleProfile: (ReferenceStyleProfile) -> Unit,
  modifier: Modifier = Modifier
) {
  val pickMediaLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      onPickUserVideo(uri)
    }
  }

  var showCalibrationModal by remember { mutableStateOf(false) }
  var apiKeyInput by remember { mutableStateOf(customApiKey) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DeepSlate950)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Hero Banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .background(
          Brush.linearGradient(
            listOf(Slate850, ElectricIndigo.copy(alpha = 0.35f), DeepSlate950)
          )
        )
        .border(1.dp, ElectricIndigo.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
        .padding(18.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = AmberZoom,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color.Black,
              modifier = Modifier.padding(5.dp)
            )
          }
          Text(
            text = "CUTSZOOM AI VIDEO ENGINE",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
        }
        Text(
          text = "Speech-boundary driven video editor calibrated from professional vertical reference style. Real frame-accurate keyframes, wide cuts, and MP4 rendering.",
          color = Slate400,
          fontSize = 12.sp,
          lineHeight = 17.sp
        )
      }
    }

    // Media Source Selection: Upload or Use Calibrated Demo
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Pick User Video (Photo Picker)
      Card(
        modifier = Modifier
          .weight(1f)
          .clickable {
            pickMediaLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
          }
          .testTag("upload_video_button"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.5f))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = ElectricIndigo.copy(alpha = 0.2f),
            modifier = Modifier.size(42.dp)
          ) {
            Icon(
              Icons.Default.UploadFile,
              contentDescription = null,
              tint = ElectricIndigo,
              modifier = Modifier.padding(10.dp)
            )
          }
          Text("Import Video", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          Text("Device Gallery", color = Slate500, fontSize = 11.sp)
        }
      }

      // Load Calibrated Demo Video
      Card(
        modifier = Modifier
          .weight(1f)
          .clickable { onLoadSampleVideo() }
          .testTag("sample_video_button"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AmberZoom.copy(alpha = 0.4f))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = AmberZoom.copy(alpha = 0.2f),
            modifier = Modifier.size(42.dp)
          ) {
            Icon(
              Icons.Default.PlayCircleOutline,
              contentDescription = null,
              tint = AmberZoom,
              modifier = Modifier.padding(10.dp)
            )
          }
          Text("Calibrated Demo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          Text("Vertical Speech Clip", color = Slate500, fontSize = 11.sp)
        }
      }
    }

    // Current Media Inspection Card
    if (metadata != null) {
      Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.VideoFile, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
              Text("INSPECTED MEDIA STREAM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (metadata.isVertical) EmeraldCut else AmberZoom
            ) {
              Text(
                text = if (metadata.isVertical) "9:16 VERTICAL" else "HORIZONTAL (AUTO-BLUR)",
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            DetailPill("Duration", "${String.format("%.2f", metadata.durationSeconds)}s")
            DetailPill("Resolution", "${metadata.width}x${metadata.height}")
            DetailPill("FPS", String.format("%.2f", metadata.fps))
            DetailPill("Audio", if (metadata.hasAudio) "${metadata.audioChannels}ch @ ${metadata.audioSampleRate}Hz" else "None")
          }
        }
      }
    }

    // Reference Style Calibration Card
    Card(
      colors = CardDefaults.cardColors(containerColor = SurfaceCard),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = AmberZoom, modifier = Modifier.size(18.dp))
            Text("REFERENCE STYLE PROFILE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Slate850
          ) {
            Text(
              text = "48.576s REF",
              color = Slate400,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Text(
          text = "Calibrated Zoom Model: Normal = 1.00x → Speech Split → Wide = ${String.format("%.2f", styleProfile.wideScale)}x → 10-frame Cubic Ease-Out recovery to 1.00x.",
          color = Slate400,
          fontSize = 12.sp,
          lineHeight = 16.sp
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          DetailPill("Wide Scale", "${String.format("%.2f", styleProfile.wideScale)}x")
          DetailPill("Recovery", "10 Frames")
          DetailPill("Easing", "Cubic Ease-Out")
          DetailPill("Center", "50% / 50%")
        }
      }
    }

    // Gemini API Configuration (Optional override)
    Card(
      colors = CardDefaults.cardColors(containerColor = SurfaceCard),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Icon(Icons.Default.VpnKey, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(18.dp))
          Text("GEMINI SEMANTIC ANALYSIS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(
          text = "Gemini evaluates thought completeness and sentence structure. Built-in acoustic intelligence acts as offline engine if no key is supplied.",
          color = Slate400,
          fontSize = 11.sp
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            placeholder = { Text("Paste Gemini API Key (optional)", fontSize = 12.sp, color = Slate500) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = ElectricIndigo,
              unfocusedBorderColor = Slate700,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("api_key_input")
          )
          Button(
            onClick = { onSetCustomApiKey(apiKeyInput) },
            colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = CyberCyan),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Set", fontSize = 12.sp)
          }
        }
      }
    }

    // Primary Action: [ AUTO EDIT ]
    Button(
      onClick = onRunAutoEdit,
      enabled = metadata != null,
      colors = ButtonDefaults.buttonColors(
        containerColor = ElectricIndigo,
        contentColor = Color.White
      ),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .testTag("auto_edit_primary_button")
    ) {
      Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberZoom, modifier = Modifier.size(20.dp))
      Spacer(Modifier.width(8.dp))
      Text(
        text = "AUTO EDIT (DETECT SPEECH & ZOOM CUTS)",
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp
      )
    }
  }
}

@Composable
private fun DetailPill(label: String, value: String) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Slate850)
      .padding(horizontal = 8.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(label, color = Slate500, fontSize = 9.sp)
    Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
  }
}
