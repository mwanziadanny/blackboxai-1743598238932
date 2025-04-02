package com.example.danzygram.base

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.danzygram.util.NetworkUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class BaseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    protected abstract suspend fun doWork(dispatcher: CoroutineDispatcher): Result

    override suspend fun doWork(): Result {
        return try {
            doWork(Dispatchers.IO)
        } catch (e: Exception) {
            Timber.e(e)
            handleError(e)
        }
    }

    protected fun isNetworkAvailable(): Boolean {
        return NetworkUtil.isNetworkAvailable(applicationContext)
    }

    protected open fun handleError(error: Throwable): Result {
        return Result.failure(createFailureData(error))
    }

    protected fun createSuccessData(message: String? = null): Data {
        return Data.Builder()
            .putString(KEY_RESULT_MESSAGE, message)
            .build()
    }

    protected fun createFailureData(error: Throwable): Data {
        return Data.Builder()
            .putString(KEY_ERROR_MESSAGE, error.message)
            .build()
    }

    protected suspend fun <T> withDispatcher(
        dispatcher: CoroutineDispatcher,
        block: suspend () -> T
    ): T {
        return withContext(dispatcher) { block() }
    }

    protected suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        apiCall: suspend () -> T
    ): com.example.danzygram.data.Result<T> {
        return withContext(dispatcher) {
            try {
                if (!isNetworkAvailable()) {
                    return@withContext com.example.danzygram.data.Result.Error(Exception("No internet connection"))
                }
                com.example.danzygram.data.Result.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                com.example.danzygram.data.Result.Error(Exception(throwable))
            }
        }
    }

    protected suspend fun <T> safeCacheCall(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        cacheCall: suspend () -> T
    ): com.example.danzygram.data.Result<T> {
        return withContext(dispatcher) {
            try {
                com.example.danzygram.data.Result.Success(cacheCall.invoke())
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                com.example.danzygram.data.Result.Error(Exception(throwable))
            }
        }
    }

    companion object {
        const val KEY_RESULT_MESSAGE = "key_result_message"
        const val KEY_ERROR_MESSAGE = "key_error_message"
        const val DEFAULT_BACKOFF_DELAY_MILLIS = 30_000L // 30 seconds
        const val MAX_ATTEMPTS = 3

        fun getBackoffDelay(attempt: Int): Long {
            return DEFAULT_BACKOFF_DELAY_MILLIS * (attempt + 1)
        }

        fun hasReachedMaxAttempts(attempt: Int): Boolean {
            return attempt >= MAX_ATTEMPTS
        }
    }
}