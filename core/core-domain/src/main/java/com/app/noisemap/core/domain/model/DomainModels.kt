package com.app.noisemap.core.domain.model

import java.time.LocalDate

data class AppNotificationSummary(
    val packageName: String,
    val appName: String,
    val appIconUri: String?,
    val totalCount: Int,
    val tappedCount: Int,
    val dismissedCount: Int,
    val tapRate: Float,              // tappedCount / totalCount
    val avgTimeOnScreenMs: Long,
    val lastNotifiedAt: Long,
    val isNoise: Boolean,            // tapRate < 0.1 && totalCount > 10
)

data class HourlyActivity(
    val hour: Int,                   // 0..23
    val count: Int,
    val packageBreakdown: Map<String, Int> = emptyMap(),
)

data class DailyInsight(
    val date: LocalDate,
    val totalInterruptions: Int,
    val mostNoisyApp: AppNotificationSummary?,
    val mostSignalApp: AppNotificationSummary?,
    val peakHour: Int,
    val focusScore: Int,             // 0..100, inversely correlated with interruptions
    val changeVsYesterday: Float,    // percentage change
)

data class WeeklySummary(
    val weekStart: LocalDate,
    val totalInterruptions: Int,
    val dailyBreakdown: List<DailyInsight>,
    val topNoiseApps: List<AppNotificationSummary>,
    val topSignalApps: List<AppNotificationSummary>,
    val focusTrend: List<Int>,       // focusScore per day
)

data class DailyBarData(
    val dayLabel: String,
    val count: Int,
    val isToday: Boolean,
)

data class Notification(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val body: String?,
    val postedAt: Long,
    val removedAt: Long?,
    val removalReason: Int?,
    val category: String?,
    val channelId: String?,
    val isOngoing: Boolean,
    val isGroupSummary: Boolean,
    val groupKey: String?,
)
