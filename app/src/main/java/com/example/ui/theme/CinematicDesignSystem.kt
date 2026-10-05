package com.example.ui.theme

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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Premium 3D Dark Glass Card with customizable gradient border and soft ambient depth shadow.
 */
@Composable
fun CinematicGlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(20.dp),
  backgroundColor: Color = GlassSurface,
  borderBrush: Brush = Brush.linearGradient(
    listOf(
      ElectricBlueGlow.copy(alpha = 0.45f),
      NeonPurpleGlow.copy(alpha = 0.35f),
      Color.Transparent
    )
  ),
  borderWidth: Dp = 1.dp,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = modifier
      .shadow(elevation = 12.dp, shape = shape, spotColor = NeonPurple.copy(alpha = 0.25f), ambientColor = Color.Black)
      .clip(shape)
      .background(backgroundColor)
      .border(borderWidth, borderBrush, shape)
      .padding(1.dp)
  ) {
    content()
  }
}

/**
 * 3D Elevated Icon Container with gradient reflection and highlight ring.
 */
@Composable
fun Cinematic3DIconContainer(
  icon: ImageVector,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
  iconSize: Dp = 22.dp,
  gradientColors: List<Color> = listOf(ElectricBlue, NeonPurple),
  tint: Color = Color.White
) {
  Box(
    modifier = modifier
      .size(size)
      .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp), spotColor = gradientColors.first().copy(alpha = 0.5f))
      .clip(RoundedCornerShape(14.dp))
      .background(Brush.linearGradient(gradientColors))
      .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
          listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)
        ),
        shape = RoundedCornerShape(14.dp)
      ),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = tint,
      modifier = Modifier.size(iconSize)
    )
  }
}

/**
 * High-end glowing badge for statuses (e.g., ⚡ AI POWERED, ✓ 9:16 VERTICAL).
 */
@Composable
fun GlowBadge(
  text: String,
  modifier: Modifier = Modifier,
  accentColor: Color = EmeraldGreen,
  backgroundColor: Color = accentColor.copy(alpha = 0.15f)
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(backgroundColor)
      .border(0.75.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = accentColor,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.5.sp
    )
  }
}

/**
 * Primary glowing pill CTA with 3D elevation, purple->blue gradient, and micro-press animation.
 */
@Composable
fun CinematicPillButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  leadingIcon: ImageVector? = null,
  gradient: Brush = Brush.horizontalGradient(listOf(NeonPurple, ElectricBlue)),
  enabled: Boolean = true
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.96f else 1.0f,
    label = "button_press_scale"
  )

  Box(
    modifier = modifier
      .scale(scale)
      .shadow(
        elevation = if (enabled) 12.dp else 2.dp,
        shape = RoundedCornerShape(30.dp),
        spotColor = NeonPurpleGlow.copy(alpha = 0.6f)
      )
      .clip(RoundedCornerShape(30.dp))
      .background(if (enabled) gradient else Brush.horizontalGradient(listOf(Slate700, Slate800)))
      .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
          listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)
        ),
        shape = RoundedCornerShape(30.dp)
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick
      )
      .padding(horizontal = 16.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (leadingIcon != null) {
        Icon(
          imageVector = leadingIcon,
          contentDescription = null,
          tint = GoldHighlight,
          modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
      }
      Text(
        text = text,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp
      )
    }
  }
}

/**
 * Animated Circular Score Gauge for the AI Scoring panel.
 */
@Composable
fun CircularScoreGauge(
  score: Int,
  label: String,
  color: Color,
  modifier: Modifier = Modifier,
  size: Dp = 76.dp,
  strokeWidth: Dp = 6.dp
) {
  val animatedProgress by animateFloatAsState(
    targetValue = score / 100f,
    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
    label = "circular_score_anim"
  )

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Box(
      modifier = Modifier.size(size),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val sweepAngle = animatedProgress * 360f
        val stroke = strokeWidth.toPx()
        val diameter = size.toPx() - stroke

        // Inactive background track
        drawCircle(
          color = Color(0xFF1E293B).copy(alpha = 0.6f),
          radius = diameter / 2f,
          style = Stroke(width = stroke)
        )

        // Active glowing arc
        drawArc(
          brush = Brush.sweepGradient(
            listOf(color.copy(alpha = 0.7f), color, color)
          ),
          startAngle = -90f,
          sweepAngle = sweepAngle,
          useCenter = false,
          topLeft = Offset(stroke / 2f, stroke / 2f),
          size = Size(diameter, diameter),
          style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
      }

      Text(
        text = "$score%",
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    }

    Text(
      text = label,
      color = Slate400,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium
    )
  }
}
