package com.app.noisemap.service.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.app.noisemap.core.domain.analytics.AnalyticsEvent
import com.app.noisemap.core.domain.analytics.AnalyticsTracker
import com.app.noisemap.core.domain.analytics.CrashReporter
import com.app.noisemap.core.domain.repository.NotificationRepository
import com.app.noisemap.core.domain.model.Notification as DomainNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NoisemapListenerService : NotificationListenerService() {

    @Inject lateinit var notificationRepository: NotificationRepository
    @Inject lateinit var analyticsTracker: AnalyticsTracker
    @Inject lateinit var crashReporter: CrashReporter

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (shouldIgnore(sbn)) return

        val entity = DomainNotification(
            key = sbn.key,
            packageName = sbn.packageName,
            appName = resolveAppName(sbn.packageName),
            title = sbn.notification.extras.getString(Notification.EXTRA_TITLE),
            body = sbn.notification.extras.getString(Notification.EXTRA_TEXT),
            postedAt = sbn.postTime,
            removedAt = null,
            removalReason = null,
            category = sbn.notification.category,
            channelId = sbn.notification.channelId,
            isOngoing = sbn.isOngoing,
            isGroupSummary = sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0,
            groupKey = sbn.groupKey,
        )

        serviceScope.launch {
            try {
                notificationRepository.insert(entity)
                analyticsTracker.logEvent(AnalyticsEvent.NotificationReceived(sbn.packageName))
            } catch (e: Exception) {
                crashReporter.recordException(e)
            }
        }
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification,
        rankingMap: RankingMap,
        reason: Int,
    ) {
        serviceScope.launch {
            try {
                notificationRepository.markRemoved(
                    key = sbn.key,
                    removedAt = System.currentTimeMillis(),
                    reason = reason,
                )
                analyticsTracker.logEvent(AnalyticsEvent.NotificationRemoved(sbn.packageName, reason))
            } catch (e: Exception) {
                crashReporter.recordException(e)
            }
        }
    }

    private fun shouldIgnore(sbn: StatusBarNotification): Boolean =
        sbn.packageName == "android" ||
            sbn.notification.flags and Notification.FLAG_ONGOING_EVENT != 0 ||
            sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0

    private fun resolveAppName(packageName: String): String =
        runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0),
            ).toString()
        }.getOrDefault(packageName)

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
