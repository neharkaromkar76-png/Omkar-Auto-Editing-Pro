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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AutoEditState
import com.example.model.ProcessingStage
import com.example.ui.theme.AmberGold
import com.example.ui.theme.Cinematic3DIconContainer
import com.example.ui.theme.CyanHighlight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkVoid
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850

@Composable
fun StageProgressModal(
  state: AutoEditState,
  onDismiss: () -> Unit
) {
  if (!state.isProcessing && state.currentStage != ProcessingStage.RENDERING_EXPORT) return

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = NeonPurpleGlow)
        .clip(RoundedCornerShape(24.dp))
        .background(DarkVoid)
        .border(
          width = 1.dp,
          brush = Brush.linearGradient(
            listOf(NeonPurpleGlow.copy(alpha = 0.6f), ElectricBlueGlow.copy(alpha = 0.4f))
          ),
          shape = RoundedCornerShape(24.dp)
        )
        .padding(20.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
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
              size = 36.dp,
              iconSize = 18.dp,
              gradientColors = listOf(NeonPurple, ElectricBlue)
            )
            Column {
              Text(
                text = "AI VIDEO EDITOR PIPELINE",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
              )
              Text(
                text = "Processing Speech Boundaries & Keyframes",
                color = Slate400,
                fontSize = 10.sp
              )
            }
          }

          Text(
            text = "${(state.progress * 100).toInt()}%",
            color = CyanHighlight,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        LinearProgressIndicator(
          progress = { state.progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
          color = CyanHighlight,
          trackColor = Slate800
        )

        Text(
          text = state.statusMessage,
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )

        // Stage Steps
        val currentStep = state.currentStage?.stepNumber ?: 1
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
          ProcessingStage.values().take(10).forEach { stage ->
            val isDone = currentStep > stage.stepNumber
            val isCurrent = currentStep == stage.stepNumber

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isCurrent) DarkSurface else Color.Transparent)
                .padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (isDone) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = EmeraldGlow,
                  modifier = Modifier.size(14.dp)
                )
              } else if (isCurrent) {
                CircularProgressIndicator(
                  modifier = Modifier.size(12.dp),
                  color = GoldHighlight,
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
