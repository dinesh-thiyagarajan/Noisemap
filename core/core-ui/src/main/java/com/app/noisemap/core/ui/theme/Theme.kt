package com.app.noisemap.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NoisemapDarkColorScheme = darkColorScheme(
    background        = NoisemapColors.Background,
    surface           = NoisemapColors.Surface,
    surfaceVariant    = NoisemapColors.SurfaceElevated,
    primary           = NoisemapColors.AccentTeal,
    secondary         = NoisemapColors.AccentGreen,
    error             = NoisemapColors.AccentRed,
    onBackground      = NoisemapColors.TextPrimary,
    onSurface         = NoisemapColors.TextPrimary,
    onPrimary         = NoisemapColors.Background,
    outline           = NoisemapColors.BorderDefault,
)

@Composable
fun NoisemapTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = NoisemapColors.Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = NoisemapDarkColorScheme,
        typography = NoisemapTypography,
        shapes = NoisemapShapes,
        content = content,
    )
}
