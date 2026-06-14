package com.aibudgetplanner.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF3B82F6),
    secondary = Color(0xFF10B981),
    background = Color(0xFF090E1A),
    surface = Color(0xFF111827),
    onBackground = Color.White,
    onSurface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF3B82F6),
    secondary = Color(0xFF10B981),
    background = Color(0xFF090E1A),
    surface = Color(0xFF111827),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun AIBudgetPlannerTheme(
    darkTheme: Boolean = true, // Force dark mode by default
    dynamicColor: Boolean = false, // Disable dynamic color overrides
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
