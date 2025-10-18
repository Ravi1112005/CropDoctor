package com.example.cropdoctor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MediumGreen,
    secondary = DarkCard, 
    background = DarkBackground, // Use a true dark background
    surface = DarkCard, // Cards are a slightly lighter dark color
    onPrimary = TextWhite,
    onSecondary = TextWhite,
    onBackground = TextWhite, // Text on the main background is white
    onSurface = TextWhite, // Text on cards is white
    error = AccentRed,
    errorContainer = DarkErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = DarkGreen,
    secondary = LightGreen,
    background = LightGreen,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = DarkGreen,
    onBackground = TextBlack,
    onSurface = TextBlack,
    error = AccentRed,
    errorContainer = Color(0xFFFDE7E7)
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
