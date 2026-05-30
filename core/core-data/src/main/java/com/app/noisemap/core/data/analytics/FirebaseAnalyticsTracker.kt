@file:SuppressLint("MissingPermission")

package com.app.noisemap.core.data.analytics

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import com.app.noisemap.core.domain.analytics.AnalyticsEvent
import com.app.noisemap.core.domain.analytics.AnalyticsTracker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsTracker @Inject constructor(
    @ApplicationContext private val context: Context,
) : AnalyticsTracker {

    // Permissions (INTERNET, ACCESS_NETWORK_STATE, WAKE_LOCK) are declared in Firebase's own
    // AAR manifest and merged into the final app manifest at build time.
    private val firebase: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    override fun logEvent(event: AnalyticsEvent) {
        val (name, params) = event.toFirebaseParams()
        firebase.logEvent(name, params)
    }

    override fun setUserProperty(name: String, value: String) {
        firebase.setUserProperty(name, value)
    }

    // ── Mapping sealed class → Firebase event name + params ──────────────────

    private fun AnalyticsEvent.toFirebaseParams(): Pair<String, Bundle> {
        val bundle = Bundle()
        val name = when (this) {
            // Screens
            is AnalyticsEvent.ScreenDashboard -> FirebaseAnalytics.Event.SCREEN_VIEW.also {
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "dashboard")
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "DashboardScreen")
            }
            is AnalyticsEvent.ScreenTimeline -> FirebaseAnalytics.Event.SCREEN_VIEW.also {
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "timeline")
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "TimelineScreen")
            }
            is AnalyticsEvent.ScreenInsights -> FirebaseAnalytics.Event.SCREEN_VIEW.also {
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "insights")
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "InsightsScreen")
            }
            is AnalyticsEvent.ScreenAbout -> FirebaseAnalytics.Event.SCREEN_VIEW.also {
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "about")
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "AboutScreen")
            }
            is AnalyticsEvent.ScreenAppDetail -> FirebaseAnalytics.Event.SCREEN_VIEW.also {
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "app_detail")
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "AppDetailScreen")
                bundle.putString("package_name", packageName)
            }
            is AnalyticsEvent.ScreenOnboarding -> FirebaseAnalytics.Event.SCREEN_VIEW.also {
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "onboarding")
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "OnboardingScreen")
            }

            // Dashboard
            is AnalyticsEvent.DashboardRefreshed -> "dashboard_refreshed"
            is AnalyticsEvent.AppCardTapped -> "app_card_tapped".also {
                bundle.putString("package_name", packageName)
                bundle.putString("app_name", appName)
            }

            // Timeline
            is AnalyticsEvent.TimelineFilterChanged -> "timeline_filter_changed".also {
                bundle.putString("filter", filter)
            }
            is AnalyticsEvent.TimelineSearched -> "timeline_searched".also {
                bundle.putInt("query_length", queryLength)
            }
            is AnalyticsEvent.TimelineSearchCleared -> "timeline_search_cleared"

            // Insights
            is AnalyticsEvent.InsightsRefreshed -> "insights_refreshed"
            is AnalyticsEvent.InsightCardViewed -> "insight_card_viewed".also {
                bundle.putString("headline", headline)
            }

            // App Detail
            is AnalyticsEvent.AppDetailLoaded -> "app_detail_loaded".also {
                bundle.putString("package_name", packageName)
                bundle.putInt("total_count", totalCount)
            }

            // Onboarding
            is AnalyticsEvent.OnboardingPermissionGranted -> "onboarding_permission_granted"
            is AnalyticsEvent.OnboardingPermissionPending -> "onboarding_permission_pending"

            // Notification Service
            is AnalyticsEvent.NotificationReceived -> "notification_received".also {
                bundle.putString("package_name", packageName)
            }
            is AnalyticsEvent.NotificationRemoved -> "notification_removed".also {
                bundle.putString("package_name", packageName)
                bundle.putInt("reason", reason)
            }

            // Errors
            is AnalyticsEvent.ErrorOccurred -> "error_occurred".also {
                bundle.putString("screen", screen)
                bundle.putString("message", message)
            }
        }
        return name to bundle
    }
}
