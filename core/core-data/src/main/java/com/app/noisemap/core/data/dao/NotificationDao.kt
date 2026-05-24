package com.app.noisemap.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.noisemap.core.data.entity.NotificationEntity
import com.app.noisemap.core.data.model.AppNotificationStats
import com.app.noisemap.core.data.model.DailyCount
import com.app.noisemap.core.data.model.HourlyCount
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY postedAt DESC")
    fun getNotificationsStream(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE packageName = :packageName ORDER BY postedAt DESC")
    fun getNotificationsForPackage(packageName: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE postedAt BETWEEN :startMs AND :endMs ORDER BY postedAt DESC")
    fun getNotificationsInRange(startMs: Long, endMs: Long): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' ORDER BY postedAt DESC")
    fun searchNotifications(query: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY postedAt DESC LIMIT :limit")
    suspend fun getRecentNotifications(limit: Int): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("UPDATE notifications SET removedAt = :removedAt, removalReason = :reason WHERE `key` = :key")
    suspend fun updateRemoval(key: String, removedAt: Long, reason: Int)

    @Query("""
        SELECT
            packageName,
            MAX(appName) as appName,
            COUNT(*) as totalCount,
            SUM(CASE WHEN removalReason = 1 THEN 1 ELSE 0 END) as tappedCount,
            SUM(CASE WHEN removalReason != 1 AND removalReason IS NOT NULL THEN 1 ELSE 0 END) as dismissedCount,
            AVG(CASE WHEN removedAt IS NOT NULL THEN removedAt - postedAt ELSE 0 END) as avgTimeOnScreenMs,
            MAX(postedAt) as lastNotifiedAt
        FROM notifications
        GROUP BY packageName
    """)
    suspend fun getStats(): List<AppNotificationStats>

    @Query("""
        SELECT
            packageName,
            MAX(appName) as appName,
            COUNT(*) as totalCount,
            SUM(CASE WHEN removalReason = 1 THEN 1 ELSE 0 END) as tappedCount,
            SUM(CASE WHEN removalReason != 1 AND removalReason IS NOT NULL THEN 1 ELSE 0 END) as dismissedCount,
            AVG(CASE WHEN removedAt IS NOT NULL THEN removedAt - postedAt ELSE 0 END) as avgTimeOnScreenMs,
            MAX(postedAt) as lastNotifiedAt
        FROM notifications
        WHERE packageName = :packageName
        GROUP BY packageName
    """)
    suspend fun getStatsForPackage(packageName: String): AppNotificationStats?

    @Query("""
        SELECT
            CAST(strftime('%H', postedAt / 1000, 'unixepoch', 'localtime') AS INTEGER) as hour,
            COUNT(*) as count
        FROM notifications
        WHERE (:packageName IS NULL OR packageName = :packageName)
        GROUP BY hour
        ORDER BY hour ASC
    """)
    suspend fun getHourlyDistribution(packageName: String?): List<HourlyCount>

    @Query("""
        SELECT
            packageName,
            MAX(appName) as appName,
            COUNT(*) as totalCount,
            SUM(CASE WHEN removalReason = 1 THEN 1 ELSE 0 END) as tappedCount,
            SUM(CASE WHEN removalReason != 1 AND removalReason IS NOT NULL THEN 1 ELSE 0 END) as dismissedCount,
            AVG(CASE WHEN removedAt IS NOT NULL THEN removedAt - postedAt ELSE 0 END) as avgTimeOnScreenMs,
            MAX(postedAt) as lastNotifiedAt
        FROM notifications
        WHERE postedAt >= :since
        GROUP BY packageName
        ORDER BY totalCount DESC
        LIMIT :limit
    """)
    suspend fun getTopNoisyApps(limit: Int, since: Long): List<AppNotificationStats>

    @Query("""
        SELECT
            date(postedAt / 1000, 'unixepoch', 'localtime') as dateStr,
            COUNT(*) as count
        FROM notifications
        WHERE postedAt >= :sinceMs
        GROUP BY dateStr
        ORDER BY dateStr ASC
    """)
    suspend fun getWeeklyStats(sinceMs: Long): List<DailyCount>

    @Query("DELETE FROM notifications WHERE postedAt < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long)
}
