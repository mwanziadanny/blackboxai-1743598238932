package com.example.danzygram.base

import com.example.danzygram.data.Result
import com.example.danzygram.util.NetworkUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class BaseRepository {

    protected suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        apiCall: suspend () -> T
    ): Result<T> {
        return withContext(dispatcher) {
            try {
                Result.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                Result.Error(Exception(throwable))
            }
        }
    }

    protected fun <T> safeFlow(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend () -> T
    ): Flow<Result<T>> {
        return kotlinx.coroutines.flow.flow {
            emit(Result.Loading)
            emit(Result.Success(block()))
        }.catch { e ->
            Timber.e(e)
            emit(Result.Error(Exception(e)))
        }.flowOn(dispatcher)
    }

    protected suspend fun <T> safeCacheCall(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        cacheCall: suspend () -> T
    ): Result<T> {
        return withContext(dispatcher) {
            try {
                Result.Success(cacheCall.invoke())
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                Result.Error(Exception(throwable))
            }
        }
    }

    protected suspend fun <T> networkBoundResource(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        shouldFetch: (T?) -> Boolean = { true },
        query: suspend () -> T,
        fetch: suspend () -> Unit,
        saveFetchResult: suspend (T) -> Unit,
        onFetchFailed: (Throwable) -> Unit = { }
    ): Flow<Result<T>> = kotlinx.coroutines.flow.flow {
        emit(Result.Loading)

        val data = query()

        val flow = if (shouldFetch(data)) {
            emit(Result.Loading)

            try {
                fetch()
                saveFetchResult(query())
                kotlinx.coroutines.flow.flow { emit(Result.Success(query())) }
            } catch (throwable: Throwable) {
                onFetchFailed(throwable)
                kotlinx.coroutines.flow.flow { emit(Result.Error(Exception(throwable))) }
            }
        } else {
            kotlinx.coroutines.flow.flow { emit(Result.Success(data)) }
        }

        flow.collect { result ->
            emit(result)
        }
    }.flowOn(dispatcher)

    protected suspend fun <T> networkResource(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        networkCall: suspend () -> Result<T>
    ): Flow<Result<T>> = kotlinx.coroutines.flow.flow {
        emit(Result.Loading)
        emit(networkCall())
    }.flowOn(dispatcher)

    protected suspend fun <T> cacheResource(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        cacheCall: suspend () -> Result<T>
    ): Flow<Result<T>> = kotlinx.coroutines.flow.flow {
        emit(Result.Loading)
        emit(cacheCall())
    }.flowOn(dispatcher)

    protected fun isNetworkAvailable(): Boolean {
        return NetworkUtil.isNetworkAvailable(DanzyGramApplication.instance)
    }

    protected fun handleApiError(code: Int): Exception {
        return when (code) {
            401 -> Exception("Unauthorized")
            403 -> Exception("Forbidden")
            404 -> Exception("Not Found")
            500 -> Exception("Server Error")
            else -> Exception("Unknown Error")
        }
    }

    protected fun handleNetworkError(throwable: Throwable): Exception {
        return when (throwable) {
            is java.net.UnknownHostException -> Exception("No Internet Connection")
            is java.net.SocketTimeoutException -> Exception("Connection Timed Out")
            is java.io.IOException -> Exception("Network Error")
            else -> Exception(throwable.message ?: "Unknown Error")
        }
    }

    protected fun handleDatabaseError(throwable: Throwable): Exception {
        return Exception("Database Error: ${throwable.message}")
    }

    protected fun handleCacheError(throwable: Throwable): Exception {
        return Exception("Cache Error: ${throwable.message}")
    }
}