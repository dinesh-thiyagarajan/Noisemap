package com.app.noisemap.core.domain.usecase.dashboard

import com.app.noisemap.core.domain.base.Result
import com.app.noisemap.core.domain.base.UseCase
import com.app.noisemap.core.domain.model.AppNotificationSummary
import com.app.noisemap.core.domain.model.DailyBarData
import com.app.noisemap.core.domain.model.HourlyActivity
import com.app.noisemap.core.domain.model.Notification
import com.app.noisemap.core.domain.repository.NotificationRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class DashboardData(
    val focusScore: Int,
    val focusScoreTrend: Float,
    val todayTotal: Int,
    val todayTapped: Int,
    val todayDismissed: Int,
    val topApps: List<AppNotificationSummary>,
    val hourlyActivity: List<HourlyActivity>,
    val weeklyData: List<DailyBarData>,
    val recentNotifications: List<Notification>,
)

class GetDashboardDataUseCase @Inject constructor(
    private val repository: NotificationRepository,
) : UseCase<Unit, DashboardData>() {

    override suspend fun execute(params: Unit): Result<DashboardData> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val zone = ZoneId.systemDefault()

        val startOfToday = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val startOfYesterday = yesterday.atStartOfDay(zone).toInstant().toEpochMilli()
        val sevenDaysAgo = today.minusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()

        val stats = repository.getStats()
        val todayStats = stats.filter { it.lastNotifiedAt >= startOfToday }
        val yesterdayStats = stats.filter {
            it.lastNotifiedAt in startOfYesterday until startOfToday
        }

        val todayTotal = todayStats.sumOf { it.totalCount }
        val todayTapped = todayStats.sumOf { it.tappedCount }
        val todayDismissed = todayStats.sumOf { it.dismissedCount }
        val yesterdayTotal = yesterdayStats.sumOf { it.totalCount }

        val focusScore = computeFocusScore(todayTotal, todayTapped)
        val yesterdayScore = computeFocusScore(yesterdayTotal, yesterdayStats.sumOf { it.tappedCount })
        val trend = if (yesterdayScore > 0) {
            ((focusScore - yesterdayScore).toFloat() / yesterdayScore) * 100
        } else 0f

        val topApps = repository.getTopNoisyApps(5, startOfToday)
        val hourly = repository.getHourlyDistribution(null)
        val weekly = repository.getWeeklyStats(sevenDaysAgo)
        val recent = repository.getRecentNotifications(5)

        return Result.Success(
            DashboardData(
                focusScore = focusScore,
                focusScoreTrend = trend,
                todayTotal = todayTotal,
                todayTapped = todayTapped,
                todayDismissed = todayDismissed,
                topApps = topApps,
                hourlyActivity = hourly,
                weeklyData = weekly,
                recentNotifications = recent,
            ),
        )
    }

    private fun computeFocusScore(total: Int, tapped: Int): Int {
        val penalty = (total / 5).coerceAtMost(50)
        val bonus = if (total > 0) (tapped.toFloat() / total * 20).toInt() else 0
        return (100 - penalty + bonus).coerceIn(0, 100)
    }
}
