package com.app.noisemap.core.domain.model

import java.time.LocalDate
import java.time.LocalTime

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

// ============================================================================
// Focus Score & Trending
// ============================================================================
data class FocusScoreData(
    val score: Int,              // 0..100
    val trend: Float,            // percentage change vs yesterday
    val description: String,
)

// ============================================================================
// Insight Cards
// ============================================================================
data class InsightCard(
    val type: InsightType,
    val headline: String,
    val body: String,
    val highlightedValue: String,   // the bold coloured number/percentage in body
    val sentiment: InsightSentiment,
    val iconResId: Int? = null,
)

enum class InsightType {
    NOISE_APP,      // "WhatsApp sent you 47 notifications"
    SIGNAL_APP,     // "You tapped 90% of Calendar notifications"
    PEAK_HOUR,      // "You're most interrupted at 3pm"
    SCORE_CHANGE,   // "Your focus score improved 23% this week"
    STREAK,         // "3-day no-interruption streak"
}

enum class InsightSentiment {
    POSITIVE,       // Green tint
    NEGATIVE,       // Red tint
    WARNING,        // Amber tint
    NEUTRAL,        // Teal tint
}

// ============================================================================
// UI-specific Models
// ============================================================================
enum class NotificationAction {
    TAPPED,
    DISMISSED,
    ACTIVE,
}

enum class Verdict {
    SIGNAL,
    NOISE,
    MIXED,
}

data class TimelineParams(
    val searchQuery: String = "",
    val filter: TimelineFilter = TimelineFilter.ALL,
)

enum class TimelineFilter {
    ALL,
    TAPPED,
    DISMISSED,
    ACTIVE,
}

data class AppDetailParams(
    val packageName: String,
)

data class AppHistoryParams(
    val packageName: String,
    val pageSize: Int = 20,
)

// ============================================================================
// Dashboard Data (aggregated from use cases)
// ============================================================================
data class DashboardData(
    val focusScore: Int,
    val focusScoreTrend: Float,
    val todayTotal: Int,
    val todayTapped: Int,
    val todayDismissed: Int,
    val weeklyData: List<DailyBarData>,
    val hourlyActivity: List<HourlyActivity>,
    val topApps: List<AppNotificationSummary>,
    val recentNotifications: List<Notification>,
)

// ============================================================================
// Summary Models for Screens
// ============================================================================
data class TodaySummary(
    val total: Int,
    val tapped: Int,
    val dismissed: Int,
)

data class AppDetail(
    val summary: AppNotificationSummary,
    val hourlyActivity: List<HourlyActivity>,
    val notifications: List<Notification>,
    val verdict: Verdict,
    val verdictHeadline: String,
    val verdictSuggestion: String,
)
