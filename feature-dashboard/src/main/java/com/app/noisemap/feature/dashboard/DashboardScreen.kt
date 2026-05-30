package com.app.noisemap.feature.dashboard

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.noisemap.core.domain.model.AppNotificationSummary
import com.app.noisemap.core.domain.model.DailyBarData
import com.app.noisemap.core.domain.model.Notification
import com.app.noisemap.core.ui.components.ActionTag
import com.app.noisemap.core.ui.components.AnimatedCounter
import com.app.noisemap.core.ui.components.AnimatedListItem
import com.app.noisemap.core.ui.components.FocusScoreArc
import com.app.noisemap.core.ui.components.NoisemapCard
import com.app.noisemap.core.ui.components.NotificationAction
import com.app.noisemap.core.ui.components.PulsingDot
import com.app.noisemap.core.ui.components.SectionTitle
import com.app.noisemap.core.ui.components.Verdict
import com.app.noisemap.core.ui.components.VerdictBadge
import com.app.noisemap.core.ui.theme.NoisemapColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

// ─── Screen root ────────────────────────────────────────────────────────────

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

// ─── Feed ───────────────────────────────────────────────────────────────────

@Composable
private fun DashboardContent(
    data: com.app.noisemap.core.domain.usecase.dashboard.DashboardData,
    onAppClick: (packageName: String, appName: String) -> Unit,
) {
    val dateLabel = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Page header
        item {
            AnimatedListItem(0) {
                Column(modifier = Modifier.padding(bottom = 2.dp)) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Text(
                        text = dateLabel,
                        fontSize = 13.sp,
                        color = NoisemapColors.TextSecondary,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }

        // Hero — score arc + total count
        item { AnimatedListItem(1) { HeroCard(data) } }

        // Engagement breakdown
        item { AnimatedListItem(2) { EngagementRow(data) } }

        // Weekly trend
        item { AnimatedListItem(3) { WeeklyCard(data) } }

        // Heatmap
        item { AnimatedListItem(4) { HeatmapCard(data) } }

        // Top apps
        if (data.topApps.isNotEmpty()) {
            item { AnimatedListItem(5) { TopAppsCard(apps = data.topApps, onAppClick = onAppClick) } }
        }

        // Recent feed
        if (data.recentNotifications.isNotEmpty()) {
            item { AnimatedListItem(6) { RecentCard(notifications = data.recentNotifications) } }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ─── Hero card ──────────────────────────────────────────────────────────────
// Left: Focus Score arc + descriptor label
// Right: Total notifications + trend pill

@Composable
fun HeroCard(data: com.app.noisemap.core.domain.usecase.dashboard.DashboardData) {
    NoisemapCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // LEFT — score arc
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(118.dp),
            ) {
                FocusScoreArc(
                    score = data.focusScore,
                    modifier = Modifier.size(112.dp),
                    strokeWidth = 9.dp,
                )
                Spacer(Modifier.height(6.dp))
                val descriptor = when {
                    data.focusScore >= 70 -> "Great focus"
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = descriptorColor,
                    textAlign = TextAlign.Center,
                )
            }

            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(104.dp)
                    .background(NoisemapColors.BorderDefault),
            )

            // RIGHT — total + trend
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "NOTIFICATIONS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NoisemapColors.TextMuted,
                    letterSpacing = 0.8.sp,
                )
                AnimatedCounter(
                    target = data.todayTotal,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NoisemapColors.TextPrimary,
                    ),
                )
                Text(
                    text = "today",
                    fontSize = 12.sp,
                    color = NoisemapColors.TextSecondary,
                )
                Spacer(Modifier.height(8.dp))

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
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = trendText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = trendColor,
                    )
                }
            }
        }
    }
}

// ─── Engagement row ─────────────────────────────────────────────────────────
// Two equal cards: Tapped | Dismissed — with animated fill bar

