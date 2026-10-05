package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaMetadata
import com.example.model.ReferenceStyleProfile
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberZoom
import com.example.ui.theme.Cinematic3DIconContainer
import com.example.ui.theme.CinematicGlassCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkVoid
import com.example.ui.theme.DeepCanvas
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.ElectricIndigo
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
  onNavigateToAiScoring: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val pickMediaLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      onPickUserVideo(uri)
    }
  }

  var apiKeyInput by remember { mutableStateOf(customApiKey) }

  // Subtle animated light sweep on hero card
  val infiniteTransition = rememberInfiniteTransition(label = "hero_light_sweep")
  val sweepOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "sweep_anim"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkVoid)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {

    // 2. AI VIDEO ENGINE HERO CARD (3D Glass, Gradient Border, Light Sweep, AI Badge)
    CinematicGlassCard(
      modifier = Modifier.fillMaxWidth(),
      borderBrush = Brush.linearGradient(
        listOf(
          ElectricBlueGlow.copy(alpha = 0.5f),
          NeonPurpleGlow.copy(alpha = 0.4f),
          ElectricBlue.copy(alpha = 0.2f)
        )
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.radialGradient(
              colors = listOf(
                NeonPurple.copy(alpha = 0.15f),
                DarkSurface.copy(alpha = 0.95f)
              ),
              center = androidx.compose.ui.geometry.Offset(100f, 80f),
              radius = 600f
            )
          )
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Cinematic3DIconContainer(
              icon = Icons.Default.Videocam,
              contentDescription = "Video Engine",
              size = 42.dp,
              iconSize = 22.dp,
              gradientColors = listOf(ElectricBlue, NeonPurple)
            )

            Column {
              Text(
                text = "CUTSZOOM AI VIDEO ENGINE",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
              )
              Text(
                text = "Calibrated Vertical Reference Engine",
                color = Slate400,
                fontSize = 11.sp
              )
            }
          }

          GlowBadge(
            text = "⚡ AI POWERED",
            accentColor = CyanHighlight,
            backgroundColor = CyberCyan.copy(alpha = 0.18f)
          )
        }

        Text(
          text = "Speech-boundary driven video editor calibrated from professional vertical reference style.\nReal frame-accurate keyframes, wide cuts, and MP4 rendering.",
          color = Slate200,
          fontSize = 12.sp,
          lineHeight = 18.sp,
          fontWeight = FontWeight.Normal
        )
      }
    }

    // 3. MAIN ACTION CARDS (Side-by-side on mobile: Import Video + Calibrated Demo)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // CARD 1: Import Video (Blue/Cyan Theme, 3D Icon, Arrow Button, Press Micro-interaction)
      ActionCard3D(
        title = "Import Video",
        subtitle = "Device Gallery",
        icon = Icons.Default.UploadFile,
        accentColor = CyanHighlight,
        gradientColors = listOf(ElectricBlue, CyberCyan),
        modifier = Modifier
          .weight(1f)
          .testTag("upload_video_button"),
        onClick = {
          pickMediaLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
          )
        }
      )

      // CARD 2: Calibrated Demo (Gold/Amber Theme, 3D Icon, Arrow Button, Press Micro-interaction)
      ActionCard3D(
        title = "Calibrated Demo",
        subtitle = "Vertical Speech Clip",
        icon = Icons.Default.PlayArrow,
        accentColor = GoldHighlight,
        gradientColors = listOf(AmberGold, GoldHighlight),
        modifier = Modifier
          .weight(1f)
          .testTag("sample_video_button"),
        onClick = onLoadSampleVideo
      )
    }

    // 4. INSPECTED MEDIA STREAM (Professional Media Inspection Panel)
    if (metadata != null) {
      CinematicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderBrush = Brush.linearGradient(
          listOf(CyberCyan.copy(alpha = 0.35f), Color.Transparent)
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
              Cinematic3DIconContainer(
                icon = Icons.Default.CameraAlt,
                contentDescription = null,
                size = 32.dp,
                iconSize = 16.dp,
                gradientColors = listOf(CyberCyan, ElectricBlue)
              )
              Text(
                text = "INSPECTED MEDIA STREAM",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              )
            }

            GlowBadge(
              text = if (metadata.isVertical) "✓ 9:16 VERTICAL" else "HORIZONTAL (AUTO-BLUR)",
              accentColor = if (metadata.isVertical) EmeraldGlow else AmberGold
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            MetricPill3D(
              label = "Duration",
              value = "${String.format("%.2f", metadata.durationSeconds)}s",
              icon = Icons.Default.AccessTime,
              color = CyanHighlight
            )
            MetricPill3D(
              label = "Resolution",
              value = "${metadata.width}×${metadata.height}",
              icon = Icons.Default.AspectRatio,
              color = ElectricBlueGlow
            )
            MetricPill3D(
              label = "FPS",
              value = String.format("%.2f", metadata.fps),
              icon = Icons.Default.Speed,
              color = EmeraldGlow
            )
            MetricPill3D(
              label = "Audio",
              value = if (metadata.hasAudio) "${metadata.audioChannels}ch Detected" else "None",
              icon = Icons.Default.Audiotrack,
              color = GoldHighlight
            )
          }
        }
      }
    }

    // 5. REFERENCE STYLE PROFILE (Calibrated Zoom Model & Neon Metric Chips)
    CinematicGlassCard(
      modifier = Modifier.fillMaxWidth(),
      borderBrush = Brush.linearGradient(
        listOf(AmberGold.copy(alpha = 0.35f), NeonPurple.copy(alpha = 0.25f))
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
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
            Cinematic3DIconContainer(
              icon = Icons.Default.Tune,
              contentDescription = null,
              size = 32.dp,
              iconSize = 16.dp,
              gradientColors = listOf(AmberGold, NeonPurple)
            )
            Text(
              text = "REFERENCE STYLE PROFILE",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            )
          }

          GlowBadge(
            text = "48.576s REF",
            accentColor = GoldHighlight,
            backgroundColor = AmberGold.copy(alpha = 0.15f)
          )
        }

        Text(
          text = "Calibrated Zoom Model: Normal = 1.00x → Speech Split → Wide = ${String.format("%.2f", styleProfile.wideScale)}x → 10-frame Cubic Ease-Out recovery to 1.00x.",
          color = Slate400,
          fontSize = 12.sp,
          lineHeight = 17.sp
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          MetricPill3D("Wide Scale", "${String.format("%.2f", styleProfile.wideScale)}x", null, CyanHighlight)
          MetricPill3D("Recovery", "10 Frames", null, NeonPurpleGlow)
          MetricPill3D("Easing", "Cubic Ease-Out", null, EmeraldGlow)
          MetricPill3D("Center", "50% / 50%", null, GoldHighlight)
        }
      }
    }

    // 6. GEMINI SEMANTIC ANALYSIS (Purple AI Icon, Status, Details Navigation Button)
    CinematicGlassCard(
      modifier = Modifier.fillMaxWidth(),
      borderBrush = Brush.linearGradient(
        listOf(NeonPurpleGlow.copy(alpha = 0.4f), ElectricBlue.copy(alpha = 0.2f))
      )
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
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Cinematic3DIconContainer(
              icon = Icons.Default.Psychology,
              contentDescription = null,
              size = 36.dp,
              iconSize = 18.dp,
              gradientColors = listOf(NeonPurple, NeonPurpleGlow)
            )
            Column {
              Text(
                text = "GEMINI SEMANTIC ANALYSIS",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              )
              Text(
                text = "AI-powered transcript analysis and smart boundary detection.",
                color = Slate400,
                fontSize = 10.sp
              )
            }
          }

          IconButton(
            onClick = onNavigateToAiScoring,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "View Scoring",
              tint = NeonPurpleGlow,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Optional API Key configuration input
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            placeholder = { Text("Paste Gemini API Key (optional)", fontSize = 11.sp, color = Slate500) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonPurple,
              unfocusedBorderColor = Slate700,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("api_key_input")
          )
          Button(
            onClick = { onSetCustomApiKey(apiKeyInput) },
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = CyberCyan),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
          ) {
            Text("Set", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 7. PRIMARY CTA: [ AUTO EDIT (DETECT SPEECH & ZOOM CUTS) ]
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(
          elevation = if (metadata != null) 16.dp else 2.dp,
          shape = RoundedCornerShape(16.dp),
          spotColor = NeonPurpleGlow.copy(alpha = 0.6f)
        )
        .clip(RoundedCornerShape(16.dp))
        .background(
          if (metadata != null) {
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
          enabled = metadata != null,
          onClick = onRunAutoEdit
        )
        .padding(vertical = 16.dp)
        .testTag("auto_edit_primary_button"),
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = GoldHighlight,
          modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
          text = "AUTO EDIT (DETECT SPEECH & ZOOM CUTS)",
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
 * 3D Action Card with gradient reflection, elevated surface, and press micro-interaction.
 */
@Composable
private fun ActionCard3D(
  title: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  gradientColors: List<Color>,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by androidx.compose.animation.core.animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1.0f,
    label = "card_scale_anim"
  )

  Box(
    modifier = modifier
      .scale(scale)
      .shadow(elevation = 10.dp, shape = RoundedCornerShape(18.dp), spotColor = gradientColors.first().copy(alpha = 0.4f))
      .clip(RoundedCornerShape(18.dp))
      .background(GlassSurface)
      .border(
        width = 1.dp,
        brush = Brush.linearGradient(
          listOf(gradientColors.first().copy(alpha = 0.6f), Color.Transparent)
        ),
        shape = RoundedCornerShape(18.dp)
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      )
      .padding(14.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Cinematic3DIconContainer(
        icon = icon,
        contentDescription = title,
        size = 46.dp,
        iconSize = 24.dp,
        gradientColors = gradientColors
      )

      Text(
        text = title,
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = subtitle,
        color = Slate400,
        fontSize = 11.sp
      )

      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(DarkSurface),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(13.dp)
        )
      }
    }
  }
}

/**
 * 3D Metric Pill with subtle glowing accent.
 */
@Composable
private fun MetricPill3D(
  label: String,
  value: String,
  icon: ImageVector? = null,
  color: Color = Color.White
) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(DarkSurface)
      .border(0.5.dp, color.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
      .padding(horizontal = 8.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(2.dp)
  ) {
    Text(label, color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      if (icon != null) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(11.dp))
      }
      Text(
        text = value,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
