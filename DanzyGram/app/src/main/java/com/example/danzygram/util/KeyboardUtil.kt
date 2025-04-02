package com.example.danzygram.util

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

object KeyboardUtil {
    fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus ?: View(activity)
        hideKeyboard(view)
    }

    fun hideKeyboard(fragment: Fragment) {
        fragment.view?.let { view ->
            hideKeyboard(view)
        }
    }

    fun focusAndShowKeyboard(editText: EditText) {
        editText.requestFocus()
        editText.postDelayed({
            showKeyboard(editText)
        }, 200)
    }

    fun clearFocusAndHideKeyboard(editText: EditText) {
        editText.clearFocus()
        hideKeyboard(editText)
    }

    fun isKeyboardVisible(rootView: View): Boolean {
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = rootView.height
        val keypadHeight = screenHeight - rect.bottom
        return keypadHeight > screenHeight * 0.15
    }

    fun setKeyboardVisibilityListener(
        activity: Activity,
        onKeyboardVisibilityChanged: (Boolean) -> Unit
    ): ViewTreeObserver.OnGlobalLayoutListener {
        val contentView = activity.findViewById<ViewGroup>(android.R.id.content)
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            onKeyboardVisibilityChanged(isKeyboardVisible(contentView))
        }
        contentView.viewTreeObserver.addOnGlobalLayoutListener(listener)
        return listener
    }

    fun removeKeyboardVisibilityListener(
        activity: Activity,
        listener: ViewTreeObserver.OnGlobalLayoutListener
    ) {
        val contentView = activity.findViewById<ViewGroup>(android.R.id.content)
        contentView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }

    fun getKeyboardHeight(rootView: View): Int {
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        return rootView.height - rect.bottom
    }

    fun adjustResize(rootView: View) {
        rootView.setOnApplyWindowInsetsListener { view, windowInsets ->
            val insets = ViewCompat.getRootWindowInsets(view)
            val imeHeight = insets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
            view.setPadding(0, 0, 0, imeHeight)
            windowInsets
        }
    }

    fun disableKeyboardVisibility(editText: EditText) {
        editText.showSoftInputOnFocus = false
    }

    fun enableKeyboardVisibility(editText: EditText) {
        editText.showSoftInputOnFocus = true
    }

    fun moveCursorToEnd(editText: EditText) {
        editText.setSelection(editText.text.length)
    }

    fun moveCursorToStart(editText: EditText) {
        editText.setSelection(0)
    }

    fun moveCursorToPosition(editText: EditText, position: Int) {
        if (position in 0..editText.text.length) {
            editText.setSelection(position)
        }
    }

    fun setKeyboardDoneAction(editText: EditText, onDone: () -> Unit) {
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                onDone()
                true
            } else {
                false
            }
        }
    }

    fun setKeyboardNextAction(editText: EditText, onNext: () -> Unit) {
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                onNext()
                true
            } else {
                false
            }
        }
    }

    fun setKeyboardSearchAction(editText: EditText, onSearch: () -> Unit) {
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                onSearch()
                true
            } else {
                false
            }
        }
    }

    fun setKeyboardSendAction(editText: EditText, onSend: () -> Unit) {
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                onSend()
                true
            } else {
                false
            }
        }
    }
}