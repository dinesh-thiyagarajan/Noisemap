package com.app.noisemap.core.domain.usecase.dashboard

import com.app.noisemap.core.domain.base.Result
import com.app.noisemap.core.domain.base.UseCase
import com.app.noisemap.core.domain.model.AppNotificationSummary
import com.app.noisemap.core.domain.repository.NotificationRepository
import javax.inject.Inject

class GetTopNoisyAppsUseCase @Inject constructor(
    private val repository: NotificationRepository
) : UseCase<GetTopNoisyAppsUseCase.Params, List<AppNotificationSummary>>() {

    data class Params(val limit: Int, val since: Long)

    override suspend fun execute(params: Params): Result<List<AppNotificationSummary>> {
        val stats = repository.getTopNoisyApps(params.limit, params.since)
        return Result.Success(stats)
    }
}
