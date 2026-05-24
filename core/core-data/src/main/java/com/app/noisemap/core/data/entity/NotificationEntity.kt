package com.app.noisemap.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val key: String,           // Unique key from StatusBarNotification
    val packageName: String,
    val appName: String,
    val title: String?,
    val body: String?,
    val postedAt: Long,                    // System.currentTimeMillis() on post
    val removedAt: Long?,                  // null = still active
    val removalReason: Int?,               // REASON_CANCEL, REASON_CLICK, etc.
    val category: String?,                 // Notification.CATEGORY_*
    val channelId: String?,
    val isOngoing: Boolean,
    val isGroupSummary: Boolean,
    val groupKey: String?,
)
