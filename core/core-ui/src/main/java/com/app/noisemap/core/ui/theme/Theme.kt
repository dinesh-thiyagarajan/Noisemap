package com.app.noisemap.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ColorAccentTeal,
    secondary = ColorAccentAmber,
    tertiary = ColorAccentGreen,
    background = ColorBackground,
    surface = ColorSurface,
    onPrimary = ColorBackground,
    onSecondary = ColorBackground,
    onTertiary = ColorBackground,
    onBackground = ColorTextPrimary,
    onSurface = ColorTextPrimary,
    error = ColorAccentRed
)

@Composable
fun NoisemapTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ColorBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
