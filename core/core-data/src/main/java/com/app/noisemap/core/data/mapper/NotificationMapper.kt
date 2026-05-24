package com.app.noisemap.core.data.mapper

import com.app.noisemap.core.data.entity.NotificationEntity
import com.app.noisemap.core.data.model.AppNotificationStats
import com.app.noisemap.core.data.model.DailyCount
import com.app.noisemap.core.data.model.HourlyCount
import com.app.noisemap.core.domain.model.AppNotificationSummary
import com.app.noisemap.core.domain.model.DailyBarData
import com.app.noisemap.core.domain.model.HourlyActivity
import com.app.noisemap.core.domain.model.Notification
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun NotificationEntity.toDomain() = Notification(
    key = key,
    packageName = packageName,
    appName = appName,
    title = title,
    body = body,
    postedAt = postedAt,
    removedAt = removedAt,
    removalReason = removalReason,
    category = category,
    channelId = channelId,
    isOngoing = isOngoing,
    isGroupSummary = isGroupSummary,
    groupKey = groupKey,
)

fun Notification.toEntity() = NotificationEntity(
    key = key,
    packageName = packageName,
    appName = appName,
    title = title,
    body = body,
    postedAt = postedAt,
    removedAt = removedAt,
    removalReason = removalReason,
    category = category,
    channelId = channelId,
    isOngoing = isOngoing,
    isGroupSummary = isGroupSummary,
    groupKey = groupKey,
)

fun AppNotificationStats.toDomain() = AppNotificationSummary(
    packageName = packageName,
    appName = appName,
    appIconUri = null,
    totalCount = totalCount,
    tappedCount = tappedCount,
    dismissedCount = dismissedCount,
    tapRate = if (totalCount > 0) tappedCount.toFloat() / totalCount else 0f,
    avgTimeOnScreenMs = avgTimeOnScreenMs,
    lastNotifiedAt = lastNotifiedAt,
    isNoise = totalCount > 10 && (tappedCount.toFloat() / totalCount) < 0.1f,
)

fun HourlyCount.toDomain() = HourlyActivity(hour = hour, count = count)

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

fun DailyCount.toDomain(): DailyBarData {
    val date = runCatching { LocalDate.parse(dateStr, dateFormatter) }.getOrNull()
    val today = LocalDate.now()
    val label = when {
        date == null -> dateStr.takeLast(5)
        date == today -> "Today"
        else -> dayNames[date.dayOfWeek.value % 7]
    }
    return DailyBarData(
        dayLabel = label,
        count = count,
        isToday = date == today,
    )
}
