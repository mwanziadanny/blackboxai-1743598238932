package com.example.danzygram.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.danzygram.util.KeyboardUtil
import com.example.danzygram.util.NetworkUtil
import com.example.danzygram.util.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseActivity<VB : ViewDataBinding> : AppCompatActivity() {
    
    protected lateinit var binding: VB
    private var networkSnackbar: com.google.android.material.snackbar.Snackbar? = null

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun initViews()
    abstract fun observeData()
    abstract fun setupListeners()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        binding.lifecycleOwner = this

        initViews()
        observeData()
        setupListeners()
        observeNetworkState()
    }

    private fun observeNetworkState() {
        lifecycleScope.launch {
            NetworkUtil.observeNetworkState(this@BaseActivity).collect { isConnected ->
                handleNetworkState(isConnected)
            }
        }
    }

    private fun handleNetworkState(isConnected: Boolean) {
        if (!isConnected) {
            showNetworkError()
        } else {
            hideNetworkError()
        }
    }

    private fun showNetworkError() {
        if (networkSnackbar == null) {
            networkSnackbar = binding.root.showSnackbar(
                "No internet connection",
                com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
            )
        }
        networkSnackbar?.show()
    }

    private fun hideNetworkError() {
        networkSnackbar?.dismiss()
        networkSnackbar = null
    }

    protected fun hideKeyboard() {
        KeyboardUtil.hideKeyboard(this)
    }

    protected fun showKeyboard(view: View) {
        KeyboardUtil.showKeyboard(view)
    }

    protected fun launchWhenStarted(block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }

    protected fun launchWhenResumed(block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED, block)
        }
    }

    protected fun showError(message: String) {
        binding.root.showSnackbar(message)
    }

    protected fun showError(messageResId: Int) {
        binding.root.showSnackbar(messageResId)
    }

    protected fun showMessage(message: String) {
        binding.root.showSnackbar(message)
    }

    protected fun showMessage(messageResId: Int) {
        binding.root.showSnackbar(messageResId)
    }

    override fun onDestroy() {
        hideKeyboard()
        super.onDestroy()
    }

    override fun onPause() {
        hideKeyboard()
        super.onPause()
    }

    protected fun isNetworkAvailable(): Boolean {
        return NetworkUtil.isNetworkAvailable(this)
    }

    protected fun handleError(error: Throwable) {
        error.message?.let { showError(it) }
    }

    protected fun handleLoading(isLoading: Boolean) {
        // Override in child classes if needed
    }

    protected fun handleEmpty() {
        // Override in child classes if needed
    }

    protected fun refreshData() {
        // Override in child classes if needed
    }

    protected fun onBackPressed() {
        hideKeyboard()
        super.onBackPressed()
    }

    protected fun finishWithAnimation() {
        finish()
        overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
    }

    protected fun startActivityWithAnimation(intent: android.content.Intent) {
        startActivity(intent)
        overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
    }

    protected fun startActivityForResultWithAnimation(
        intent: android.content.Intent,
        requestCode: Int
    ) {
        startActivityForResult(intent, requestCode)
        overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
    }
}