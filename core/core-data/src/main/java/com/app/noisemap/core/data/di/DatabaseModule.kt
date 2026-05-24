package com.app.noisemap.core.data.di

import android.content.Context
import androidx.room.Room
import com.app.noisemap.core.data.dao.NotificationDao
import com.app.noisemap.core.data.database.NoisemapDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NoisemapDatabase {
        return Room.databaseBuilder(
            context,
            NoisemapDatabase::class.java,
            "noisemap.db"
        ).build()
    }

    @Provides
    fun provideNotificationDao(database: NoisemapDatabase): NotificationDao {
        return database.notificationDao()
    }
}
