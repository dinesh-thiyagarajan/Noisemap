package com.app.noisemap.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.noisemap.core.domain.analytics.AnalyticsEvent
import com.app.noisemap.core.domain.analytics.AnalyticsTracker
import com.app.noisemap.core.domain.analytics.CrashReporter
import com.app.noisemap.core.domain.base.Result
import com.app.noisemap.core.domain.usecase.dashboard.DashboardData
import com.app.noisemap.core.domain.usecase.dashboard.GetDashboardDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val data: DashboardData? = null,
    val error: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        analyticsTracker.logEvent(AnalyticsEvent.ScreenDashboard)
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = getDashboardDataUseCase(Unit)) {
                is Result.Success -> {
                    analyticsTracker.logEvent(AnalyticsEvent.DashboardRefreshed)
                    _uiState.value = DashboardUiState(
                        isLoading = false,
                        data = result.data,
                    )
                }
                is Result.Error -> {
                    val msg = result.exception.message ?: "Unknown error"
                    crashReporter.recordException(result.exception)
                    analyticsTracker.logEvent(AnalyticsEvent.ErrorOccurred("dashboard", msg))
                    _uiState.value = DashboardUiState(isLoading = false, error = msg)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onAppCardTapped(packageName: String, appName: String) {
        analyticsTracker.logEvent(AnalyticsEvent.AppCardTapped(packageName, appName))
    }
}
