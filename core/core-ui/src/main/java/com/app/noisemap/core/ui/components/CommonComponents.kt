package com.app.noisemap.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.noisemap.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifiqScaffold(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
        containerColor = ColorBackground,
        contentColor = ColorTextPrimary,
        content = content
    )
}

@Composable
fun NotifiqCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ColorSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, ColorDivider, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        content = content
    )
}

@Composable
fun StatChip(
    label: String,
    value: String,
    trend: String? = null,
    trendPositive: Boolean = true,
    modifier: Modifier = Modifier
) {
    NotifiqCard(
        modifier = modifier.height(100.dp),
        backgroundColor = ColorSurfaceElevated
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ColorTextSecondary
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = ColorTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (trend != null) {
                    Text(
                        text = trend,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (trendPositive) ColorAccentGreen else ColorAccentRed,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InsightBadge(
    text: String,
    type: InsightType,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (type) {
        InsightType.SIGNAL -> ColorAccentGreen.copy(alpha = 0.1f) to ColorAccentGreen
        InsightType.NOISE -> ColorAccentRed.copy(alpha = 0.1f) to ColorAccentRed
        InsightType.MIXED -> ColorAccentAmber.copy(alpha = 0.1f) to ColorAccentAmber
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

enum class InsightType {
    SIGNAL, NOISE, MIXED
}
