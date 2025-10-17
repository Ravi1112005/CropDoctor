package com.example.cropdoctor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreen, // Main brand color
    secondary = LightGreen, // Lighter variant for accents
    background = DarkBackground, // Dark background for dark mode
    surface = DarkCard, // Card backgrounds in dark mode
    onPrimary = Color.White, // Text on primary color buttons
    onSecondary = TextBlack, // Text on secondary color elements
    onBackground = TextWhite, // Main text color for dark mode
    onSurface = TextWhite, // Text on cards for dark mode
    error = AccentRed, // Color for errors
    errorContainer = Color(0xFF6e2f2f) // A darker red for error backgrounds in dark mode
)

private val LightColorScheme = lightColorScheme(
    primary = DarkGreen, // Main brand color
    secondary = LightGreen, // Lighter variant used for backgrounds
    background = LightGreen, // The main background of the app
    surface = CardBackground, // The color of cards
    onPrimary = Color.White, // Text on primary color buttons
    onSecondary = DarkGreen, // Text on secondary color surfaces
    onBackground = TextBlack, // Main text color
    onSurface = TextBlack, // Text on cards
    error = AccentRed, // Color for errors
    errorContainer = Color(0xFFFDE7E7) // Light red for diagnosis card background
)

@Composable
fun CropDoctorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
