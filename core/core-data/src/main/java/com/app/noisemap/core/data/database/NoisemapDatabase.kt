package com.app.noisemap.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.noisemap.core.data.dao.NotificationDao
import com.app.noisemap.core.data.entity.NotificationEntity

@Database(entities = [NotificationEntity::class], version = 1, exportSchema = false)
abstract class NoisemapDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}