@Composable
fun EngagementRow(data: com.app.noisemap.core.domain.usecase.dashboard.DashboardData) {
    val tapRate    = if (data.todayTotal > 0) data.todayTapped    * 100f / data.todayTotal else 0f
    val dismissRate = if (data.todayTotal > 0) data.todayDismissed * 100f / data.todayTotal else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EngagementCard(
            label = "Tapped",
            value = data.todayTapped,
            rate = tapRate,
            color = NoisemapColors.AccentGreen,
            modifier = Modifier.weight(1f),
        )
        EngagementCard(
            label = "Dismissed",
            value = data.todayDismissed,
            rate = dismissRate,
            color = NoisemapColors.AccentRed,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun EngagementCard(
    label: String,
    value: Int,
    rate: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val animatedFill by animateFloatAsState(
        targetValue = (rate / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "engagementFill_$label",
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(NoisemapColors.Surface)
            .border(1.dp, NoisemapColors.BorderDefault, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        // Label row with colour dot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = NoisemapColors.TextSecondary,
                letterSpacing = 0.3.sp,
            )
        }

        Spacer(Modifier.height(8.dp))

        // Big count
        AnimatedCounter(
            target = value,
            style = MaterialTheme.typography.headlineLarge.copy(
                color = color,
                fontWeight = FontWeight.ExtraBold,
            ),
        )

        Spacer(Modifier.height(8.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(NoisemapColors.SurfaceElevated),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFill)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "${rate.toInt()}% of total",
            fontSize = 10.sp,
            color = NoisemapColors.TextMuted,
        )
    }
}

// ─── Weekly card ────────────────────────────────────────────────────────────

@Composable
fun WeeklyCard(data: com.app.noisemap.core.domain.usecase.dashboard.DashboardData) {
    val avgPerDay = if (data.weeklyData.isNotEmpty())
        data.weeklyData.sumOf { it.count } / data.weeklyData.size
    else 0

    NoisemapCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle("This week")
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(NoisemapColors.SurfaceElevated)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "avg $avgPerDay / day",
                    fontSize = 10.sp,
                    color = NoisemapColors.TextSecondary,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        DashboardWeeklyBarChart(data = data.weeklyData)
    }
}

@Composable
fun DashboardWeeklyBarChart(data: List<DailyBarData>) {
    val maxCount = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

    Column {
        // Bar area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            data.forEach { day ->
                val barFraction = (day.count.toFloat() / maxCount).coerceAtLeast(0.04f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    // Count label floats above today's bar
                    if (day.isToday && day.count > 0) {
                        Text(
                            text = "${day.count}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NoisemapColors.AccentTeal,
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxHeight(barFraction)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (day.isToday) NoisemapColors.AccentTeal
                                else NoisemapColors.SurfaceElevated,
                            ),
                    )
                }
            }
        }

        // Day labels
        Spacer(Modifier.height(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            data.forEach { day ->
                Text(
                    text = day.dayLabel.take(1),
                    fontSize = 11.sp,
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (day.isToday) NoisemapColors.AccentTeal else NoisemapColors.TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ─── Heatmap card ───────────────────────────────────────────────────────────
// 2 rows × 12 cells  (row 0 = midnight–11am, row 1 = noon–11pm)

@Composable
fun HeatmapCard(data: com.app.noisemap.core.domain.usecase.dashboard.DashboardData) {
    val maxCount    = data.hourlyActivity.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val peakHour    = data.hourlyActivity.maxByOrNull { it.count }?.hour ?: -1
    val currentHour = LocalTime.now().hour

    NoisemapCard {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle("Hourly activity")
            if (peakHour >= 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NoisemapColors.TintTeal)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "Peak ${formatHour(peakHour)}",
                        fontSize = 10.sp,
                        color = NoisemapColors.AccentTeal,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // AM row (hours 0–11) and PM row (hours 12–23)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("AM" to (0..11), "PM" to (12..23)).forEach { (label, range) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    Text(
                        text = label,
                        fontSize = 8.sp,
                        color = NoisemapColors.TextMuted,
                        modifier = Modifier.width(22.dp),
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        range.forEach { hour ->
                            val count     = data.hourlyActivity.find { it.hour == hour }?.count ?: 0
                            val intensity = count.toFloat() / maxCount
                            val isCurrent = hour == currentHour

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when {
                                            isCurrent     -> NoisemapColors.AccentTeal.copy(
                                                alpha = (0.5f + intensity * 0.5f).coerceIn(0.4f, 1f)
                                            )
                                            intensity == 0f   -> NoisemapColors.SurfaceElevated
                                            intensity < 0.25f -> NoisemapColors.AccentTeal.copy(alpha = 0.20f)
                                            intensity < 0.50f -> NoisemapColors.AccentTeal.copy(alpha = 0.45f)
                                            intensity < 0.75f -> NoisemapColors.AccentTeal.copy(alpha = 0.70f)
                                            else              -> NoisemapColors.AccentTeal
                                        }
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isCurrent) PulsingDot(size = 4.dp)
                            }
                        }
                    }
                }
            }
        }

        // Time range hints
        Spacer(Modifier.height(5.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("12a", "3a", "6a", "9a", "12p", "3p", "6p", "9p", "11p").forEach {
                Text(it, fontSize = 8.sp, color = NoisemapColors.TextMuted)
            }
        }
    }
}

