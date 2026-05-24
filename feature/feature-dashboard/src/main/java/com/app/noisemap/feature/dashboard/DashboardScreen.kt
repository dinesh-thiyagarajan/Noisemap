package com.app.noisemap.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import com.app.noisemap.core.domain.model.DailyBarData
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
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onAppClick: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground),
    ) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ColorAccentTeal)
                }
            }
            uiState.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Something went wrong",
                        color = ColorTextSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            uiState.data != null -> {
                DashboardContent(data = uiState.data!!, onAppClick = onAppClick)
            }
        }
    }
}

@Composable
private fun DashboardContent(
    data: com.app.noisemap.core.domain.usecase.dashboard.DashboardData,
    onAppClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            SectionHeader("Today")
        }

        item {
            FocusScoreHeroCard(score = data.focusScore, trend = data.focusScoreTrend)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatChip(
                    label = "Total",
                    value = data.todayTotal.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatChip(
                    label = "Tapped",
                    value = data.todayTapped.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatChip(
                    label = "Dismissed",
                    value = data.todayDismissed.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (data.weeklyData.isNotEmpty()) {
            item { SectionHeader("This Week") }
            item { WeeklyBarChart(data = data.weeklyData) }
        }

        if (data.hourlyActivity.isNotEmpty()) {
            item { SectionHeader("When You're Interrupted") }
            item { HourlyHeatmap(hourlyData = data.hourlyActivity) }
        }

        if (data.topApps.isNotEmpty()) {
            item { SectionHeader("Your Loudest Apps") }
            itemsIndexed(data.topApps) { index, app ->
                AnimatedAppRow(index = index) {
                    AppSummaryRow(app = app, onClick = { onAppClick(app.packageName) })
                }
            }
        }

        if (data.recentNotifications.isNotEmpty()) {
            item { SectionHeader("Recent") }
            items(data.recentNotifications) { notification ->
                RecentNotificationRow(notification = notification)
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = ColorTextPrimary,
        fontWeight = FontWeight.SemiBold,
    )
}

// ─── Focus Score Hero ────────────────────────────────────────────────────────

@Composable
fun FocusScoreHeroCard(score: Int, trend: Float = 0f) {
    val scoreColor = when {
        score < 40 -> ColorAccentRed
        score < 70 -> ColorAccentAmber
        else -> ColorAccentTeal
    }
    val trendPositive = trend >= 0
    val trendText = when {
        trend > 0 -> "↑ %.0f vs yesterday".format(trend)
        trend < 0 -> "↓ %.0f vs yesterday".format(-trend)
        else -> "Same as yesterday"
    }

    NotifiqCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        backgroundColor = ColorSurfaceElevated,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Radial background glow
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(scoreColor.copy(alpha = 0.08f), Color.Transparent),
                        ),
                    ),
            )

            FocusScoreArcGauge(score = score, scoreColor = scoreColor, modifier = Modifier.size(180.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedCounter(target = score, textColor = ColorTextPrimary)
                Text(
                    text = "Focus Score",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorTextSecondary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = trendText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (trendPositive) ColorAccentGreen else ColorAccentRed,
                )
            }
        }
    }
}

@Composable
fun FocusScoreArcGauge(
    score: Int,
    scoreColor: Color = ColorAccentTeal,
    modifier: Modifier = Modifier,
) {
    val animatedSweep by animateFloatAsState(
        targetValue = (score / 100f) * 270f,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic),
        label = "ArcSweep",
    )

    Canvas(modifier = modifier.padding(20.dp)) {
        drawArc(
            color = ColorDivider,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round),
        )
        drawArc(
            brush = Brush.sweepGradient(
                listOf(scoreColor.copy(alpha = 0.6f), scoreColor),
            ),
            startAngle = 135f,
            sweepAngle = animatedSweep,
            useCenter = false,
            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
fun AnimatedCounter(target: Int, textColor: Color = ColorTextPrimary) {
    val animatedValue by animateIntAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "Counter",
    )
    Text(
        text = animatedValue.toString(),
        style = MaterialTheme.typography.displayMedium,
        color = textColor,
        fontWeight = FontWeight.Bold,
    )
}

