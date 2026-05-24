package com.app.noisemap.core.data.model

data class AppNotificationStats(
    val packageName: String,
    val appName: String,
    val totalCount: Int,
    val tappedCount: Int,
    val dismissedCount: Int,
    val avgTimeOnScreenMs: Long,
    val lastNotifiedAt: Long,
)

data class HourlyCount(
    val hour: Int,
    val count: Int,
)

data class DailyCount(
    val dateStr: String,
    val count: Int,
)
