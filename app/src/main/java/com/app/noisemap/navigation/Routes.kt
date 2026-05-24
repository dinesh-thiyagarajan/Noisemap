package com.app.noisemap.navigation

import kotlinx.serialization.Serializable

@Serializable object OnboardingRoute
@Serializable object DashboardRoute
@Serializable object TimelineRoute
@Serializable object InsightsRoute
@Serializable object AboutRoute
@Serializable data class AppDetailRoute(val packageName: String)
