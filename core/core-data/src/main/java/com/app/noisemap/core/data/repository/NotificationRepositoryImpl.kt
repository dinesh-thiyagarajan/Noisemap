package com.app.noisemap.core.data.repository

import com.app.noisemap.core.data.dao.NotificationDao
import com.app.noisemap.core.data.mapper.toDomain
import com.app.noisemap.core.data.mapper.toEntity
import com.app.noisemap.core.domain.model.AppNotificationSummary
import com.app.noisemap.core.domain.model.DailyBarData
import com.app.noisemap.core.domain.model.HourlyActivity
import com.app.noisemap.core.domain.model.Notification
import com.app.noisemap.core.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao,
) : NotificationRepository {

    override fun getNotificationsStream(): Flow<List<Notification>> =
        dao.getNotificationsStream().map { it.map { e -> e.toDomain() } }

    override fun getNotificationsForPackage(packageName: String): Flow<List<Notification>> =
        dao.getNotificationsForPackage(packageName).map { it.map { e -> e.toDomain() } }

    override fun getNotificationsInRange(startMs: Long, endMs: Long): Flow<List<Notification>> =
        dao.getNotificationsInRange(startMs, endMs).map { it.map { e -> e.toDomain() } }

    override fun searchNotifications(query: String): Flow<List<Notification>> =
        dao.searchNotifications(query).map { it.map { e -> e.toDomain() } }

    override suspend fun insert(notification: Notification) =
        dao.insert(notification.toEntity())

    override suspend fun markRemoved(key: String, removedAt: Long, reason: Int) =
        dao.updateRemoval(key, removedAt, reason)

    override suspend fun getStats(): List<AppNotificationSummary> =
        dao.getStats().map { it.toDomain() }

    override suspend fun getStatsForPackage(packageName: String): AppNotificationSummary? =
        dao.getStatsForPackage(packageName)?.toDomain()

    override suspend fun getHourlyDistribution(packageName: String?): List<HourlyActivity> =
        dao.getHourlyDistribution(packageName).map { it.toDomain() }

    override suspend fun getTopNoisyApps(limit: Int, since: Long): List<AppNotificationSummary> =
        dao.getTopNoisyApps(limit, since).map { it.toDomain() }

    override suspend fun getWeeklyStats(sinceMs: Long): List<DailyBarData> =
        dao.getWeeklyStats(sinceMs).map { it.toDomain() }

    override suspend fun getRecentNotifications(limit: Int): List<Notification> =
        dao.getRecentNotifications(limit).map { it.toDomain() }

    override suspend fun deleteOlderThan(cutoffMs: Long) =
        dao.deleteOlderThan(cutoffMs)
}
