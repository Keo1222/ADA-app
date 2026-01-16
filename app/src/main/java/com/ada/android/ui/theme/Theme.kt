package com.ada.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// A.D.A. Color Palette
val AdaCyan = Color(0xFF00BCD4)
val AdaCyanDark = Color(0xFF00838F)
val AdaPurple = Color(0xFF9C27B0)
val AdaPurpleDark = Color(0xFF6A1B9A)
val AdaBackground = Color(0xFF0A0A14)
val AdaSurface = Color(0xFF1A1A2E)
val AdaSurfaceVariant = Color(0xFF2A2A3E)
val AdaOnSurface = Color(0xFFFFFFFF)
val AdaOnSurfaceVariant = Color(0xFFB0B0B0)
val AdaError = Color(0xFFCF6679)
val AdaSuccess = Color(0xFF4CAF50)
val AdaWarning = Color(0xFFFFC107)

// Dark color scheme (A.D.A. is always dark themed)
private val DarkColorScheme = darkColorScheme(
    primary = AdaCyan,
    onPrimary = Color.Black,
    primaryContainer = AdaCyanDark,
    onPrimaryContainer = Color.White,
    secondary = AdaPurple,
    onSecondary = Color.White,
    secondaryContainer = AdaPurpleDark,
    onSecondaryContainer = Color.White,
    tertiary = AdaSuccess,
    onTertiary = Color.Black,
    background = AdaBackground,
    onBackground = AdaOnSurface,
    surface = AdaSurface,
    onSurface = AdaOnSurface,
    surfaceVariant = AdaSurfaceVariant,
    onSurfaceVariant = AdaOnSurfaceVariant,
    error = AdaError,
    onError = Color.Black
)

@Composable
fun ADATheme(
    darkTheme: Boolean = true, // A.D.A. is always dark
    dynamicColor: Boolean = false, // Disable dynamic colors for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        else -> DarkColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
