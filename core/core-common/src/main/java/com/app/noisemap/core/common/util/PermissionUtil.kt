package com.app.noisemap.core.common.util

import android.content.Context
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object PermissionUtil {
    fun isNotificationListenerPermissionGranted(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }
}
