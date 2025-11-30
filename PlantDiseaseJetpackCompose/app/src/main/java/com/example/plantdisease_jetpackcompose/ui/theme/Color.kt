package com.example.plantdisease_jetpackcompose.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)


// Light Theme Colors
val LightPrimary = Color(0xFF10B981)
val LightSecondary = Color(0xFF0EA5E9)
val LightBackground = Color(0xFFF9FAFB)
val LightSurface = Color(0xFFFFFFFF)
val LightError = Color(0xFFEF4444)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF1F2937)
val LightOnSurface = Color(0xFF1F2937)

// Dark Theme Colors
val DarkPrimary = Color(0xFF34D399)
val DarkSecondary = Color(0xFF38BDF8)
val DarkBackground = Color(0xFF111827)
val DarkSurface = Color(0xFF1F2937)
val DarkError = Color(0xFFF87171)
val DarkOnPrimary = Color(0xFF000000)
val DarkOnBackground = Color(0xFFF9FAFB)
val DarkOnSurface = Color(0xFFF9FAFB)

// Disease Colors (same for both themes)
val BlightColor = Color(0xFFEF4444)
val CobRootColor = Color(0xFFF59E0B)
val RustColor = Color(0xFFF97316)
val GraySpotColor = Color(0xFF8B5CF6)
val HealthyColor = Color(0xFF10B981)

// Gradient Colors
object GradientColors {
    // Light Mode Gradients
    val LightBackgroundGradient = listOf(
        Color(0xFFD0F4DE),
        Color(0xFFA9DEF9),
        Color(0xFFE4C1F9)
    )

    val LightPrimaryGradient = listOf(
        Color(0xFF10B981),
        Color(0xFF059669)
    )

    val LightSecondaryGradient = listOf(
        Color(0xFF0EA5E9),
        Color(0xFF0284C7)
    )

    val LightHeaderGradient = listOf(
        Color(0xFF10B981),
        Color(0xFF0EA5E9)
    )

    // Dark Mode Gradients
    val DarkBackgroundGradient = listOf(
        Color(0xFF1F2937),
        Color(0xFF111827),
        Color(0xFF1E293B)
    )

    val DarkPrimaryGradient = listOf(
        Color(0xFF059669),
        Color(0xFF047857)
    )

    val DarkSecondaryGradient = listOf(
        Color(0xFF0284C7),
        Color(0xFF0369A1)
    )

    val DarkHeaderGradient = listOf(
        Color(0xFF059669),
        Color(0xFF0284C7)
    )
}
