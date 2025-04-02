package com.example.danzygram.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danzygram.data.Result
import com.example.danzygram.data.ViewState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseViewModel : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

    protected fun <T> launchWithState(
        state: MutableStateFlow<ViewState<T>>,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend () -> T
    ): Job {
        return viewModelScope.launch {
            state.value = ViewState.Loading
            try {
                val result = withDispatcher(dispatcher) { block() }
                state.value = ViewState.Success(result)
            } catch (e: Exception) {
                Timber.e(e)
                state.value = ViewState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    protected fun <T> launchWithResult(
        result: MutableStateFlow<Result<T>>,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend () -> T
    ): Job {
        return viewModelScope.launch {
            result.value = Result.Loading
            try {
                val data = withDispatcher(dispatcher) { block() }
                result.value = Result.Success(data)
            } catch (e: Exception) {
                Timber.e(e)
                result.value = Result.Error(e)
            }
        }
    }

    protected fun launch(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend () -> Unit
    ): Job {
        return viewModelScope.launch {
            try {
                withDispatcher(dispatcher) { block() }
            } catch (e: Exception) {
                Timber.e(e)
                _error.emit(e.message ?: "Unknown error occurred")
            }
        }
    }

    protected fun <T> Flow<T>.handleErrors(): Flow<T> {
        return this.catch { e ->
            Timber.e(e)
            _error.emit(e.message ?: "Unknown error occurred")
        }
    }

    protected fun <T> Flow<T>.handleLoading(): Flow<T> {
        return this
            .onStart { _loading.value = true }
            .catch { e ->
                _loading.value = false
                throw e
            }
            .flowOn(Dispatchers.IO)
    }

    private suspend fun <T> withDispatcher(
        dispatcher: CoroutineDispatcher,
        block: suspend () -> T
    ): T {
        return kotlinx.coroutines.withContext(dispatcher) { block() }
    }

    protected fun showLoading() {
        _loading.value = true
    }

    protected fun hideLoading() {
        _loading.value = false
    }

    protected suspend fun emitError(message: String) {
        _error.emit(message)
    }

    protected fun <T> MutableStateFlow<ViewState<T>>.setLoading() {
        value = ViewState.Loading
    }

    protected fun <T> MutableStateFlow<ViewState<T>>.setSuccess(data: T) {
        value = ViewState.Success(data)
    }

    protected fun <T> MutableStateFlow<ViewState<T>>.setError(message: String) {
        value = ViewState.Error(message)
    }

    protected fun <T> MutableStateFlow<ViewState<T>>.setEmpty(message: String = "No data available") {
        value = ViewState.Empty(message)
    }

    protected fun <T> MutableStateFlow<Result<T>>.setLoading() {
        value = Result.Loading
    }

    protected fun <T> MutableStateFlow<Result<T>>.setSuccess(data: T) {
        value = Result.Success(data)
    }

    protected fun <T> MutableStateFlow<Result<T>>.setError(error: Exception) {
        value = Result.Error(error)
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
    }
}