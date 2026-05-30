package com.app.noisemap.core.domain.analytics

/**
 * Exhaustive sealed hierarchy of every analytics event the app can emit.
 * Adding a new event is a single change here — the compiler enforces it everywhere.
 */
sealed class AnalyticsEvent {

    // ── Screens ──────────────────────────────────────────────────────────────
    data object ScreenDashboard : AnalyticsEvent()
    data object ScreenTimeline : AnalyticsEvent()
    data object ScreenInsights : AnalyticsEvent()
    data object ScreenAbout : AnalyticsEvent()
    data class ScreenAppDetail(val packageName: String) : AnalyticsEvent()
    data object ScreenOnboarding : AnalyticsEvent()

    // ── Dashboard ─────────────────────────────────────────────────────────────
    data object DashboardRefreshed : AnalyticsEvent()
    data class AppCardTapped(val packageName: String, val appName: String) : AnalyticsEvent()

    // ── Timeline ──────────────────────────────────────────────────────────────
    data class TimelineFilterChanged(val filter: String) : AnalyticsEvent()
    /** queryLength instead of raw query so we never log PII. */
    data class TimelineSearched(val queryLength: Int) : AnalyticsEvent()
    data object TimelineSearchCleared : AnalyticsEvent()

    // ── Insights ──────────────────────────────────────────────────────────────
    data object InsightsRefreshed : AnalyticsEvent()
    data class InsightCardViewed(val headline: String) : AnalyticsEvent()

    // ── App Detail ────────────────────────────────────────────────────────────
    data class AppDetailLoaded(val packageName: String, val totalCount: Int) : AnalyticsEvent()

    // ── Onboarding ────────────────────────────────────────────────────────────
    data object OnboardingPermissionGranted : AnalyticsEvent()
    data object OnboardingPermissionPending : AnalyticsEvent()

    // ── Notification Service ──────────────────────────────────────────────────
    data class NotificationReceived(val packageName: String) : AnalyticsEvent()
    data class NotificationRemoved(val packageName: String, val reason: Int) : AnalyticsEvent()

    // ── Errors ────────────────────────────────────────────────────────────────
    data class ErrorOccurred(val screen: String, val message: String) : AnalyticsEvent()
}
