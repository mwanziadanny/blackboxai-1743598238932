package com.example.danzygram.base

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.danzygram.util.LogUtil
import com.example.danzygram.util.NetworkUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseApplication : Application(), LifecycleObserver {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isInForeground = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeApp()
    }

    private fun initializeApp() {
        setupTimber()
        setupLifecycleObserver()
        setupNetworkObserver()
        initializeComponents()
    }

    private fun setupTimber() {
        LogUtil.init()
    }

    private fun setupLifecycleObserver() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private fun setupNetworkObserver() {
        applicationScope.launch {
            NetworkUtil.observeNetworkState(this@BaseApplication).collect { isConnected ->
                onNetworkStateChanged(isConnected)
            }
        }
    }

    protected abstract fun initializeComponents()

    protected open fun onNetworkStateChanged(isConnected: Boolean) {
        if (isConnected) {
            Timber.d("Network is available")
        } else {
            Timber.d("Network is not available")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    open fun onAppForegrounded() {
        isInForeground = true
        Timber.d("App in foreground")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    open fun onAppBackgrounded() {
        isInForeground = false
        Timber.d("App in background")
    }

    fun isAppInForeground(): Boolean = isInForeground

    fun getApplicationScope(): CoroutineScope = applicationScope

    companion object {
        @Volatile
        private lateinit var instance: BaseApplication

        fun getInstance(): BaseApplication = instance

        fun getApplicationContext(): Context = instance.applicationContext
    }
}