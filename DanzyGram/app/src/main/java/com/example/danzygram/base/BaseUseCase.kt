package com.example.danzygram.base

import com.example.danzygram.data.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class BaseUseCase<in P, R> {

    protected abstract suspend fun execute(parameters: P): Result<R>

    suspend operator fun invoke(
        parameters: P,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Result<R> {
        return withContext(dispatcher) {
            try {
                execute(parameters)
            } catch (e: Exception) {
                Timber.e(e)
                Result.Error(e)
            }
        }
    }
}

abstract class BaseFlowUseCase<in P, R> {

    protected abstract fun execute(parameters: P): Flow<Result<R>>

    operator fun invoke(
        parameters: P,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Flow<Result<R>> {
        return execute(parameters)
            .catch { e ->
                Timber.e(e)
                emit(Result.Error(Exception(e)))
            }
            .flowOn(dispatcher)
    }
}

abstract class BaseUseCaseWithoutParameters<R> {

    protected abstract suspend fun execute(): Result<R>

    suspend operator fun invoke(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Result<R> {
        return withContext(dispatcher) {
            try {
                execute()
            } catch (e: Exception) {
                Timber.e(e)
                Result.Error(e)
            }
        }
    }
}

abstract class BaseFlowUseCaseWithoutParameters<R> {

    protected abstract fun execute(): Flow<Result<R>>

    operator fun invoke(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Flow<Result<R>> {
        return execute()
            .catch { e ->
                Timber.e(e)
                emit(Result.Error(Exception(e)))
            }
            .flowOn(dispatcher)
    }
}

abstract class BaseUseCaseWithoutResult<in P> {

    protected abstract suspend fun execute(parameters: P)

    suspend operator fun invoke(
        parameters: P,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {
        withContext(dispatcher) {
            try {
                execute(parameters)
            } catch (e: Exception) {
                Timber.e(e)
                throw e
            }
        }
    }
}

abstract class BaseFlowUseCaseWithoutResult<in P> {

    protected abstract fun execute(parameters: P): Flow<Unit>

    operator fun invoke(
        parameters: P,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Flow<Unit> {
        return execute(parameters)
            .catch { e ->
                Timber.e(e)
                throw e
            }
            .flowOn(dispatcher)
    }
}

abstract class BaseUseCaseWithoutParametersAndResult {

    protected abstract suspend fun execute()

    suspend operator fun invoke(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {
        withContext(dispatcher) {
            try {
                execute()
            } catch (e: Exception) {
                Timber.e(e)
                throw e
            }
        }
    }
}

abstract class BaseFlowUseCaseWithoutParametersAndResult {

    protected abstract fun execute(): Flow<Unit>

    operator fun invoke(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Flow<Unit> {
        return execute()
            .catch { e ->
                Timber.e(e)
                throw e
            }
            .flowOn(dispatcher)
    }
}