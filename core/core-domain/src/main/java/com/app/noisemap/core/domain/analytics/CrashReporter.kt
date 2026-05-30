package com.app.noisemap.core.domain.analytics

/**
 * Domain-layer abstraction for crash reporting.
 * Feature modules depend on this interface — never on Crashlytics directly.
 */
interface CrashReporter {
    fun recordException(throwable: Throwable)
    fun log(message: String)
    fun setCustomKey(key: String, value: String)
}
