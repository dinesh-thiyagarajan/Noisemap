package com.app.noisemap.core.domain.base

import kotlinx.coroutines.flow.Flow

abstract class UseCase<in P, out R> {
    suspend operator fun invoke(params: P): Result<R> {
        return try {
            execute(params)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected abstract suspend fun execute(params: P): Result<R>
}

abstract class FlowUseCase<in P, out R> {
    operator fun invoke(params: P): Flow<Result<R>> {
        return execute(params)
    }

    protected abstract fun execute(params: P): Flow<Result<R>>
}
