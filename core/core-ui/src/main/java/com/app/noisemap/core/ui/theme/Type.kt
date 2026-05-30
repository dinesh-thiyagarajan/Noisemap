package com.app.noisemap.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val NoisemapTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 24.sp, fontWeight = FontWeight.Bold,
        color = NoisemapColors.TextPrimary, letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontSize = 20.sp, fontWeight = FontWeight.Bold,
        color = NoisemapColors.TextPrimary,
    ),
    titleMedium = TextStyle(
        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        color = NoisemapColors.TextSecondary, letterSpacing = 0.8.sp,
    ),
    displayLarge = TextStyle(
        fontSize = 36.sp, fontWeight = FontWeight.ExtraBold,
        color = NoisemapColors.TextPrimary, letterSpacing = (-1).sp,
    ),
    displayMedium = TextStyle(
        fontSize = 22.sp, fontWeight = FontWeight.Bold,
        color = NoisemapColors.TextPrimary,
    ),
    bodyLarge = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Medium,
        color = NoisemapColors.TextPrimary,
    ),
    bodyMedium = TextStyle(
        fontSize = 13.sp, fontWeight = FontWeight.Normal,
        color = NoisemapColors.TextSecondary, lineHeight = 19.sp,
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp, fontWeight = FontWeight.Normal,
        color = NoisemapColors.TextMuted, letterSpacing = 0.2.sp,
    ),
)
