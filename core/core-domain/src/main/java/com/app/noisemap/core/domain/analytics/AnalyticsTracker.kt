package com.app.noisemap.core.domain.analytics

/**
 * Domain-layer abstraction for analytics.
 * Feature modules depend on this interface — never on Firebase directly.
 */
interface AnalyticsTracker {
    fun logEvent(event: AnalyticsEvent)
    fun setUserProperty(name: String, value: String)
}
