package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = ElectricIndigo,
  onPrimary = Color.White,
  primaryContainer = Slate800,
  onPrimaryContainer = Color.White,
  secondary = CyberCyan,
  onSecondary = Color.Black,
  secondaryContainer = Slate850,
  onSecondaryContainer = Color.White,
  tertiary = AmberZoom,
  onTertiary = Color.Black,
  background = DeepSlate950,
  onBackground = Slate200,
  surface = DeepSlate900,
  onSurface = Slate200,
  surfaceVariant = Slate850,
  onSurfaceVariant = Slate400,
  outline = Slate700,
  error = CrimsonSplit,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
