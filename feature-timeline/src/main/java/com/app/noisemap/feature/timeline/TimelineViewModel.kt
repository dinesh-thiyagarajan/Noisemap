package com.app.noisemap.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.noisemap.core.domain.analytics.AnalyticsEvent
import com.app.noisemap.core.domain.analytics.AnalyticsTracker
import com.app.noisemap.core.domain.analytics.CrashReporter
import com.app.noisemap.core.domain.model.Notification
import com.app.noisemap.core.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

enum class TimelineFilter { ALL, TAPPED, DISMISSED, ACTIVE }

data class TimelineUiState(
    val isLoading: Boolean = true,
    val notifications: List<Notification> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: TimelineFilter = TimelineFilter.ALL,
    val error: String? = null,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(TimelineFilter.ALL)

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init {
        analyticsTracker.logEvent(AnalyticsEvent.ScreenTimeline)
        observeNotifications()
    }

    private fun observeNotifications() {
        _searchQuery
            .debounce(300)
            .flatMapLatest { query ->
                if (query.isBlank()) repository.getNotificationsStream()
                else repository.searchNotifications(query)
            }
            .combine(_filter) { notifications, filter ->
                when (filter) {
                    TimelineFilter.ALL -> notifications
                    TimelineFilter.TAPPED -> notifications.filter { it.removalReason == 1 }
                    TimelineFilter.DISMISSED -> notifications.filter {
                        it.removalReason != null && it.removalReason != 1
                    }
                    TimelineFilter.ACTIVE -> notifications.filter { it.removalReason == null }
                }
            }
            .onEach { filtered ->
                _uiState.value = _uiState.value.copy(isLoading = false, notifications = filtered)
            }
            .catch { e ->
                crashReporter.recordException(e)
                analyticsTracker.logEvent(AnalyticsEvent.ErrorOccurred("timeline", e.message ?: "Unknown"))
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)

        if (query.isNotBlank()) {
            analyticsTracker.logEvent(AnalyticsEvent.TimelineSearched(query.length))
        } else {
            analyticsTracker.logEvent(AnalyticsEvent.TimelineSearchCleared)
        }
    }

    fun onFilterChange(filter: TimelineFilter) {
        _filter.value = filter
        _uiState.value = _uiState.value.copy(activeFilter = filter)
        analyticsTracker.logEvent(AnalyticsEvent.TimelineFilterChanged(filter.name))
    }
}
