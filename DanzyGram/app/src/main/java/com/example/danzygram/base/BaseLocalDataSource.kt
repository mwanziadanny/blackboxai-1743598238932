package com.example.danzygram.base

import com.example.danzygram.data.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class BaseLocalDataSource {

    protected suspend fun <T> safeDbCall(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        dbCall: suspend () -> T
    ): Result<T> {
        return withContext(dispatcher) {
            try {
                Result.Success(dbCall.invoke())
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                Result.Error(handleError(throwable))
            }
        }
    }

    protected fun <T> safeDbFlow(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        dbCall: suspend () -> T
    ): Flow<Result<T>> = flow {
        emit(Result.Loading)
        emit(Result.Success(dbCall.invoke()))
    }.catch { e ->
        Timber.e(e)
        emit(Result.Error(handleError(e)))
    }.flowOn(dispatcher)

    protected fun handleError(throwable: Throwable): Exception {
        return when (throwable) {
            is android.database.sqlite.SQLiteException -> DatabaseException(throwable)
            else -> UnknownException(throwable)
        }
    }

    protected fun <T> handleEmptyResult(data: T?): Result<T> {
        return data?.let {
            Result.Success(it)
        } ?: Result.Error(NotFoundException())
    }

    protected fun <T> handleEmptyListResult(data: List<T>?): Result<List<T>> {
        return data?.let {
            if (it.isEmpty()) {
                Result.Error(EmptyResultException())
            } else {
                Result.Success(it)
            }
        } ?: Result.Error(NotFoundException())
    }

    class DatabaseException(throwable: Throwable) : Exception(throwable.message)
    class UnknownException(throwable: Throwable) : Exception(throwable.message)
    class NotFoundException : Exception("Data not found")
    class EmptyResultException : Exception("No data available")

    companion object {
        private const val ERROR_DATABASE = "Database error occurred"
        private const val ERROR_UNKNOWN = "Unknown error occurred"
        private const val ERROR_NOT_FOUND = "Data not found"
        private const val ERROR_EMPTY = "No data available"
    }

    protected suspend fun <T> withTransaction(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend () -> T
    ): Result<T> {
        return try {
            withContext(dispatcher) {
                Result.Success(block())
            }
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(handleError(e))
        }
    }

    protected fun <T> observeData(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        dataProvider: () -> Flow<T>
    ): Flow<Result<T>> = flow {
        emit(Result.Loading)
        dataProvider().collect { data ->
            emit(handleEmptyResult(data))
        }
    }.catch { e ->
        Timber.e(e)
        emit(Result.Error(handleError(e)))
    }.flowOn(dispatcher)

    protected fun <T> observeList(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        dataProvider: () -> Flow<List<T>>
    ): Flow<Result<List<T>>> = flow {
        emit(Result.Loading)
        dataProvider().collect { data ->
            emit(handleEmptyListResult(data))
        }
    }.catch { e ->
        Timber.e(e)
        emit(Result.Error(handleError(e)))
    }.flowOn(dispatcher)

    protected suspend fun clearData(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend () -> Unit
    ): Result<Unit> {
        return withContext(dispatcher) {
            try {
                block()
                Result.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e)
                Result.Error(handleError(e))
            }
        }
    }
}