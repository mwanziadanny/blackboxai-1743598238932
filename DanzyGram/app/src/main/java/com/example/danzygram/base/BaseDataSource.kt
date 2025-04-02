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

abstract class BaseDataSource {

    protected suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        apiCall: suspend () -> T
    ): Result<T> {
        return withContext(dispatcher) {
            try {
                if (!isNetworkAvailable()) {
                    return@withContext Result.Error(Exception("No internet connection"))
                }
                Result.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                Result.Error(handleError(throwable))
            }
        }
    }

    protected fun <T> safeFlow(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend () -> T
    ): Flow<Result<T>> {
        return kotlinx.coroutines.flow.flow {
            if (!isNetworkAvailable()) {
                emit(Result.Error(Exception("No internet connection")))
                return@flow
            }
            emit(Result.Loading)
            emit(Result.Success(block()))
        }.catch { e ->
            Timber.e(e)
            emit(Result.Error(handleError(e)))
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
                Result.Error(handleCacheError(throwable))
            }
        }
    }

    protected fun <T> safeCacheFlow(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend () -> T
    ): Flow<Result<T>> {
        return kotlinx.coroutines.flow.flow {
            emit(Result.Loading)
            emit(Result.Success(block()))
        }.catch { e ->
            Timber.e(e)
            emit(Result.Error(handleCacheError(e)))
        }.flowOn(dispatcher)
    }

    private fun isNetworkAvailable(): Boolean {
        return NetworkUtil.isNetworkAvailable(DanzyGramApplication.instance)
    }

    private fun handleError(throwable: Throwable): Exception {
        return when (throwable) {
            is java.net.UnknownHostException -> Exception("No internet connection")
            is java.net.SocketTimeoutException -> Exception("Connection timed out")
            is java.io.IOException -> Exception("Network error")
            is retrofit2.HttpException -> {
                when (throwable.code()) {
                    401 -> Exception("Unauthorized")
                    403 -> Exception("Forbidden")
                    404 -> Exception("Not found")
                    500 -> Exception("Server error")
                    else -> Exception("Unknown error")
                }
            }
            else -> Exception(throwable.message ?: "Unknown error")
        }
    }

    private fun handleCacheError(throwable: Throwable): Exception {
        return when (throwable) {
            is java.io.IOException -> Exception("Cache error")
            else -> Exception(throwable.message ?: "Unknown cache error")
        }
    }

    protected fun handleDatabaseError(throwable: Throwable): Exception {
        return when (throwable) {
            is android.database.sqlite.SQLiteException -> Exception("Database error")
            else -> Exception(throwable.message ?: "Unknown database error")
        }
    }

    protected fun handleFirebaseError(throwable: Throwable): Exception {
        return when (throwable) {
            is com.google.firebase.FirebaseException -> Exception("Firebase error: ${throwable.message}")
            else -> Exception(throwable.message ?: "Unknown Firebase error")
        }
    }

    protected fun handleStorageError(throwable: Throwable): Exception {
        return when (throwable) {
            is java.io.IOException -> Exception("Storage error")
            else -> Exception(throwable.message ?: "Unknown storage error")
        }
    }

    protected fun handleAuthError(throwable: Throwable): Exception {
        return when (throwable) {
            is com.google.firebase.auth.FirebaseAuthException -> Exception("Authentication error: ${throwable.message}")
            else -> Exception(throwable.message ?: "Unknown authentication error")
        }
    }
}