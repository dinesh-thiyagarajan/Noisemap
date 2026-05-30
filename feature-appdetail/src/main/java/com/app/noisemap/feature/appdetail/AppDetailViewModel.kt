package com.app.noisemap.feature.appdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.noisemap.core.domain.analytics.AnalyticsEvent
import com.app.noisemap.core.domain.analytics.AnalyticsTracker
import com.app.noisemap.core.domain.analytics.CrashReporter
import com.app.noisemap.core.domain.model.AppNotificationSummary
import com.app.noisemap.core.domain.model.HourlyActivity
import com.app.noisemap.core.domain.model.Notification
import com.app.noisemap.core.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppDetailUiState(
    val isLoading: Boolean = true,
    val packageName: String = "",
    val summary: AppNotificationSummary? = null,
    val hourlyActivity: List<HourlyActivity> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val packageName: String = savedStateHandle["packageName"] ?: ""

    private val _uiState = MutableStateFlow(AppDetailUiState(packageName = packageName))
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    init {
        analyticsTracker.logEvent(AnalyticsEvent.ScreenAppDetail(packageName))
        if (packageName.isNotBlank()) loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val summary = repository.getStatsForPackage(packageName)
                val hourly = repository.getHourlyDistribution(packageName)

                repository.getNotificationsForPackage(packageName).collect { notifications ->
                    if (summary != null) {
                        analyticsTracker.logEvent(
                            AnalyticsEvent.AppDetailLoaded(packageName, summary.totalCount),
                        )
                    }
                    _uiState.value = AppDetailUiState(
                        isLoading = false,
                        packageName = packageName,
                        summary = summary,
                        hourlyActivity = hourly,
                        notifications = notifications,
                    )
                }
            } catch (e: Exception) {
                crashReporter.recordException(e)
                analyticsTracker.logEvent(AnalyticsEvent.ErrorOccurred("app_detail", e.message ?: "Unknown"))
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
