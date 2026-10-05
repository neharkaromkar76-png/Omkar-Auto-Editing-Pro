package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeechBoundary
import com.example.model.ZoomEvent
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberZoom
import com.example.ui.theme.Cinematic3DIconContainer
import com.example.ui.theme.CinematicGlassCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkVoid
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldCut
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldGreen
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
fun KeyframeInspector(
  selectedZoomEvent: ZoomEvent?,
  associatedBoundary: SpeechBoundary?,
  onUpdateScale: (Double) -> Unit,
  onUpdateDuration: (Int) -> Unit,
  onUpdatePosition: (Double, Double) -> Unit,
  onDeleteEvent: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (selectedZoomEvent == null) {
    CinematicGlassCard(
      modifier = modifier
        .fillMaxWidth()
        .padding(14.dp),
      borderBrush = Brush.linearGradient(listOf(Slate800, DarkSurface))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Cinematic3DIconContainer(
          icon = Icons.Default.Tune,
          contentDescription = null,
          size = 46.dp,
          iconSize = 22.dp,
          gradientColors = listOf(Slate700, Slate800)
        )
        Spacer(Modifier.height(12.dp))
        Text(
          text = "No Zoom Event Selected",
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Tap any split marker on the timeline or click 'Split at Playhead'",
          color = Slate500,
          fontSize = 12.sp,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }
    return
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header Info Card with Boundary Details
    CinematicGlassCard(
      modifier = Modifier.fillMaxWidth(),
      borderBrush = Brush.linearGradient(
        listOf(GoldHighlight.copy(alpha = 0.5f), DarkSurface)
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
              icon = Icons.Default.AutoAwesome,
              contentDescription = null,
              size = 34.dp,
              iconSize = 18.dp,
              gradientColors = listOf(AmberGold, GoldHighlight)
            )
            Text("ZOOM SPLIT INSPECTOR", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }

          GlowBadge(
            text = "FRAME ${selectedZoomEvent.boundaryFrame}",
            accentColor = GoldHighlight
          )
        }

        if (associatedBoundary != null) {
          Text(
            text = "Trigger: ${associatedBoundary.reason}",
            color = Slate200,
            fontSize = 12.sp
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            val s = associatedBoundary.scoreDetails
            InspectorMetricPill("Semantic", "${(s.semanticCompletionScore * 100).toInt()}%", EmeraldGlow)
            InspectorMetricPill("Punctuation", "${(s.punctuationScore * 100).toInt()}%", CyanHighlight)
            InspectorMetricPill("Pause", "${(s.pauseScore * 100).toInt()}%", GoldHighlight)
            InspectorMetricPill("Rhythm", "${(s.rhythmScore * 100).toInt()}%", ElectricBlueGlow)
          }
        }
      }
    }

    // Wide Scale Slider Card
    CinematicGlassCard(
      modifier = Modifier.fillMaxWidth(),
      borderBrush = Brush.linearGradient(listOf(CyanHighlight.copy(alpha = 0.35f), DarkSurface))
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
          Text("WIDE SCALE AT SPLIT MOMENT", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Text(
            text = "${String.format("%.2f", selectedZoomEvent.wideScale)}x",
            color = CyanHighlight,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        Slider(
          value = selectedZoomEvent.wideScale.toFloat(),
          onValueChange = { onUpdateScale(it.toDouble()) },
          valueRange = 0.50f..0.90f,
          steps = 8,
          colors = SliderDefaults.colors(
            thumbColor = CyanHighlight,
            activeTrackColor = CyanHighlight,
            inactiveTrackColor = Slate800
          ),
          modifier = Modifier.testTag("scale_slider")
        )

        Text(
          text = "Calibrated default: 0.70x. Smaller scale yields wider field of view.",
          color = Slate500,
          fontSize = 11.sp
        )
      }
    }

    // Recovery Duration Slider Card
    CinematicGlassCard(
      modifier = Modifier.fillMaxWidth(),
      borderBrush = Brush.linearGradient(listOf(NeonPurpleGlow.copy(alpha = 0.35f), DarkSurface))
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
          Text("RECOVERY DURATION (FRAMES)", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Text(
            text = "${selectedZoomEvent.durationFrames} Frames",
            color = NeonPurpleGlow,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        Slider(
          value = selectedZoomEvent.durationFrames.toFloat(),
          onValueChange = { onUpdateDuration(it.toInt()) },
          valueRange = 6f..24f,
          steps = 9,
          colors = SliderDefaults.colors(
            thumbColor = NeonPurpleGlow,
            activeTrackColor = NeonPurpleGlow,
            inactiveTrackColor = Slate800
          ),
          modifier = Modifier.testTag("duration_slider")
        )

        Text(
          text = "Calibrated default: 10 frames (~0.42s at 24fps) with Cubic Ease-Out recovery.",
          color = Slate500,
          fontSize = 11.sp
        )
      }
    }

    // Delete Split Button
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0xFF7F1D1D).copy(alpha = 0.4f))
        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
        .clickable(onClick = onDeleteEvent)
        .padding(vertical = 12.dp),
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("DELETE THIS ZOOM SPLIT", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun InspectorMetricPill(label: String, value: String, color: Color) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(DarkSurface)
      .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(label, color = Slate500, fontSize = 9.sp)
    Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
  }
}
