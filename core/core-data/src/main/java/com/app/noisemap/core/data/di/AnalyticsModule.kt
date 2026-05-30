package com.app.noisemap.core.data.di

import com.app.noisemap.core.data.analytics.FirebaseAnalyticsTracker
import com.app.noisemap.core.data.analytics.FirebaseCrashlyticsTracker
import com.app.noisemap.core.domain.analytics.AnalyticsTracker
import com.app.noisemap.core.domain.analytics.CrashReporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(
        impl: FirebaseAnalyticsTracker,
    ): AnalyticsTracker

    @Binds
    @Singleton
    abstract fun bindCrashReporter(
        impl: FirebaseCrashlyticsTracker,
    ): CrashReporter
}
