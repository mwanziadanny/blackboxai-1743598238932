package com.example.danzygram.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

object CoroutineUtil {
    private val defaultExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Coroutine exception")
    }

    val applicationScope = CoroutineScope(
        SupervisorJob() + 
        Dispatchers.Main + 
        defaultExceptionHandler
    )

    fun launchMain(
        exceptionHandler: CoroutineExceptionHandler = defaultExceptionHandler,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return applicationScope.launch(Dispatchers.Main + exceptionHandler) {
            block()
        }
    }

    fun launchIO(
        exceptionHandler: CoroutineExceptionHandler = defaultExceptionHandler,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return applicationScope.launch(Dispatchers.IO + exceptionHandler) {
            block()
        }
    }

    fun launchDefault(
        exceptionHandler: CoroutineExceptionHandler = defaultExceptionHandler,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return applicationScope.launch(Dispatchers.Default + exceptionHandler) {
            block()
        }
    }

    suspend fun <T> withMainContext(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.Main) {
            block()
        }
    }

    suspend fun <T> withIOContext(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.IO) {
            block()
        }
    }

    suspend fun <T> withDefaultContext(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.Default) {
            block()
        }
    }

    fun <T> Flow<T>.flowWithLifecycle(
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        onStart: () -> Unit = {},
        onError: (Throwable) -> Unit = { Timber.e(it) },
        onComplete: () -> Unit = {}
    ): Flow<T> {
        return this
            .onStart { onStart() }
            .catch { onError(it) }
            .onCompletion { onComplete() }
            .flowOn(dispatcher)
    }

    fun createExceptionHandler(
        tag: String,
        message: String = "Coroutine exception",
        onError: ((Throwable) -> Unit)? = null
    ): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            Timber.tag(tag).e(throwable, message)
            onError?.invoke(throwable)
        }
    }

    suspend fun <T> retryIO(
        times: Int = 3,
        initialDelay: Long = 100,
        maxDelay: Long = 1000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(times - 1) {
            try {
                return withIOContext { block() }
            } catch (e: Exception) {
                Timber.e(e, "Retry attempt ${it + 1} failed")
            }
            kotlinx.coroutines.delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
        return withIOContext { block() } // last attempt
    }

    fun CoroutineScope.launchWithCatch(
        tag: String,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return launch(createExceptionHandler(tag)) {
            block()
        }
    }

    suspend fun <T> measureTimeMillis(
        tag: String,
        block: suspend () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        val result = block()
        val endTime = System.currentTimeMillis()
        Timber.tag(tag).d("Operation took ${endTime - startTime}ms")
        return result
    }

    fun cancelChildren(scope: CoroutineScope) {
        scope.coroutineContext[Job]?.cancelChildren()
    }

    fun cancelAll(scope: CoroutineScope) {
        scope.coroutineContext[Job]?.cancel()
    }

    fun isActive(scope: CoroutineScope): Boolean {
        return scope.coroutineContext[Job]?.isActive == true
    }

    fun isCancelled(scope: CoroutineScope): Boolean {
        return scope.coroutineContext[Job]?.isCancelled == true
    }

    fun isCompleted(scope: CoroutineScope): Boolean {
        return scope.coroutineContext[Job]?.isCompleted == true
    }
}