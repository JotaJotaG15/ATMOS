package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WeatherSkyPrimaryDark,
    onPrimary = Color(0xFF00354E),
    primaryContainer = Color(0xFF004D70),
    onPrimaryContainer = Color(0xFFCBE6FF),
    secondary = WeatherSecondaryDark,
    onSecondary = Color(0xFF003822),
    secondaryContainer = Color(0xFF005234),
    onSecondaryContainer = Color(0xFF8FF7C1),
    tertiary = WeatherAmberDark,
    onTertiary = Color(0xFF452B00),
    background = WeatherBackgroundDark,
    onBackground = Color(0xFFE2E8F0),
    surface = WeatherSurfaceDark,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = WeatherSurfaceVariantDark,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = WeatherCardBorderDark,
    error = WeatherRedDark
)

private val LightColorScheme = lightColorScheme(
    primary = WeatherSkyPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = WeatherSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA7F3D0),
    onSecondaryContainer = Color(0xFF047857),
    tertiary = WeatherAmberLight,
    onTertiary = Color.White,
    background = WeatherBackgroundLight,
    onBackground = Color(0xFF0F172A),
    surface = WeatherSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = WeatherSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = WeatherCardBorderLight,
    error = WeatherRedLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted atmospheric weather scheme by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
