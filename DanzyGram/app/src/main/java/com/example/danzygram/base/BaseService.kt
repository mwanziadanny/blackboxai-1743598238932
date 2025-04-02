package com.example.danzygram.base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.danzygram.util.NetworkUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseService : LifecycleService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    protected fun launch(
        block: suspend CoroutineScope.() -> Unit
    ): Job = serviceScope.launch {
        try {
            block()
        } catch (e: Exception) {
            Timber.e(e)
            handleError(e)
        }
    }

    protected fun <T> Flow<T>.handleErrors(): Flow<T> {
        return this.catch { e ->
            Timber.e(e)
            handleError(e)
        }
    }

    protected fun <T> Flow<T>.flowWithScope(): Flow<T> {
        return this
            .catch { e ->
                Timber.e(e)
                handleError(e)
            }
            .flowOn(Dispatchers.IO)
    }

    protected open fun handleError(error: Throwable) {
        // Override in child classes if needed
    }

    protected fun isNetworkAvailable(): Boolean {
        return NetworkUtil.isNetworkAvailable(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        serviceScope.cancel()
        serviceJob.cancel()
        super.onDestroy()
    }

    protected fun startForegroundService(
        notificationId: Int,
        notification: android.app.Notification
    ) {
        startForeground(notificationId, notification)
    }

    protected fun stopForegroundService(removeNotification: Boolean = true) {
        stopForeground(removeNotification)
    }

    protected fun restartService() {
        val intent = Intent(applicationContext, this::class.java)
        stopSelf()
        startService(intent)
    }

    protected fun stopService() {
        stopSelf()
    }

    protected fun getLifecycleScope(): CoroutineScope {
        return lifecycleScope
    }

    protected fun getServiceScope(): CoroutineScope {
        return serviceScope
    }

    companion object {
        const val EXTRA_COMMAND = "extra_command"
        const val COMMAND_START = "command_start"
        const val COMMAND_STOP = "command_stop"
        const val COMMAND_RESTART = "command_restart"
    }
}