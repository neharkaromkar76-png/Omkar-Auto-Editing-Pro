package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeechBoundary
import com.example.model.ZoomEvent
import com.example.ui.theme.AmberZoom
import com.example.ui.theme.CyberCyan
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
    Card(
      modifier = modifier
        .fillMaxWidth()
        .padding(12.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceCard),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.ZoomIn,
          contentDescription = null,
          tint = Slate500,
          modifier = Modifier.size(36.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
          text = "No Zoom Event Selected",
          color = Slate400,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold
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
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header Info Card
    Card(
      colors = CardDefaults.cardColors(containerColor = SurfaceCard),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberZoom, modifier = Modifier.size(18.dp))
            Text("ZOOM SPLIT INSPECTOR", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (selectedZoomEvent.confidence >= 0.85) EmeraldCut else AmberZoom
          ) {
            Text(
              text = "${(selectedZoomEvent.confidence * 100).toInt()}% CONFIDENCE",
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
          Column {
            Text("Boundary Time", color = Slate400, fontSize = 11.sp)
            Text(
              text = "${String.format("%.3f", selectedZoomEvent.boundaryTime)}s",
              color = CyberCyan,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Column {
            Text("Boundary Frame", color = Slate400, fontSize = 11.sp)
            Text(
              text = "Frame ${selectedZoomEvent.boundaryFrame}",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Column {
            Text("Curve Model", color = Slate400, fontSize = 11.sp)
            Text(
              text = "Cubic Ease-Out",
              color = ElectricViolet,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        if (associatedBoundary != null) {
          Text(
            text = "Trigger: ${associatedBoundary.reason}",
            color = Slate400,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }
    }

    // Wide Scale Slider
    Card(
      colors = CardDefaults.cardColors(containerColor = SurfaceCard),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Wide Frame Scale", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          Text(
            text = "${String.format("%.2f", selectedZoomEvent.wideScale)}x (Ref: 0.70x)",
            color = AmberZoom,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        Slider(
          value = selectedZoomEvent.wideScale.toFloat(),
          onValueChange = { onUpdateScale(it.toDouble()) },
          valueRange = 0.50f..0.90f,
          colors = SliderDefaults.colors(
            thumbColor = AmberZoom,
            activeTrackColor = AmberZoom,
            inactiveTrackColor = Slate700
          ),
          modifier = Modifier.testTag("wide_scale_slider")
        )
      }
    }

    // Zoom Recovery Duration Slider
    Card(
      colors = CardDefaults.cardColors(containerColor = SurfaceCard),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Zoom-In Duration", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          Text(
            text = "${selectedZoomEvent.durationFrames} Frames (~${(selectedZoomEvent.durationFrames * 41.6).toInt()}ms)",
            color = CyberCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        Slider(
          value = selectedZoomEvent.durationFrames.toFloat(),
          onValueChange = { onUpdateDuration(it.toInt()) },
          valueRange = 6f..24f,
          steps = 8,
          colors = SliderDefaults.colors(
            thumbColor = CyberCyan,
            activeTrackColor = CyberCyan,
            inactiveTrackColor = Slate700
          ),
          modifier = Modifier.testTag("zoom_duration_slider")
        )
      }
    }

    // Linguistic Score Breakdown
    if (associatedBoundary != null) {
      val scores = associatedBoundary.scoreDetails
      Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text("AI BOUNDARY METRICS", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)

          MetricBar(label = "Semantic Completion", score = scores.semanticCompletionScore, color = EmeraldCut)
          MetricBar(label = "Punctuation & Syntax", score = scores.punctuationScore, color = CyberCyan)
          MetricBar(label = "Acoustic Pause Gap", score = scores.pauseScore, color = AmberZoom)
          MetricBar(label = "Speaking Rhythm Cadence", score = scores.rhythmScore, color = ElectricIndigo)
        }
      }
    }

    // Delete Zoom Event Action
    Button(
      onClick = onDeleteEvent,
      colors = ButtonDefaults.buttonColors(
        containerColor = Color.Red.copy(alpha = 0.15f),
        contentColor = Color.Red
      ),
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color.Red.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        .testTag("inspector_delete_button")
    ) {
      Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
      Spacer(Modifier.width(8.dp))
      Text("Remove Split Marker", fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
private fun MetricBar(label: String, score: Double, color: Color) {
  Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(label, color = Color.White, fontSize = 11.sp)
      Text("${(score * 100).toInt()}%", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
    LinearProgressIndicator(
      progress = { score.toFloat() },
      modifier = Modifier
        .fillMaxWidth()
        .height(5.dp)
        .clip(RoundedCornerShape(3.dp)),
      color = color,
      trackColor = Slate800
    )
  }
}
