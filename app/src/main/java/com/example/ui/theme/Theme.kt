package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  themeIndex: Int = 0,
  fontScale: Float = 1.0f,
  content: @Composable () -> Unit,
) {
  val selectedTheme = ThemeOptions.getOrElse(themeIndex) { ThemeOptions[0] }

  val colorScheme = if (darkTheme) {
    darkColorScheme(
      primary = selectedTheme.primary,
      secondary = selectedTheme.secondary,
      tertiary = selectedTheme.tertiary,
      background = Color(0xFF121318),
      surface = Color(0xFF1E1F26),
      onPrimary = Color.White,
      onSecondary = Color.White,
      onTertiary = Color.White,
      onBackground = Color(0xFFE3E2E6),
      onSurface = Color(0xFFE3E2E6),
      surfaceVariant = Color(0xFF2C2D35),
      onSurfaceVariant = Color(0xFFC4C6D0)
    )
  } else {
    lightColorScheme(
      primary = selectedTheme.primary,
      secondary = selectedTheme.secondary,
      tertiary = selectedTheme.tertiary,
      background = Color(0xFFF3F4F9),
      surface = Color.White,
      onPrimary = Color.White,
      onSecondary = Color.White,
      onTertiary = Color.White,
      onBackground = Color(0xFF1A1C1E),
      onSurface = Color(0xFF1A1C1E),
      surfaceVariant = Color(0xFFE1E2EC),
      onSurfaceVariant = Color(0xFF43474E)
    )
  }

  val scaledTypography = getScaledTypography(fontScale)

  MaterialTheme(
    colorScheme = colorScheme,
    typography = scaledTypography,
    content = content
  )
}
