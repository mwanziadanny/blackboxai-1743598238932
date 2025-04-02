package com.example.danzygram.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.danzygram.util.KeyboardUtil
import com.example.danzygram.util.NetworkUtil
import com.example.danzygram.util.showSnackbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseBottomSheetDialogFragment<VB : ViewDataBinding> : BottomSheetDialogFragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun initViews()
    abstract fun observeData()
    abstract fun setupListeners()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            setOnShowListener {
                val bottomSheet = findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                )
                bottomSheet?.let { sheet ->
                    bottomSheetBehavior = BottomSheetBehavior.from(sheet)
                    setupBottomSheetBehavior(bottomSheetBehavior)
                }
            }
        }
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

    protected open fun setupBottomSheetBehavior(behavior: BottomSheetBehavior<View>) {
        behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
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
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    protected fun expandSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    protected fun collapseSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    protected fun hideSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    protected fun isDraggable(draggable: Boolean) {
        bottomSheetBehavior.isDraggable = draggable
    }

    protected fun setSheetState(state: Int) {
        bottomSheetBehavior.state = state
    }

    protected fun getBaseActivity(): BaseActivity<*>? {
        return activity as? BaseActivity<*>
    }

    protected fun setPeekHeight(height: Int) {
        bottomSheetBehavior.peekHeight = height
    }

    protected fun setMaxHeight(height: Int) {
        bottomSheetBehavior.maxHeight = height
    }
}