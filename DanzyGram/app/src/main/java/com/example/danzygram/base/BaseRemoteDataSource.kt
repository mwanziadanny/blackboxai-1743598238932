package com.example.danzygram.base

import com.example.danzygram.data.Result
import com.example.danzygram.util.NetworkUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

abstract class BaseRemoteDataSource {

    protected suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        apiCall: suspend () -> T
    ): Result<T> {
        return withContext(dispatcher) {
            try {
                if (!isNetworkAvailable()) {
                    return@withContext Result.Error(NoInternetException())
                }
                Result.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                Result.Error(handleError(throwable))
            }
        }
    }

    protected fun <T> safeApiFlow(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        apiCall: suspend () -> T
    ): Flow<Result<T>> = flow {
        if (!isNetworkAvailable()) {
            emit(Result.Error(NoInternetException()))
            return@flow
        }
        emit(Result.Loading)
        emit(Result.Success(apiCall.invoke()))
    }.catch { e ->
        Timber.e(e)
        emit(Result.Error(handleError(e)))
    }.flowOn(dispatcher)

    protected fun isNetworkAvailable(): Boolean {
        return NetworkUtil.isNetworkAvailable(BaseApplication.getApplicationContext())
    }

    protected fun handleError(throwable: Throwable): Exception {
        return when (throwable) {
            is IOException -> NetworkException(throwable)
            is HttpException -> {
                when (throwable.code()) {
                    401 -> UnauthorizedException()
                    403 -> ForbiddenException()
                    404 -> NotFoundException()
                    500 -> ServerException()
                    else -> ApiException(throwable)
                }
            }
            else -> UnknownException(throwable)
        }
    }

    protected fun handleErrorResponse(response: BaseResponse): Exception {
        return when {
            !response.isSuccess() -> ApiException(response.getErrorMessage())
            else -> UnknownException("Unknown error occurred")
        }
    }

    class NetworkException(throwable: Throwable) : Exception(throwable.message)
    class ApiException : Exception {
        constructor(throwable: Throwable) : super(throwable.message)
        constructor(message: String) : super(message)
    }
    class UnauthorizedException : Exception("Unauthorized")
    class ForbiddenException : Exception("Forbidden")
    class NotFoundException : Exception("Not found")
    class ServerException : Exception("Server error")
    class UnknownException(throwable: Throwable) : Exception(throwable.message)
    class NoInternetException : Exception("No internet connection")

    companion object {
        private const val ERROR_NETWORK = "Network error occurred"
        private const val ERROR_API = "API error occurred"
        private const val ERROR_UNKNOWN = "Unknown error occurred"
        private const val ERROR_NO_INTERNET = "No internet connection"
        private const val ERROR_UNAUTHORIZED = "Unauthorized"
        private const val ERROR_FORBIDDEN = "Forbidden"
        private const val ERROR_NOT_FOUND = "Not found"
        private const val ERROR_SERVER = "Server error"
    }

    protected fun <T> handleApiResponse(response: BaseResponse, data: T?): Result<T> {
        return if (response.isSuccess() && data != null) {
            Result.Success(data)
        } else {
            Result.Error(handleErrorResponse(response))
        }
    }

    protected fun <T> handlePagedApiResponse(response: PagedResponse<T>): Result<List<T>> {
        return if (response.isSuccess() && !response.data.isNullOrEmpty()) {
            Result.Success(response.data)
        } else {
            Result.Error(handleErrorResponse(response))
        }
    }

    protected fun handleEmptyResponse(response: BaseResponse): Result<Unit> {
        return if (response.isSuccess()) {
            Result.Success(Unit)
        } else {
            Result.Error(handleErrorResponse(response))
        }
    }

    protected fun handleTokenResponse(response: TokenResponse): Result<TokenResponse> {
        return if (response.isSuccess() && response.isValid()) {
            Result.Success(response)
        } else {
            Result.Error(handleErrorResponse(response))
        }
    }
}