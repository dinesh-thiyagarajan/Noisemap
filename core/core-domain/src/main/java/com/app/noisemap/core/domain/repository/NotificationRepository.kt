package com.app.noisemap.core.domain.repository

import com.app.noisemap.core.domain.model.AppNotificationSummary
import com.app.noisemap.core.domain.model.DailyBarData
import com.app.noisemap.core.domain.model.HourlyActivity
import com.app.noisemap.core.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotificationsStream(): Flow<List<Notification>>
    fun getNotificationsForPackage(packageName: String): Flow<List<Notification>>
    fun getNotificationsInRange(startMs: Long, endMs: Long): Flow<List<Notification>>
    suspend fun insert(notification: Notification)
    suspend fun markRemoved(key: String, removedAt: Long, reason: Int)
    suspend fun getStats(): List<AppNotificationSummary>
    suspend fun getStatsForPackage(packageName: String): AppNotificationSummary?
    suspend fun getHourlyDistribution(packageName: String?): List<HourlyActivity>
    suspend fun getTopNoisyApps(limit: Int, since: Long): List<AppNotificationSummary>
    suspend fun getWeeklyStats(sinceMs: Long): List<DailyBarData>
    suspend fun getRecentNotifications(limit: Int): List<Notification>
    suspend fun deleteOlderThan(cutoffMs: Long)
    fun searchNotifications(query: String): Flow<List<Notification>>
}