// ─── Weekly Bar Chart ─────────────────────────────────────────────────────────

@Composable
fun WeeklyBarChart(data: List<DailyBarData>) {
    val maxCount = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

    NotifiqCard(backgroundColor = ColorSurfaceElevated) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            data.forEach { day ->
                WeeklyBarItem(
                    day = day,
                    fraction = day.count.toFloat() / maxCount,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun WeeklyBarItem(day: DailyBarData, fraction: Float, modifier: Modifier = Modifier) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        started = true
    }
    val animatedFraction by animateFloatAsState(
        targetValue = if (started) fraction else 0f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "Bar",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        if (day.count > 0) {
            Text(
                text = day.count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (day.isToday) ColorAccentTeal else ColorTextMuted,
                fontSize = 9.sp,
            )
        }
        Spacer(Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .width(20.dp)
                .height((80 * animatedFraction).coerceAtLeast(3f).dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(
                    if (day.isToday) ColorAccentTeal
                    else ColorTextMuted.copy(alpha = 0.3f),
                ),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = day.dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (day.isToday) ColorAccentTeal else ColorTextMuted,
            fontSize = 9.sp,
        )
    }
}

// ─── Hourly Heatmap ───────────────────────────────────────────────────────────

@Composable
fun HourlyHeatmap(hourlyData: List<HourlyActivity>) {
    val maxCount = hourlyData.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val countByHour = hourlyData.associate { it.hour to it.count }

    NotifiqCard(backgroundColor = ColorSurfaceElevated) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                (0..23).forEach { hour ->
                    val count = countByHour[hour] ?: 0
                    val intensity = count.toFloat() / maxCount
                    Box(
                        modifier = Modifier
                            .size(width = 10.dp, height = 32.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                ColorAccentTeal.copy(alpha = 0.1f + 0.7f * intensity),
                            ),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("12a", "6a", "12p", "6p", "11p").forEachIndexed { i, label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorTextMuted,
                        fontSize = 9.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = if (i == 0) TextAlign.Start else if (i == 4) TextAlign.End else TextAlign.Center,
                    )
                }
            }
        }
    }
}

// ─── App Summary Row ──────────────────────────────────────────────────────────

@Composable
fun AnimatedAppRow(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 80L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 3 },
    ) {
        content()
    }
}

@Composable
fun AppSummaryRow(app: AppNotificationSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorSurface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ColorSurfaceElevated)
                .border(1.dp, ColorDivider, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("📱", fontSize = 20.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName.ifEmpty { app.packageName },
                style = MaterialTheme.typography.bodyLarge,
                color = ColorTextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${app.totalCount} notifications · ${(app.tapRate * 100).toInt()}% tapped",
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTextSecondary,
            )
        }

        Spacer(Modifier.width(8.dp))

        InsightBadge(
            text = if (app.isNoise) "NOISE" else "SIGNAL",
            type = if (app.isNoise) InsightType.NOISE else InsightType.SIGNAL,
        )
    }
}

// ─── Recent Activity ──────────────────────────────────────────────────────────

@Composable
fun RecentNotificationRow(notification: Notification) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.92f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        ) + fadeIn(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ColorSurface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val indicatorColor = when {
                notification.removalReason == 1 -> ColorAccentGreen
                notification.removalReason != null -> ColorAccentRed
                else -> ColorAccentTeal
            }

            Box(
                modifier = Modifier
                    .size(4.dp, 36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(indicatorColor),
            )

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ColorSurfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                Text("📱", fontSize = 16.sp)
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.appName.ifEmpty { notification.packageName },
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorTextSecondary,
                )
                Text(
                    text = notification.title ?: notification.body ?: "No content",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatRelativeTime(notification.postedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorTextMuted,
                )
                Spacer(Modifier.height(2.dp))
                val actionLabel = when {
                    notification.removalReason == 1 -> "TAPPED"
                    notification.removalReason != null -> "DISMISSED"
                    else -> "ACTIVE"
                }
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = indicatorColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun formatRelativeTime(timestampMs: Long): String {
    val diffMs = System.currentTimeMillis() - timestampMs
    val minutes = diffMs / (1000 * 60)
    val hours = minutes / 60
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${hours / 24}d ago"
    }
}
