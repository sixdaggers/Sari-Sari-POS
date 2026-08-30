package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppleLightColorScheme =
  lightColorScheme(
    primary = AppleBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5F1FF),
    onPrimaryContainer = AppleBlue,
    secondary = AppleGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F8EE),
    onSecondaryContainer = Color(0xFF1E7E34),
    tertiary = AppleOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF3E0),
    onTertiaryContainer = Color(0xFFD35400),
    background = AppleLightBackground,
    onBackground = AppleTextPrimary,
    surface = AppleCardLight,
    onSurface = AppleTextPrimary,
    surfaceVariant = Color(0xFFE9E9EB),
    onSurfaceVariant = AppleTextSecondary,
    outline = AppleBorder,
    outlineVariant = Color(0xFFD1D1D6),
    error = AppleRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFECEB),
    onErrorContainer = AppleRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Enforce light theme as requested
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = AppleLightColorScheme,
    typography = Typography,
    content = content
  )
}

