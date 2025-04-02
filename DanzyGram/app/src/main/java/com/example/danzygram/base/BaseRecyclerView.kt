package com.example.danzygram.base

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.danzygram.R

abstract class BaseRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var emptyView: View? = null
    private var loadingView: View? = null
    private var errorView: View? = null

    private val dataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            checkIfEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            checkIfEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            checkIfEmpty()
        }
    }

    init {
        setupAttributes(attrs)
        setupRecyclerView()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.BaseRecyclerView)
            try {
                val emptyViewId = typedArray.getResourceId(
                    R.styleable.BaseRecyclerView_emptyView,
                    View.NO_ID
                )
                val loadingViewId = typedArray.getResourceId(
                    R.styleable.BaseRecyclerView_loadingView,
                    View.NO_ID
                )
                val errorViewId = typedArray.getResourceId(
                    R.styleable.BaseRecyclerView_errorView,
                    View.NO_ID
                )

                if (emptyViewId != View.NO_ID) {
                    setEmptyView(emptyViewId)
                }
                if (loadingViewId != View.NO_ID) {
                    setLoadingView(loadingViewId)
                }
                if (errorViewId != View.NO_ID) {
                    setErrorView(errorViewId)
                }
            } finally {
                typedArray.recycle()
            }
        }
    }

    private fun setupRecyclerView() {
        if (layoutManager == null) {
            layoutManager = LinearLayoutManager(context)
        }
        setHasFixedSize(true)
        itemAnimator = DefaultItemAnimator()
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(dataObserver)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(dataObserver)
        checkIfEmpty()
    }

    fun setEmptyView(view: View) {
        emptyView = view
        checkIfEmpty()
    }

    fun setEmptyView(viewId: Int) {
        if (viewId != View.NO_ID) {
            val rootView = rootView
            val view = rootView.findViewById<View>(viewId)
            setEmptyView(view)
        }
    }

    fun setLoadingView(view: View) {
        loadingView = view
    }

    fun setLoadingView(viewId: Int) {
        if (viewId != View.NO_ID) {
            val rootView = rootView
            val view = rootView.findViewById<View>(viewId)
            setLoadingView(view)
        }
    }

    fun setErrorView(view: View) {
        errorView = view
    }

    fun setErrorView(viewId: Int) {
        if (viewId != View.NO_ID) {
            val rootView = rootView
            val view = rootView.findViewById<View>(viewId)
            setErrorView(view)
        }
    }

    fun showLoading() {
        emptyView?.visibility = View.GONE
        errorView?.visibility = View.GONE
        loadingView?.visibility = View.VISIBLE
        visibility = View.GONE
    }

    fun showError() {
        emptyView?.visibility = View.GONE
        loadingView?.visibility = View.GONE
        errorView?.visibility = View.VISIBLE
        visibility = View.GONE
    }

    fun showContent() {
        emptyView?.visibility = View.GONE
        loadingView?.visibility = View.GONE
        errorView?.visibility = View.GONE
        visibility = View.VISIBLE
    }

    private fun checkIfEmpty() {
        val adapter = adapter
        if (adapter != null) {
            if (adapter.itemCount == 0) {
                emptyView?.visibility = View.VISIBLE
                visibility = View.GONE
            } else {
                emptyView?.visibility = View.GONE
                visibility = View.VISIBLE
            }
        }
    }

    fun addOnScrollListener(
        onScrolled: ((dx: Int, dy: Int) -> Unit)? = null,
        onScrollStateChanged: ((newState: Int) -> Unit)? = null
    ) {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                onScrolled?.invoke(dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                onScrollStateChanged?.invoke(newState)
            }
        })
    }

    fun addLoadMoreListener(loadMore: () -> Unit) {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!canScrollVertically(1)) {
                    loadMore()
                }
            }
        })
    }

    fun scrollToTop() {
        smoothScrollToPosition(0)
    }

    fun scrollToBottom() {
        adapter?.let {
            smoothScrollToPosition(it.itemCount - 1)
        }
    }
}