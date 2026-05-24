package com.app.noisemap.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.noisemap.core.domain.model.AppNotificationSummary
import com.app.noisemap.core.domain.model.DailyBarData
import com.app.noisemap.core.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class InsightCard(
    val emoji: String,
    val headline: String,
    val detail: String,
    val type: InsightCardType,
)

enum class InsightCardType { POSITIVE, NEGATIVE, NEUTRAL, TIP }

data class InsightsUiState(
    val isLoading: Boolean = true,
    val weeklyBars: List<DailyBarData> = emptyList(),
    val insightCards: List<InsightCard> = emptyList(),
    val topNoiseApps: List<AppNotificationSummary> = emptyList(),
    val topSignalApps: List<AppNotificationSummary> = emptyList(),
    val totalThisWeek: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    fun loadInsights() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val zone = ZoneId.systemDefault()
                val sevenDaysAgo = LocalDate.now().minusDays(7)
                    .atStartOfDay(zone).toInstant().toEpochMilli()

                val weekly = repository.getWeeklyStats(sevenDaysAgo)
                val allApps = repository.getTopNoisyApps(50, sevenDaysAgo)
                val noiseApps = allApps.filter { it.isNoise }.take(5)
                val signalApps = allApps.filter { !it.isNoise && it.tapRate > 0.5f }.take(5)
                val total = weekly.sumOf { it.count }
                val peakDay = weekly.maxByOrNull { it.count }
                val peakHour = repository.getHourlyDistribution(null).maxByOrNull { it.count }

                val cards = buildInsightCards(
                    total = total,
                    noiseApps = noiseApps,
                    signalApps = signalApps,
                    peakDay = peakDay,
                    peakHour = peakHour?.hour,
                    allApps = allApps,
                )

                _uiState.value = InsightsUiState(
                    isLoading = false,
                    weeklyBars = weekly,
                    insightCards = cards,
                    topNoiseApps = noiseApps,
                    topSignalApps = signalApps,
                    totalThisWeek = total,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun buildInsightCards(
        total: Int,
        noiseApps: List<AppNotificationSummary>,
        signalApps: List<AppNotificationSummary>,
        peakDay: DailyBarData?,
        peakHour: Int?,
        allApps: List<AppNotificationSummary>,
    ): List<InsightCard> {
        val cards = mutableListOf<InsightCard>()

        if (total > 0) {
            cards += InsightCard(
                emoji = if (total > 100) "🔴" else "🟢",
                headline = "$total interruptions this week",
                detail = if (total > 100) "That's a lot. Consider silencing some apps."
                else "Nice! You're managing your notifications well.",
                type = if (total > 100) InsightCardType.NEGATIVE else InsightCardType.POSITIVE,
            )
        }

        noiseApps.firstOrNull()?.let { app ->
            val tapPct = (app.tapRate * 100).toInt()
            cards += InsightCard(
                emoji = "🔴",
                headline = "${app.appName} interrupted you ${app.totalCount} times",
                detail = "You tapped only $tapPct% of them. Consider turning off badge notifications.",
                type = InsightCardType.NEGATIVE,
            )
        }

        signalApps.firstOrNull()?.let { app ->
            val tapPct = (app.tapRate * 100).toInt()
            cards += InsightCard(
                emoji = "🟢",
                headline = "${app.appName} has a $tapPct% tap rate",
                detail = "High signal app — these notifications earn their interruptions.",
                type = InsightCardType.POSITIVE,
            )
        }

        peakHour?.let { hour ->
            val ampm = if (hour < 12) "${hour}am" else "${if (hour == 12) 12 else hour - 12}pm"
            cards += InsightCard(
                emoji = "🕐",
                headline = "Peak interruption hour: $ampm",
                detail = "Try enabling Focus mode during this window.",
                type = InsightCardType.TIP,
            )
        }

        peakDay?.let { day ->
            cards += InsightCard(
                emoji = "📅",
                headline = "${day.dayLabel} was your busiest day",
                detail = "${day.count} notifications in a single day.",
                type = InsightCardType.NEUTRAL,
            )
        }

        val avgTapRate = if (allApps.isNotEmpty()) {
            allApps.map { it.tapRate }.average().toFloat()
        } else 0f
        if (avgTapRate > 0) {
            val pct = (avgTapRate * 100).toInt()
            cards += InsightCard(
                emoji = "💡",
                headline = "You tap $pct% of all notifications",
                detail = if (pct < 20) "Most notifications go untouched. Audit your app permissions."
                else "Good engagement rate — your apps are earning their spot.",
                type = if (pct < 20) InsightCardType.TIP else InsightCardType.POSITIVE,
            )
        }

        return cards
    }
}
