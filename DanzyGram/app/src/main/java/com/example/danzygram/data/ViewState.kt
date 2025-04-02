package com.example.danzygram.data

sealed class ViewState<out T> {
    object Initial : ViewState<Nothing>()
    object Loading : ViewState<Nothing>()
    data class Success<T>(val data: T) : ViewState<T>()
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val retry: (() -> Unit)? = null
    ) : ViewState<Nothing>()
    data class Empty(
        val message: String,
        val action: (() -> Unit)? = null,
        val actionLabel: String? = null
    ) : ViewState<Nothing>()

    val isInitial get() = this is Initial
    val isLoading get() = this is Loading
    val isSuccess get() = this is Success
    val isError get() = this is Error
    val isEmpty get() = this is Empty

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun <R> map(transform: (T) -> R): ViewState<R> = when (this) {
        is Initial -> Initial
        is Loading -> Loading
        is Success -> Success(transform(data))
        is Error -> Error(message, cause, retry)
        is Empty -> Empty(message, action, actionLabel)
    }

    suspend fun <R> suspendMap(transform: suspend (T) -> R): ViewState<R> = when (this) {
        is Initial -> Initial
        is Loading -> Loading
        is Success -> Success(transform(data))
        is Error -> Error(message, cause, retry)
        is Empty -> Empty(message, action, actionLabel)
    }

    inline fun onSuccess(action: (T) -> Unit): ViewState<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (String, Throwable?) -> Unit): ViewState<T> {
        if (this is Error) action(message, cause)
        return this
    }

    inline fun onEmpty(action: (String) -> Unit): ViewState<T> {
        if (this is Empty) action(message)
        return this
    }

    inline fun onLoading(action: () -> Unit): ViewState<T> {
        if (this is Loading) action()
        return this
    }

    inline fun onInitial(action: () -> Unit): ViewState<T> {
        if (this is Initial) action()
        return this
    }
}

fun <T> Result<T>.toViewState(
    emptyMessage: String = "No data available",
    errorMessage: String = "An error occurred"
): ViewState<T> = when (this) {
    is Result.Loading -> ViewState.Loading
    is Result.Success -> {
        when {
            data is Collection<*> && data.isEmpty() -> ViewState.Empty(emptyMessage)
            data is Map<*, *> && data.isEmpty() -> ViewState.Empty(emptyMessage)
            data == null -> ViewState.Empty(emptyMessage)
            else -> ViewState.Success(data)
        }
    }
    is Result.Error -> ViewState.Error(
        message = exception.message ?: errorMessage,
        cause = exception
    )
}

fun <T> UiState<T>.toViewState(
    emptyMessage: String = "No data available",
    errorMessage: String = "An error occurred"
): ViewState<T> = when (this) {
    is UiState.Loading -> ViewState.Loading
    is UiState.Success -> {
        when {
            data is Collection<*> && data.isEmpty() -> ViewState.Empty(emptyMessage)
            data is Map<*, *> && data.isEmpty() -> ViewState.Empty(emptyMessage)
            data == null -> ViewState.Empty(emptyMessage)
            else -> ViewState.Success(data)
        }
    }
    is UiState.Error -> ViewState.Error(message = message)
}

fun <T> ViewState<T>.toUiState(): UiState<T> = when (this) {
    is ViewState.Initial -> UiState.Loading
    is ViewState.Loading -> UiState.Loading
    is ViewState.Success -> UiState.Success(data)
    is ViewState.Error -> UiState.Error(message)
    is ViewState.Empty -> UiState.Success(null as T)
}

fun <T> ViewState<T>.toResult(): Result<T> = when (this) {
    is ViewState.Initial -> Result.Loading
    is ViewState.Loading -> Result.Loading
    is ViewState.Success -> Result.Success(data)
    is ViewState.Error -> Result.Error(cause ?: Exception(message))
    is ViewState.Empty -> Result.Success(null as T)
}