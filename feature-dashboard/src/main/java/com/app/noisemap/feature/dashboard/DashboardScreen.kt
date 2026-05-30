package com.app.noisemap.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.noisemap.core.domain.model.AppNotificationSummary
import com.app.noisemap.core.domain.model.DailyBarData
import com.app.noisemap.core.domain.model.HourlyActivity
import com.app.noisemap.core.domain.model.Notification
import com.app.noisemap.core.ui.components.ActionTag
import com.app.noisemap.core.ui.components.AnimatedCounter
import com.app.noisemap.core.ui.components.AnimatedListItem
import com.app.noisemap.core.ui.components.FocusScoreArc
import com.app.noisemap.core.ui.components.NoisemapCard
import com.app.noisemap.core.ui.components.NotificationAction
import com.app.noisemap.core.ui.components.SectionTitle
import com.app.noisemap.core.ui.components.Verdict
import com.app.noisemap.core.ui.components.VerdictBadge
import com.app.noisemap.core.ui.theme.NoisemapColors
import java.time.LocalTime
import kotlin.math.abs

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onAppClick: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NoisemapColors.Background)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NoisemapColors.AccentTeal)
                }
            }
            uiState.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Something went wrong",
                        color = NoisemapColors.TextSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            uiState.data != null -> {
                DashboardContent(
                    data = uiState.data!!,
                    onAppClick = { packageName, appName ->
                        viewModel.onAppCardTapped(packageName, appName)
                        onAppClick(packageName)
                    },
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    data: com.app.noisemap.core.domain.usecase.dashboard.DashboardData,
    onAppClick: (packageName: String, appName: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            AnimatedListItem(0) {
                Column {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    FocusScoreAndWeeklyCard(data = data)
                }
            }
        }

        item {
            AnimatedListItem(1) {
                StatsRow(data = data)
            }
        }

        item {
            AnimatedListItem(2) {
                HeatmapCard(data = data)
            }
        }

        if (data.topApps.isNotEmpty()) {
            item {
                AnimatedListItem(3) {
                    TopAppsCard(apps = data.topApps, onAppClick = onAppClick)
                }
            }
        }

        if (data.recentNotifications.isNotEmpty()) {
            item {
                AnimatedListItem(4) {
                    RecentCard(notifications = data.recentNotifications)
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
fun FocusScoreAndWeeklyCard(data: com.app.noisemap.core.domain.usecase.dashboard.DashboardData) {
    NoisemapCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(4.dp))

            // Large centred arc
            FocusScoreArc(
                score = data.focusScore,
                modifier = Modifier.size(136.dp),
                strokeWidth = 10.dp,
            )

            Spacer(Modifier.height(8.dp))

            // Descriptor label
            val descriptor = when {
                data.focusScore >= 70 -> "Great focus!"
                data.focusScore >= 40 -> "Getting there"
                else                  -> "Needs work"
            }
            val descriptorColor = when {
                data.focusScore >= 70 -> NoisemapColors.AccentTeal
                data.focusScore >= 40 -> NoisemapColors.AccentAmber
                else                  -> NoisemapColors.AccentRed
            }
            Text(
                text = descriptor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = descriptorColor,
            )

            Spacer(Modifier.height(6.dp))

            // Trend pill
            val trendText = if (data.focusScoreTrend >= 0)
                "↑ ${data.focusScoreTrend.toInt()}% vs yesterday"
            else
                "↓ ${abs(data.focusScoreTrend.toInt())}% vs yesterday"
            val trendColor = if (data.focusScoreTrend >= 0)
                NoisemapColors.AccentTeal else NoisemapColors.AccentRed

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(trendColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = trendText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = trendColor,
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = NoisemapColors.BorderSubtle, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            // Weekly bar chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("This week")
            }
            Spacer(Modifier.height(6.dp))
            DashboardWeeklyBarChart(data = data.weeklyData)

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun DashboardWeeklyBarChart(data: List<DailyBarData>) {
    val maxCount = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        data.forEach { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight(day.count.toFloat() / maxCount)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(
                            if (day.isToday) NoisemapColors.AccentTeal
                            else Color(0xFF3D5A6B),
                        ),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = day.dayLabel.take(1),
                    fontSize = 9.sp,
                    color = if (day.isToday) NoisemapColors.AccentTeal else NoisemapColors.TextMuted,
                )
            }
        }
    }
}

@Composable
fun StatsRow(data: com.app.noisemap.core.domain.usecase.dashboard.DashboardData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val tapRate = if (data.todayTotal > 0) (data.todayTapped * 100 / data.todayTotal) else 0
        val dismissRate = if (data.todayTotal > 0) (data.todayDismissed * 100 / data.todayTotal) else 0

        StatCardItem(
            label = "Total",
            value = data.todayTotal,
            trend = null, // Logic for total trend not explicitly in DashboardData
            trendPositive = false,
            modifier = Modifier.weight(1f),
        )
        StatCardItem(
            label = "Tapped",
            value = data.todayTapped,
            trend = "${tapRate}%",
            trendPositive = true,
            valueColor = NoisemapColors.AccentGreen,
            modifier = Modifier.weight(1f),
        )
        StatCardItem(
            label = "Dismissed",
            value = data.todayDismissed,
            trend = "${dismissRate}%",
            trendPositive = false,
            valueColor = NoisemapColors.AccentRed,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun StatCardItem(
    label: String,
    value: Int,
    trend: String?,
    trendPositive: Boolean,
    valueColor: Color = NoisemapColors.TextPrimary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(NoisemapColors.SurfaceNav)
            .border(1.dp, NoisemapColors.BorderDefault, RoundedCornerShape(10.dp))
            .padding(8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(2.dp))
        AnimatedCounter(value, style = MaterialTheme.typography.displayMedium.copy(color = valueColor))
        trend?.let {
            Text(
                it,
                fontSize = 10.sp,
                color = if (trendPositive) NoisemapColors.AccentGreen else NoisemapColors.AccentRed,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
fun HeatmapCard(data: com.app.noisemap.core.domain.usecase.dashboard.DashboardData) {
    val maxCount = data.hourlyActivity.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val peakHour = data.hourlyActivity.maxByOrNull { it.count }?.hour ?: -1

    NoisemapCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle("Hourly activity")
            if (peakHour >= 0) {
                Text(
                    "Peak: ${formatHour(peakHour)}",
                    fontSize = 10.sp,
                    color = NoisemapColors.AccentTeal,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(12),
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            userScrollEnabled = false,
        ) {
            items(24) { hour ->
                val count = data.hourlyActivity.find { it.hour == hour }?.count ?: 0
                val intensity = count.toFloat() / maxCount
                val isCurrentHour = hour == LocalTime.now().hour
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            when {
                                intensity == 0f -> NoisemapColors.SurfaceElevated
                                intensity < 0.25f -> NoisemapColors.AccentTeal.copy(alpha = 0.2f)
                                intensity < 0.5f  -> NoisemapColors.AccentTeal.copy(alpha = 0.45f)
                                intensity < 0.75f -> NoisemapColors.AccentTeal.copy(alpha = 0.7f)
                                else              -> NoisemapColors.AccentTeal
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCurrentHour) {
                        com.app.noisemap.core.ui.components.PulsingDot(size = 4.dp)
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("12a", "6a", "12p", "6p", "11p").forEach {
                Text(it, fontSize = 9.sp, color = NoisemapColors.TextMuted)
            }
        }
    }
}

@Composable
fun TopAppsCard(
    apps: List<AppNotificationSummary>,
    onAppClick: (packageName: String, appName: String) -> Unit,
) {
    val maxTotal = apps.maxOfOrNull { it.totalCount }?.coerceAtLeast(1) ?: 1
    NoisemapCard {
        SectionTitle("Loudest Apps")
        Spacer(Modifier.height(8.dp))
        apps.forEach { app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAppClick(app.packageName, app.appName) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(app.packageName) // Coil should be configured to handle package name if needed, or use a custom fetcher
                        .build(),
                    contentDescription = app.appName,
                    modifier = Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)),
                )

                Text(
                    app.appName.ifEmpty { app.packageName },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )

                val barColor = if (app.isNoise) NoisemapColors.AccentRed else NoisemapColors.AccentTeal
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NoisemapColors.SurfaceElevated),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(app.totalCount.toFloat() / maxTotal)
                            .clip(RoundedCornerShape(2.dp))
                            .background(barColor),
                    )
                }

                VerdictBadge(if (app.isNoise) Verdict.NOISE else Verdict.SIGNAL)
            }
            HorizontalDivider(color = NoisemapColors.BorderSubtle, thickness = 0.5.dp)
        }
    }
}

@Composable
fun RecentCard(notifications: List<Notification>) {
    NoisemapCard {
        SectionTitle("Recent")
        Spacer(Modifier.height(8.dp))
        notifications.forEach { notif ->
            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                val action = when {
                    notif.removalReason == 1 -> NotificationAction.TAPPED
                    notif.removalReason != null -> NotificationAction.DISMISSED
                    else -> NotificationAction.ACTIVE
                }
                val indicatorColor = when (action) {
                    NotificationAction.TAPPED    -> NoisemapColors.AccentGreen
                    NotificationAction.DISMISSED -> NoisemapColors.AccentRed
                    NotificationAction.ACTIVE    -> NoisemapColors.AccentTeal
                }
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(indicatorColor),
                )

                AsyncImage(
                    model = notif.packageName,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp))
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(notif.appName, fontSize = 10.sp, color = NoisemapColors.TextMuted)
                    Text(notif.title ?: notif.body ?: "", style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                    Spacer(Modifier.height(2.dp))
                    ActionTag(action, formatRelativeTime(notif.postedAt))
                }
            }
            HorizontalDivider(color = NoisemapColors.BorderSubtle, thickness = 0.5.dp)
        }
    }
}

private fun formatHour(hour: Int): String {
    return when {
        hour == 0 -> "12 AM"
        hour < 12 -> "$hour AM"
        hour == 12 -> "12 PM"
        else -> "${hour - 12} PM"
    }
}

private fun formatRelativeTime(timestampMs: Long): String {
    val diffMs = System.currentTimeMillis() - timestampMs
    val minutes = diffMs / (1000 * 60)
    val hours = minutes / 60
    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${hours / 24}d ago"
    }
}
