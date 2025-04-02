package com.example.danzygram.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.danzygram.util.KeyboardUtil
import com.example.danzygram.util.NetworkUtil
import com.example.danzygram.util.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseDialogFragment<VB : ViewDataBinding> : DialogFragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun initViews()
    abstract fun observeData()
    abstract fun setupListeners()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
        initViews()
        observeData()
        setupListeners()
        observeNetworkState()
    }

    private fun observeNetworkState() {
        viewLifecycleOwner.lifecycleScope.launch {
            NetworkUtil.observeNetworkState(requireContext()).collect { isConnected ->
                handleNetworkState(isConnected)
            }
        }
    }

    private fun handleNetworkState(isConnected: Boolean) {
        if (!isConnected) {
            showNetworkError()
        }
    }

    private fun showNetworkError() {
        binding.root.showSnackbar(
            "No internet connection",
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        )
    }

    protected fun hideKeyboard() {
        KeyboardUtil.hideKeyboard(requireActivity())
    }

    protected fun showKeyboard(view: View) {
        KeyboardUtil.showKeyboard(view)
    }

    protected fun launchWhenStarted(block: suspend CoroutineScope.() -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }

    protected fun launchWhenResumed(block: suspend CoroutineScope.() -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
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

    protected fun isNetworkAvailable(): Boolean {
        return NetworkUtil.isNetworkAvailable(requireContext())
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

    override fun onDestroyView() {
        hideKeyboard()
        _binding = null
        super.onDestroyView()
    }

    override fun onPause() {
        hideKeyboard()
        super.onPause()
    }

    protected fun dismissWithAnimation() {
        dialog?.let { dialog ->
            dialog.window?.setWindowAnimations(
                android.R.style.Animation_Dialog
            )
            dialog.dismiss()
        }
    }

    protected fun getBaseActivity(): BaseActivity<*>? {
        return activity as? BaseActivity<*>
    }

    protected fun setDialogSize(width: Int, height: Int) {
        dialog?.window?.setLayout(width, height)
    }

    protected fun setDialogBackground(drawableResId: Int) {
        dialog?.window?.setBackgroundDrawableResource(drawableResId)
    }

    protected fun enableDim(dimAmount: Float = 0.6f) {
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.window?.setDimAmount(dimAmount)
    }

    protected fun disableDim() {
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
}