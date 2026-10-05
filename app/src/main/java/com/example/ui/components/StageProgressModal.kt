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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AutoEditState
import com.example.model.ProcessingStage
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
fun StageProgressModal(
  state: AutoEditState,
  onDismiss: () -> Unit
) {
  if (!state.isProcessing && state.currentStage != ProcessingStage.RENDERING_EXPORT) return

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp)),
      colors = CardDefaults.cardColors(containerColor = DeepSlate950),
      border = androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.5f))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
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
            Surface(shape = CircleShape, color = AmberZoom, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(5.dp))
            }
            Text("AI VIDEO EDITOR PIPELINE", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }

          Text(
            text = "${(state.progress * 100).toInt()}%",
            color = CyberCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
        }

        LinearProgressIndicator(
          progress = { state.progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
          color = ElectricIndigo,
          trackColor = Slate800
        )

        Text(
          text = state.statusMessage,
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )

        // Stage Pills
        val currentStep = state.currentStage?.stepNumber ?: 1
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          ProcessingStage.values().take(10).forEach { stage ->
            val isDone = currentStep > stage.stepNumber
            val isCurrent = currentStep == stage.stepNumber

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(if (isCurrent) Slate850 else Color.Transparent)
                .padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (isDone) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldCut, modifier = Modifier.size(14.dp))
              } else if (isCurrent) {
                CircularProgressIndicator(
                  modifier = Modifier.size(12.dp),
                  color = AmberZoom,
                  strokeWidth = 2.dp
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(12.dp)
                    .border(1.dp, Slate700, CircleShape)
                )
              }

              Text(
                text = "${stage.stepNumber}. ${stage.title}",
                color = if (isCurrent) Color.White else if (isDone) Slate400 else Slate700,
                fontSize = 11.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }
      }
    }
  }
}
