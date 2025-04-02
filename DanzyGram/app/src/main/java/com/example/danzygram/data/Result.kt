package com.example.danzygram.data

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(exception: Exception): Result<Nothing> = Error(exception)
        fun loading(): Result<Nothing> = Loading

        fun <T> Result<T>.successOr(fallback: T): T {
            return (this as? Success<T>)?.data ?: fallback
        }
    }

    val isSuccess get() = this is Success<T>
    val isError get() = this is Error
    val isLoading get() = this is Loading

    fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(exception)
            is Loading -> Loading
        }
    }

    suspend fun <R> suspendMap(transform: suspend (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(exception)
            is Loading -> Loading
        }
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
        is Loading -> null
    }

    fun getOrThrow(): T {
        return when (this) {
            is Success -> data
            is Error -> throw exception
            is Loading -> throw IllegalStateException("Result is Loading")
        }
    }

    fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    fun onError(action: (Exception) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }

    fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) action()
        return this
    }
}

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()

    fun <R> map(transform: (T) -> R): UiState<R> = when (this) {
        is Loading -> Loading
        is Success -> Success(transform(data))
        is Error -> Error(message)
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
}

inline fun <T> Result<T>.fold(
    onSuccess: (T) -> Unit,
    onError: (Exception) -> Unit,
    onLoading: () -> Unit = {}
) {
    when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Error -> onError(exception)
        is Result.Loading -> onLoading()
    }
}

fun <T> Result<T>.toUiState(): UiState<T> = when (this) {
    is Result.Loading -> UiState.Loading
    is Result.Success -> UiState.Success(data)
    is Result.Error -> UiState.Error(exception.message ?: "Unknown error")
}

fun <T> UiState<T>.toResult(): Result<T> = when (this) {
    is UiState.Loading -> Result.Loading
    is UiState.Success -> Result.Success(data)
    is UiState.Error -> Result.Error(Exception(message))
}