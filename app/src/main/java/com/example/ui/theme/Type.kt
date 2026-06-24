package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun getScaledTypography(scaleFactor: Float): Typography {
    fun scaleStyle(fontWeight: FontWeight, defaultSize: Float, defaultLine: Float): TextStyle {
        return TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = fontWeight,
            fontSize = (defaultSize * scaleFactor).sp,
            lineHeight = (defaultLine * scaleFactor).sp
        )
    }

    return Typography(
        displayLarge = scaleStyle(FontWeight.Normal, 57f, 64f),
        displayMedium = scaleStyle(FontWeight.Normal, 45f, 52f),
        displaySmall = scaleStyle(FontWeight.Normal, 36f, 44f),
        headlineLarge = scaleStyle(FontWeight.Normal, 32f, 40f),
        headlineMedium = scaleStyle(FontWeight.Normal, 28f, 36f),
        headlineSmall = scaleStyle(FontWeight.Normal, 24f, 32f),
        titleLarge = scaleStyle(FontWeight.Medium, 22f, 28f),
        titleMedium = scaleStyle(FontWeight.Medium, 16f, 24f),
        titleSmall = scaleStyle(FontWeight.Medium, 14f, 20f),
        bodyLarge = scaleStyle(FontWeight.Normal, 16f, 24f),
        bodyMedium = scaleStyle(FontWeight.Normal, 14f, 20f),
        bodySmall = scaleStyle(FontWeight.Normal, 12f, 16f),
        labelLarge = scaleStyle(FontWeight.Medium, 14f, 20f),
        labelMedium = scaleStyle(FontWeight.Medium, 12f, 16f),
        labelSmall = scaleStyle(FontWeight.Medium, 11f, 16f)
    )
}

val Typography = getScaledTypography(1.0f)