// ─── Top apps card ──────────────────────────────────────────────────────────

@Composable
fun TopAppsCard(
    apps: List<AppNotificationSummary>,
    onAppClick: (packageName: String, appName: String) -> Unit,
) {
    val maxTotal = apps.maxOfOrNull { it.totalCount }?.coerceAtLeast(1) ?: 1

    NoisemapCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle("Loudest Apps")
            Text(
                text = "${apps.size} apps",
                fontSize = 10.sp,
                color = NoisemapColors.TextMuted,
            )
        }

        Spacer(Modifier.height(10.dp))

        apps.forEachIndexed { index, app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAppClick(app.packageName, app.appName) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Rank
                Text(
                    text = "${index + 1}",
                    fontSize = 11.sp,
                    color = NoisemapColors.TextMuted,
                    modifier = Modifier.width(18.dp),
                    textAlign = TextAlign.Center,
                )

                // App icon
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(app.packageName)
                        .build(),
                    contentDescription = app.appName,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )

                // Name + count + progress bar
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = app.appName.ifEmpty { app.packageName },
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Text(
                            text = "${app.totalCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (app.isNoise) NoisemapColors.AccentRed else NoisemapColors.AccentTeal,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }

                    Spacer(Modifier.height(5.dp))

                    // Progress bar — full width beneath the name row
                    val barColor = if (app.isNoise) NoisemapColors.AccentRed else NoisemapColors.AccentTeal
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(NoisemapColors.SurfaceElevated),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(app.totalCount.toFloat() / maxTotal)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(1.dp))
                                .background(barColor),
                        )
                    }
                }

                VerdictBadge(if (app.isNoise) Verdict.NOISE else Verdict.SIGNAL)
            }

            if (index < apps.lastIndex) {
                HorizontalDivider(
                    color = NoisemapColors.BorderSubtle,
                    thickness = 0.5.dp,
                )
            }
        }
    }
}

// ─── Recent card ────────────────────────────────────────────────────────────

@Composable
fun RecentCard(notifications: List<Notification>) {
    NoisemapCard {
        SectionTitle("Recent")
        Spacer(Modifier.height(10.dp))
        notifications.forEachIndexed { index, notif ->
            val action = when {
                notif.removalReason == 1    -> NotificationAction.TAPPED
                notif.removalReason != null -> NotificationAction.DISMISSED
                else                        -> NotificationAction.ACTIVE
            }
            val indicatorColor = when (action) {
                NotificationAction.TAPPED    -> NoisemapColors.AccentGreen
                NotificationAction.DISMISSED -> NoisemapColors.AccentRed
                NotificationAction.ACTIVE    -> NoisemapColors.AccentTeal
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left accent bar
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(indicatorColor),
                )

                // App icon
                AsyncImage(
                    model = notif.packageName,
                    contentDescription = null,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )

                // Text content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notif.appName,
                        fontSize = 10.sp,
                        color = NoisemapColors.TextMuted,
                    )
                    Text(
                        text = notif.title ?: notif.body ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                    )
                }

                ActionTag(action, formatRelativeTime(notif.postedAt))
            }

            if (index < notifications.lastIndex) {
                HorizontalDivider(color = NoisemapColors.BorderSubtle, thickness = 0.5.dp)
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatHour(hour: Int): String = when {
    hour == 0  -> "12 AM"
    hour < 12  -> "$hour AM"
    hour == 12 -> "12 PM"
    else       -> "${hour - 12} PM"
}

private fun formatRelativeTime(timestampMs: Long): String {
    val diffMs  = System.currentTimeMillis() - timestampMs
    val minutes = diffMs / (1000 * 60)
    val hours   = minutes / 60
    return when {
        minutes < 1  -> "now"
        minutes < 60 -> "${minutes}m ago"
        hours   < 24 -> "${hours}h ago"
        else         -> "${hours / 24}d ago"
    }
}
