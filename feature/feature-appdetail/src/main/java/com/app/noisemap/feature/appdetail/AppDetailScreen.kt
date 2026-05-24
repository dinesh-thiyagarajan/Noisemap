package com.app.noisemap.feature.appdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.noisemap.core.domain.model.AppNotificationSummary
import com.app.noisemap.core.domain.model.HourlyActivity
import com.app.noisemap.core.domain.model.Notification
import com.app.noisemap.core.ui.components.InsightBadge
import com.app.noisemap.core.ui.components.InsightType
import com.app.noisemap.core.ui.components.NotifiqCard
import com.app.noisemap.core.ui.components.StatChip
import com.app.noisemap.core.ui.theme.ColorAccentAmber
import com.app.noisemap.core.ui.theme.ColorAccentGreen
import com.app.noisemap.core.ui.theme.ColorAccentRed
import com.app.noisemap.core.ui.theme.ColorAccentTeal
import com.app.noisemap.core.ui.theme.ColorBackground
import com.app.noisemap.core.ui.theme.ColorDivider
import com.app.noisemap.core.ui.theme.ColorSurface
import com.app.noisemap.core.ui.theme.ColorSurfaceElevated
import com.app.noisemap.core.ui.theme.ColorTextMuted
import com.app.noisemap.core.ui.theme.ColorTextPrimary
import com.app.noisemap.core.ui.theme.ColorTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppDetailScreen(
    packageName: String,
    onBack: () -> Unit,
    viewModel: AppDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground),
    ) {
        AppDetailTopBar(
            packageName = uiState.summary?.appName ?: packageName,
            onBack = onBack,
        )

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ColorAccentTeal)
            }
            uiState.summary != null -> AppDetailContent(
                summary = uiState.summary!!,
                hourlyActivity = uiState.hourlyActivity,
                notifications = uiState.notifications,
            )
            else -> EmptyState()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDetailTopBar(packageName: String, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = packageName,
                color = ColorTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Text("←", color = ColorTextPrimary, fontSize = 20.sp)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorBackground),
    )
}

@Composable
private fun AppDetailContent(
    summary: AppNotificationSummary,
    hourlyActivity: List<HourlyActivity>,
    notifications: List<Notification>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        item {
            AppHeader(summary = summary)
        }

        // Signal Ring
        item {
            SignalScoreCard(tapRate = summary.tapRate, isNoise = summary.isNoise)
        }

        // Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatChip("Total", summary.totalCount.toString(), modifier = Modifier.weight(1f))
                StatChip("Tapped", summary.tappedCount.toString(), modifier = Modifier.weight(1f))
                StatChip("Dismissed", summary.dismissedCount.toString(), modifier = Modifier.weight(1f))
            }
        }

        // Hourly breakdown
        if (hourlyActivity.isNotEmpty()) {
            item {
                Text(
                    text = "Hourly Pattern",
                    style = MaterialTheme.typography.titleLarge,
                    color = ColorTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
                AppHourlyChart(hourlyActivity = hourlyActivity)
            }
        }

        // Verdict banner
        item {
            VerdictBanner(summary = summary)
        }

        // Notification history
        if (notifications.isNotEmpty()) {
            item {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleLarge,
                    color = ColorTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(notifications) { notification ->
                NotificationHistoryRow(notification = notification)
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun AppHeader(summary: AppNotificationSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorSurfaceElevated)
            .border(1.dp, ColorDivider, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ColorSurface)
                .border(1.dp, ColorDivider, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("📱", fontSize = 32.sp)
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = summary.appName.ifEmpty { summary.packageName },
                style = MaterialTheme.typography.titleLarge,
                color = ColorTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${summary.totalCount} notifications · ${(summary.tapRate * 100).toInt()}% tap rate",
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTextSecondary,
            )
            Spacer(Modifier.height(8.dp))
            InsightBadge(
                text = if (summary.isNoise) "NOISE" else "SIGNAL",
                type = if (summary.isNoise) InsightType.NOISE else InsightType.SIGNAL,
            )
        }
    }
}

@Composable
private fun SignalScoreCard(tapRate: Float, isNoise: Boolean) {
    val scoreColor = when {
        tapRate < 0.2f -> ColorAccentRed
        tapRate < 0.5f -> ColorAccentAmber
        else -> ColorAccentGreen
    }

    val animatedSweep by animateFloatAsState(
        targetValue = tapRate * 270f,
        animationSpec = tween(1500, easing = EaseOutCubic),
        label = "SignalRing",
    )

    NotifiqCard(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        backgroundColor = ColorSurfaceElevated,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(140.dp).padding(16.dp)) {
                drawArc(
                    color = ColorDivider,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                )
                drawArc(
                    color = scoreColor,
                    startAngle = 135f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(tapRate * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = scoreColor,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "tap rate",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun AppHourlyChart(hourlyActivity: List<HourlyActivity>) {
    val maxCount = hourlyActivity.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val countByHour = hourlyActivity.associate { it.hour to it.count }

    NotifiqCard(backgroundColor = ColorSurfaceElevated) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            (0..23).forEach { hour ->
                val count = countByHour[hour] ?: 0
                var started by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { started = true }
                val anim by animateFloatAsState(
                    targetValue = if (started) count.toFloat() / maxCount else 0f,
                    animationSpec = tween(700, easing = EaseOutCubic),
                    label = "HourBar$hour",
                )
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height((55 * anim).coerceAtLeast(2f).dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(ColorAccentTeal.copy(alpha = 0.2f + 0.6f * anim)),
                )
            }
        }
    }
}

@Composable
private fun VerdictBanner(summary: AppNotificationSummary) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val tapPct = (summary.tapRate * 100).toInt()
    val verdictText = when {
        summary.isNoise -> "You dismiss ${100 - tapPct}% of ${summary.appName} notifications. Consider silencing this app."
        summary.tapRate > 0.6f -> "You tap ${tapPct}% of ${summary.appName} notifications. This app earns its interruptions."
        else -> "You engage with ${tapPct}% of ${summary.appName} notifications."
    }
    val (bg, fg) = when {
        summary.isNoise -> ColorAccentRed.copy(alpha = 0.12f) to ColorAccentRed
        summary.tapRate > 0.6f -> ColorAccentGreen.copy(alpha = 0.12f) to ColorAccentGreen
        else -> ColorAccentAmber.copy(alpha = 0.12f) to ColorAccentAmber
    }

    AnimatedVisibility(visible = visible, enter = slideInVertically { it } + fadeIn()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bg)
                .border(1.dp, fg.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Text(
                text = verdictText,
                style = MaterialTheme.typography.bodyLarge,
                color = fg,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun NotificationHistoryRow(notification: Notification) {
    val actionColor = when {
        notification.removalReason == 1 -> ColorAccentGreen
        notification.removalReason != null -> ColorAccentRed
        else -> ColorAccentTeal
    }
    val actionLabel = when {
        notification.removalReason == 1 -> "TAPPED"
        notification.removalReason != null -> "DISMISSED"
        else -> "ACTIVE"
    }
    val formatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ColorSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 44.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(actionColor),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title ?: "(No title)",
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            notification.body?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = formatter.format(Date(notification.postedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = ColorTextMuted,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelSmall,
            color = actionColor,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📭", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No data for this app yet",
                style = MaterialTheme.typography.bodyLarge,
                color = ColorTextSecondary,
            )
        }
    }
}
